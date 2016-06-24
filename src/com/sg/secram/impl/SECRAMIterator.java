package com.sg.secram.impl;

import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;
import com.sg.secram.structure.SecramContainer;
import com.sg.secram.structure.SecramContainerParser;
import com.sg.secram.structure.SecramHeader;
import com.sg.secram.util.Timings;

/**
 * Iterates the SECRAM records in a SECRAM file. There will be two levels of
 * iteration: the first level iterates the containers, and second level iterates
 * records in each container.
 * 
 * @author zhihuang
 *
 */
public class SECRAMIterator implements Iterator<SecramRecord> {
	private SecramHeader secramHeader;
	private ReferenceSequenceFile mRsf;
	private List<SecramRecord> secramRecords = null;
	private Iterator<SecramContainer> containerIterator;
	private SecramContainer container;
	private SecramContainerParser parser;
	private SECRAMSecurityFilter filter;
	private Iterator<SecramRecord> iterator = Collections
			.<SecramRecord> emptyList().iterator();

	private byte[] cachedRefSequence = null;
	private int cachedRefID = -1;

	private long encPosition = -1;
	private int offset = -1;

	public SECRAMIterator(SecramHeader header, InputStream inputStream,
			ReferenceSequenceFile referenceFile, SECRAMSecurityFilter filter) {
		this.secramHeader = header;
		this.mRsf = referenceFile;
		this.filter = filter;
		this.containerIterator = new SECRAMContainerIterator(inputStream,
				filter);
		this.parser = new SecramContainerParser();
	}

	private void nextContainer() throws IllegalArgumentException,
			IllegalAccessException, IOException {
		if (!containerIterator.hasNext()) {
			container = null;
			secramRecords = null;
			return;
		}
		container = containerIterator.next();
		long nanoStart = System.nanoTime();
		secramRecords = parser.getRecords(container, filter);
		Timings.decompression += System.nanoTime() - nanoStart;
		iterator = secramRecords.iterator();
		encPosition = container.absolutePosStart;
		offset = -1;
	}

	@Override
	public boolean hasNext() {
		if (!iterator.hasNext()) {
			try {
				nextContainer();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return iterator.hasNext();
	}

	@Override
	public SecramRecord next() {
		while (hasNext()) {
			SecramRecord record = iterator.next();
			if (record.getAbsolutePosition() == encPosition)
				offset += 1;
			else {
				encPosition = record.getAbsolutePosition();
				offset = 0;
			}
			if (!filter.isRecordPermitted(encPosition, offset))
				continue;
			long nanoStart = System.nanoTime();
			{// decrypt the position
				long orgPos = offset + filter.decryptPosition(encPosition);
				record.setAbsolutionPosition(orgPos);
				for (ReadHeader rh : record.mReadHeaders) {
					long nextPos = filter.decryptPosition(rh
							.getNextAbsolutePosition());
					rh.setNextAbsolutionPosition(nextPos);
				}
			}
			Timings.decryption += System.nanoTime() - nanoStart;
			try {
				record.setReferenceBase(getReferenceBase(record
						.getAbsolutePosition()));
			} catch (Exception e) {
				throw new RuntimeException(
						"Error while getting reference base for position: "
								+ record.getAbsolutePosition());
			}
			return record;
		}
		return null;
	}

	public char getReferenceBase(long pos)
			throws ArrayIndexOutOfBoundsException, IOException {
		int refID = (int) (pos >> 32);
		if (refID == cachedRefID) {
			return (char) cachedRefSequence[(int) pos];
		}
		SAMSequenceRecord seq = secramHeader.getSamFileHeader().getSequence(
				refID);
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
}
