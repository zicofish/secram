package com.sg.secram.impl.converters;

import htsjdk.samtools.BAMRecord;
import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sg.secram.impl.SECRAMFileReader;
import com.sg.secram.impl.SECRAMIterator;
import com.sg.secram.impl.records.PosCigarFeature;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;
import com.sg.secram.util.SECRAMUtils;
import com.sg.secram.util.Timings;

/**
 * Converter from a SECRAM file to a BAM file.
 * 
 * @author zhihuang
 *
 */
public class Secram2Bam {

	private static Log log = Log.getInstance(Secram2Bam.class);

	private SAMFileHeader mSAMFileHeader;

	public Secram2Bam(SAMFileHeader samFileHeader) throws IOException {
		mSAMFileHeader = samFileHeader;
	}

	/**
	 * Reads the input file in the SECRAM format and saves it to the output file
	 * in the BAM format, using the key for decryption.
	 * 
	 * @param input
	 *            The SECRAM file to read from
	 * @param output
	 *            The new BAM file to create
	 * @param refFileName
	 *            Path of the reference file
	 * @param key
	 * @throws IOException
	 *             If an {@link IOException} occurs during the operation
	 */
	public static void convertFile(File input, File output, String refFileName,
			byte[] key) throws IOException {

		SECRAMFileReader reader = new SECRAMFileReader(input.getAbsolutePath(),
				refFileName, key);
		SAMFileWriter bamWriter = new SAMFileWriterFactory().makeBAMWriter(
				reader.getSAMFileHeader(), true, output);

		long startTime = System.currentTimeMillis();

		Secram2Bam converter = new Secram2Bam(reader.getSAMFileHeader());

		LinkedList<BAMRecordBuilder> incompleteReads = new LinkedList<BAMRecordBuilder>();
		try {
			SECRAMIterator secramIterator = reader.getCompleteIterator();
			while (secramIterator.hasNext()) {
				SecramRecord record = secramIterator.next();
				int oneBasedPosition = record.mPosition + 1;

				long nanoStart = System.nanoTime();
				converter.addSECRAMRecordToIncompleteBAMRecords(record,
						incompleteReads);
				Timings.invTransposition += System.nanoTime() - nanoStart;

				// Adds complete reads to the BAM file.
				// Sometimes even if a BAM read is complete, we must wait for
				// any potential read that starts before this read but is longer
				// before writing it to the BAM file. Hence we stop the "while"
				// loop when we see a read whose end is
				// bigger than "oneBasedPosition", even though there could be
				// subsequent reads whose ends are not
				// bigger than "oneBasedPosition".
				while (!incompleteReads.isEmpty()
						&& incompleteReads.getFirst().getAlignmentEnd() <= oneBasedPosition) {
					bamWriter.addAlignment(incompleteReads.removeFirst()
							.close());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// there shouldn't be any incomplete records left!
			if (incompleteReads.size() > 0) {
				log.error(incompleteReads.size() + " incomplete read(s)!");
				for (BAMRecordBuilder record : incompleteReads) {
					bamWriter.addAlignment(record.close());
				}
			}
			bamWriter.close();
			long totalTime = System.currentTimeMillis() - startTime;
			System.out.println("Total time elapsed: "
					+ SECRAMUtils.timeString(totalTime));
		}
	}

	public List<BAMRecord> completeBAMRecordsWithException(
			List<BAMRecordBuilder> incompleteReads) {
		List<BAMRecord> completeReads = new ArrayList<BAMRecord>();
		for (BAMRecordBuilder record : incompleteReads) {
			if (!record.isComplete()) {
				throw new IllegalArgumentException(
						"The input secram records can't be used to create *COMPLETE* BAM reads.");
			}
			completeReads.add(record.close());
		}
		return completeReads;
	}

	/*
	 * NOTE: when using the following conversion methods, the caller is
	 * responsible for supplying the secram records in a consecutive manner for
	 * the BAM reads that he is trying to recover.
	 */

	/**
	 * The caller of this method is responsible for supplying secram records
	 * that can be used to create complete BAM records, otherwise no assumptions
	 * can be made about the returned BAM records
	 */
	public List<BAMRecord> createBAMRecords(SecramRecord... records) {
		List<BAMRecordBuilder> incompleteReads = createIncompleteBAMRecord(records);
		return completeBAMRecordsWithException(incompleteReads);
	}

	/**
	 * The caller of this method is responsible for supplying secram records
	 * that contain BAM ReadHeaders for the BAM reads that cover these secram
	 * records, otherwise no assumptions can be made about the returned BAM
	 * records
	 */
	public List<BAMRecordBuilder> createIncompleteBAMRecord(
			SecramRecord... records) {
		List<BAMRecordBuilder> incompleteReads = new ArrayList<BAMRecordBuilder>();
		for (SecramRecord record : records)
			addSECRAMRecordToIncompleteBAMRecords(record, incompleteReads);
		return incompleteReads;
	}

	/**
	 * Add information of the next position (secram record) to the list of
	 * current BAM reads.
	 * 
	 * @param record
	 * @param incompleteReads
	 */
	public void addSECRAMRecordToIncompleteBAMRecords(SecramRecord record,
			List<BAMRecordBuilder> incompleteReads) {
		int refIndex = record.mReferenceIndex;
		int position = record.mPosition;
		int alignmentStart = position + 1;

		for (ReadHeader header : record.mReadHeaders) {
			BAMRecord bamRecord = DefaultSAMRecordFactory.getInstance()
					.createBAMRecord(mSAMFileHeader, refIndex, alignmentStart,
							(short) 0,// readNameLen - we set it to 0 for now,
										// otherwise we have to add it to the
										// variableLengthBlock
							(short) header.mMappingQuality, 0, // indexingBin -
																// it will need
																// to be
																// recomputed at
																// the end, no
																// need to store
																// it now
							0, // cigarLen
							header.mFlags, 
							0, // readLen
							header.mNextReferenceIndex, // mateReferenceSequenceIndex
							header.mNextPosition + 1, // mateAlignmentStart
							header.mTemplateLength, header.mTags);

			bamRecord.getAttribute((short) -1); // forces the computation of the
												// tags from the variable length
												// block

			bamRecord.setReadName(header.mReadName);

			int alignmentEnd = alignmentStart + header.mReferenceLength - 1;

			incompleteReads.add(new BAMRecordBuilder(bamRecord, alignmentStart,
					alignmentEnd));
		}

		int scoreOffset = 0;
		byte[] scores = record.mQualityScores;

		int order = 0;

		for (BAMRecordBuilder builder : incompleteReads) {
			if (builder.isComplete())
				continue;

			List<PosCigarFeature> features = record.mPosCigar
					.getCompleteFeaturesOfRead(order);
			String readStr = "";
			for (PosCigarFeature f : features) {
				switch (f.mOP) {
				case M:
				case X:
				case F:
				case I:
				case R:
				case S:
					readStr += f.mBases;
					break;
				default:
				}
				builder.addElement(f, alignmentStart);
			}

			int scoreLen = readStr.length();

			if (scoreLen > 0) {
				builder.addScores(scores, scoreOffset, scoreLen, alignmentStart);
				builder.addReadElement(readStr, alignmentStart);
				scoreOffset += scoreLen;
			}
			builder.advance();
			order++;
		}
	}
}
