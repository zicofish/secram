package com.sg.secram.impl;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import java.io.File;
import java.io.IOException;
import com.sg.secram.structure.SecramHeader;
import com.sg.secram.structure.SecramIO;
import com.sg.secram.util.ReferenceUtils;
import com.sg.secram.util.SECRAMUtils;
import com.sg.secram.util.Timings;

public class SECRAMFileReader {
	private SeekableStream inputStream;
	private File secramFile;
	private SecramHeader secramHeader;
	private ReferenceSequenceFile mRsf;
	private SecramIndex secramIndex;
	private SECRAMSecurityFilter filter;

	public SECRAMFileReader(String input, String referenceInput, byte[] key)
			throws IOException {
		secramFile = new File(input);
		inputStream = new SeekableFileStream(secramFile);
		mRsf = ReferenceUtils.findReferenceFile(referenceInput);
		secramIndex = new SecramIndex(new File(secramFile.getAbsolutePath()
				+ ".secrai"));
		filter = new SECRAMSecurityFilter(key);

		readHeader();
	}

	public void readHeader() throws IOException {
		secramHeader = SecramIO.readSecramHeader(inputStream);
	}

	public SecramHeader getSecramHeader() {
		return secramHeader;
	}

	public SAMFileHeader getSAMFileHeader() {
		return secramHeader.getSamFileHeader();
	}

	public SECRAMIterator getCompleteIterator() {
		filter.initPositionEM(secramHeader.getOpeSalt());
		SECRAMIterator secramIterator = new SECRAMIterator(secramHeader,
				inputStream, mRsf, filter);
		return secramIterator;
	}

	/**
	 * This method is only used for non-encrypted secram file
	 */
	public SECRAMIterator query(String ref, int start, int end)
			throws IOException {
		int refID = secramHeader.getSamFileHeader().getSequenceIndex(ref);
		long absoluteStart = SECRAMUtils.getAbsolutePosition(start, refID), absoluteEnd = SECRAMUtils
				.getAbsolutePosition(end, refID);
		return query(absoluteStart, absoluteEnd);
	}

	/**
	 * @param start
	 *            The OPE-encrypted absolute start position
	 * @param end
	 *            The OPE-encrypted absolute end position
	 * @return An iterator over the positions in [start, end]
	 * @throws IOException
	 */
	public SECRAMIterator query(long start, long end) throws IOException {
		long nanoStart = System.nanoTime();
		long offset = secramIndex.getContainerOffset(start);
		if (offset < 0)
			return null;
		inputStream.seek(offset);
		Timings.locateQueryPosition += System.nanoTime() - nanoStart;
		filter.initPositionEM(secramHeader.getOpeSalt());
		filter.setBounds(start, end);
		SECRAMIterator secramIterator = new SECRAMIterator(secramHeader,
				inputStream, mRsf, filter);
		return secramIterator;
	}
}
