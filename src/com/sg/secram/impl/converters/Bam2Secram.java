/**
 * Copyright © 2013-2016 Swiss Federal Institute of Technology EPFL and Sophia Genetics SA
 * 
 * All rights reserved
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials provided 
 * with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used 
 * to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * PATENTS NOTICE: Sophia Genetics SA holds worldwide pending patent applications in relation with this 
 * software functionality. For more information and licensing conditions, you should contact Sophia Genetics SA 
 * at info@sophiagenetics.com. 
 */
package com.sg.secram.impl.converters;

import htsjdk.samtools.BAMRecord;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.sg.secram.impl.SECRAMFileWriter;
import com.sg.secram.impl.records.PosCigarFeature;
import com.sg.secram.impl.records.PosCigarFeatureCode;
import com.sg.secram.impl.records.SecramRecord;
import com.sg.secram.util.ReferenceUtils;
import com.sg.secram.util.SECRAMUtils;
import com.sg.secram.util.Timings;

/**
 * Converter from a bam file to a secram file.
 * 
 * @author zhihuang
 */
public class Bam2Secram {

	// TODO handle unaligned reads
	
	private ReferenceSequenceFile mRsf;

	private byte[] cachedRefSequence = null;
	private int cachedRefID = -1;

	private SAMFileHeader mSAMFileHeader;

	/**
	 * @param samFileHeader
	 *            The SAM file header of the BAM file
	 * @param referenceInput
	 *            The path of the reference file.
	 * @throws IOException
	 */
	public Bam2Secram(SAMFileHeader samFileHeader, String referenceInput)
			throws IOException {
		this(samFileHeader, ReferenceUtils.findReferenceFile(referenceInput));
	}

	/**
	 * @param samFileHeader
	 *            The SAM file header of the BAM file
	 * @param rsf
	 *            The reference sequence file.
	 * @throws IOException
	 */
	public Bam2Secram(SAMFileHeader samFileHeader, ReferenceSequenceFile rsf) {
		mSAMFileHeader = samFileHeader;
		mRsf = rsf;
	}

	public ReferenceSequenceFile getReferenceSequenceFile() {
		return mRsf;
	}

	/**
	 * Returns the {@link SecramRecordBuilder} instance corresponding to this
	 * position. A new instance is created if the position is accessed for the
	 * first time.
	 * 
	 * @param position
	 *            The position we want to access
	 * @param secramRecords
	 *            The map to search for the position
	 * @return the instance of {@link SecramRecordBuilder} corresponding to this
	 *         position
	 * @throws IOException
	 */
	private SecramRecordBuilder getBuilder(long position,
			Map<Long, SecramRecordBuilder> pos2Builder) throws IOException {
		SecramRecordBuilder result;
		if (!pos2Builder.containsKey(position)) {

			result = new SecramRecordBuilder((int) (position >> 32),
					(int) position, getReferenceBase(position));
			pos2Builder.put(position, result);
		} else {
			result = pos2Builder.get(position);
		}
		return result;
	}

	/**
	 * Get the reference base of the position.
	 * @param pos
	 *            The absolute position. Chromosome ID is in the higher 32 bits,
	 *            and chromosome position is in the lower 32 bits.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IOException
	 */
	private char getReferenceBase(long pos)
			throws ArrayIndexOutOfBoundsException, IOException {
		int refID = (int) (pos >> 32);
		if (refID == cachedRefID) {
			return (char) cachedRefSequence[(int) pos];
		}
		SAMSequenceRecord seq = mSAMFileHeader.getSequence(refID);
		ReferenceSequence rs = mRsf.getSequence(seq.getSequenceName());

		if (rs == null || rs.length() != seq.getSequenceLength()) {
			System.err.println("Could not find the reference sequence "
					+ seq.getSequenceName() + " in the file");
			throw new IOException("No such sequence in file");
		}

		cachedRefID = refID;
		cachedRefSequence = rs.getBases();

		return (char) cachedRefSequence[(int) pos];
	}

	/**
	 * Reads the input file in the BAM format and saves it to the output file in
	 * the SECRAM format, using the key for encryption.
	 * 
	 * @param input
	 *            The BAM file to read from. The BAM records *SHOULD* be ordered
	 *            by their starting positions in the file.
	 * @param output
	 *            The new SECRAM file to create
	 * @param refFileName
	 *            Path of the reference file
	 * @param key Encryption key.
	 * @throws IOException
	 */
	public static void convertFile(File input, File output, String refFileName,
			byte[] key) throws IOException {
		SamReader reader = SamReaderFactory.makeDefault()
				.validationStringency(ValidationStringency.SILENT).open(input);

		long startTime = System.currentTimeMillis();

		SAMFileHeader samFileHeader = reader.getFileHeader();

		SECRAMFileWriter secramFileWriter = new SECRAMFileWriter(output,
				samFileHeader, key);

		Bam2Secram converter = new Bam2Secram(samFileHeader, refFileName);

		// a map that returns the record corresponding to this position
		TreeMap<Long, SecramRecordBuilder> pos2Builder = new TreeMap<Long, SecramRecordBuilder>();
		try {
			for (final SAMRecord samRecord : reader) {
				if (samRecord.getReadUnmappedFlag())
					continue;
				BAMRecord bamRecord = (BAMRecord) samRecord;

				long nanoStart = System.nanoTime();
				converter.addBamRecordToSecramRecords(bamRecord, pos2Builder);
				Timings.transposition += System.nanoTime() - nanoStart;

				long startPosition = SECRAMUtils.getAbsolutePosition(
						bamRecord.getAlignmentStart() - 1,
						bamRecord.getReferenceIndex());
				long pos;
				// Check for any position smaller than the start of this read.
				// Having this loop is to make sure
				// we will write complete secram records out to disk, and thus
				// will not run out of memory.
				while (!pos2Builder.isEmpty()
						&& (pos = pos2Builder.firstKey()) < startPosition) {
					SecramRecordBuilder builder = pos2Builder.remove(pos);
					// removes the position from the list, and saves it
					SecramRecord completedRecord = builder.close(); 
					secramFileWriter.appendRecord(completedRecord);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Save the remaining SECRAM records
			while (!pos2Builder.isEmpty()) {
				SecramRecordBuilder remainingBuilder = pos2Builder
						.remove(pos2Builder.firstKey());
				SecramRecord remainingRecord = remainingBuilder.close();
				secramFileWriter.appendRecord(remainingRecord);
			}
			// Close the writer
			secramFileWriter.close();
			System.out.println("Total number of records written out: "
					+ secramFileWriter.getNumberOfWrittenRecords());
			long totalTime = System.currentTimeMillis() - startTime;
			System.out.println("Total time elapsed: "
					+ SECRAMUtils.timeString(totalTime));
		}

	}

	/**
	 * Create a set of incomplete SECRAM records from a set of BAM records. The
	 * BAM records should be ordered by their starting positions.
	 * 
	 * @throws IOException
	 */
	public Map<Long, SecramRecordBuilder> createSECRAMRecords(
			BAMRecord... records) throws IOException {
		TreeMap<Long, SecramRecordBuilder> pos2Builder = new TreeMap<Long, SecramRecordBuilder>();
		for (int i = 0; i < records.length; i++)
			addBamRecordToSecramRecords(records[i], pos2Builder);
		return pos2Builder;
	}

	/**
	 * Extract information on each position of the BAM record, and add it to the
	 * corresponding SECRAM record builder.
	 * 
	 * @param bamRecord The BAM record to be processed.
	 * @param pos2Builder Map from positions to their SECRAM record builders. If a position is not found in the map
	 * during processing, new entry of this position will be inserted into the map.
	 * @throws IOException
	 */
	public void addBamRecordToSecramRecords(BAMRecord bamRecord,
			Map<Long, SecramRecordBuilder> pos2Builder) throws IOException {

		long startPosition = SECRAMUtils.getAbsolutePosition(
				bamRecord.getAlignmentStart() - 1,
				bamRecord.getReferenceIndex());

		long pos = startPosition;

		SecramRecordBuilder curBuilder = getBuilder(pos, pos2Builder);

		curBuilder.addReadHeader(bamRecord);

		String seq = bamRecord.getReadString();

		byte[] qualityScores = bamRecord.getBaseQualities();
		int qualityOffset = 0;

		boolean starting = true;
		PosCigarFeatureCode code;
		List<PosCigarFeature> features = new LinkedList<PosCigarFeature>();

		Iterator<CigarElement> iter = bamRecord.getCigar().getCigarElements()
				.iterator();
		CigarElement element;

		while (iter.hasNext()) {
			element = iter.next();
			CigarOperator op = element.getOperator();
			int opLength = element.getLength();

			String subSeq; // sub sequence from the read string

			// updates the position in the read sequence according to the
			// operator
			switch (op) {
			case M:
			case I:
			case S:
			case EQ:
			case X:
				subSeq = seq.substring(0, opLength); // retrieve the sub sequence for this operator
				seq = seq.substring(opLength); // updates remaining string
				break;
			default:
				subSeq = "";
			}

			switch (op) {
			case S:
			case I:
				try {
					curBuilder.updateScores(qualityScores, qualityOffset,
							element.getLength());
				} catch (Exception e) {
					System.out.println(bamRecord.getReferenceName());
					System.out.println(bamRecord.getAlignmentStart());
					System.out.println(op);
					System.out.println(opLength);
					System.exit(1);
				}
				qualityOffset += element.getLength();
			case H:
			case P:
				code = PosCigarFeatureCode.getFeatureCode(op, starting, false);
				features.add(new PosCigarFeature(curBuilder.getCoverage(),
						code, opLength, subSeq));
				break;
			case D:
			case N:
				//
				if (!starting) {
					curBuilder.addFeaturesToNextRead(features);
					curBuilder = getBuilder(++pos, pos2Builder);
					features = new LinkedList<PosCigarFeature>();
				}
				// Process the first delete/skip position
				code = PosCigarFeatureCode.getFeatureCode(op, false, false);
				features.add(new PosCigarFeature(curBuilder.getCoverage(),
						code, 1, ""));

				// Process the following delete/skip positions
				for (int i = 1; i < opLength; i++) {
					curBuilder.addFeaturesToNextRead(features);
					curBuilder = getBuilder(++pos, pos2Builder);
					features = new LinkedList<PosCigarFeature>();
					features.add(new PosCigarFeature(curBuilder.getCoverage(),
							code, 1, ""));
				}
				starting = false;
				break;
			case M:
			case EQ:
			case X:
				if (!starting) {
					curBuilder.addFeaturesToNextRead(features);
					curBuilder = getBuilder(++pos, pos2Builder);
					features = new LinkedList<PosCigarFeature>();
				}
				// Process the first match position
				code = PosCigarFeatureCode.getFeatureCode(op, false, false);
				if (curBuilder.getRefBase() != subSeq.charAt(0)) {
					features.add(new PosCigarFeature(curBuilder.getCoverage(),
							code, 1, subSeq.substring(0, 1)));
				}
				curBuilder.updateScores(qualityScores, qualityOffset++, 1);

				// Process the following match positions
				for (int i = 1; i < opLength; i++) {
					curBuilder.addFeaturesToNextRead(features);
					curBuilder = getBuilder(++pos, pos2Builder);
					features = new LinkedList<PosCigarFeature>();
					if (curBuilder.getRefBase() != subSeq.charAt(i)) {
						features.add(new PosCigarFeature(curBuilder
								.getCoverage(), code, 1, subSeq.substring(i,
								i + 1)));
					}
					curBuilder.updateScores(qualityScores, qualityOffset++, 1);
				}
				starting = false;
				break;
			}

		}
		// Add features to the last position
		curBuilder.addFeaturesToNextRead(features);
		assert qualityOffset == qualityScores.length : "Quality scores have not been consumed completely";
	}
}
