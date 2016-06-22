package com.sg.secram.impl;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sg.secram.SECRAMEncryptionFilter;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;
import com.sg.secram.structure.SecramBlock;
import com.sg.secram.structure.SecramCompressionHeaderFactory;
import com.sg.secram.structure.SecramContainer;
import com.sg.secram.structure.SecramContainerFactory;
import com.sg.secram.structure.SecramContainerIO;
import com.sg.secram.structure.SecramHeader;
import com.sg.secram.structure.SecramIO;
import com.sg.secram.util.Timings;

public class SECRAMFileWriter {
	private Log log = Log.getInstance(SECRAMFileWriter.class);

	private File secramFile;
	private int recordsPerContainer = SecramContainer.DEFATUL_RECORDS_PER_CONTAINER;
	private SecramContainerFactory containerFactory;
	private SAMFileHeader samFileHeader;
	private SECRAMSecurityFilter filter;
	private SecramHeader secramHeader;
	private SecramIndex secramIndex;

	private final OutputStream outputStream;
	private long offset;

	private List<SecramRecord> secramRecords = new ArrayList<SecramRecord>();

	public SECRAMFileWriter(final File output, final SAMFileHeader header,
			final byte[] key) throws IOException {
		this.secramFile = output;
		this.outputStream = new BufferedOutputStream(new FileOutputStream(
				output));
		this.samFileHeader = header;
		this.filter = new SECRAMSecurityFilter(key);
		this.containerFactory = new SecramContainerFactory(header,
				recordsPerContainer);
		this.secramIndex = new SecramIndex();

		writeHeader();
	}

	public SAMFileHeader getBAMHeader() {
		return samFileHeader;
	}

	public long getNumberOfWrittenRecords() {
		return this.containerFactory.getGlobalRecordCounter();
	}

	public void close() {
		try {
			if (!secramRecords.isEmpty())
				flushContainer();
			outputStream.flush();
			outputStream.close();

			// Write the index file
			File indexFile = new File(secramFile.getAbsolutePath() + ".secrai");
			secramIndex.writeIndexToFile(indexFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean shouldFlushContainer(final SecramRecord nextRecord) {
		return secramRecords.size() >= recordsPerContainer;
	}

	public void appendRecord(SecramRecord record) throws Exception {
		if (shouldFlushContainer(record)) {
			flushContainer();
		}
		secramRecords.add(record);
	}

	public void flushContainer() throws IllegalArgumentException,
			IllegalAccessException, IOException {
		// encrypt the positions
		long prevOrgPosition = secramRecords.get(0).getAbsolutePosition();
		long prevEncPosition = -1;
		long nanoStart = System.nanoTime();
		for (SecramRecord record : secramRecords) {
			if (record.getAbsolutePosition() - prevOrgPosition != 1) {
				long encPos = filter.encryptPosition(record
						.getAbsolutePosition());
				prevOrgPosition = record.getAbsolutePosition();
				record.setAbsolutionPosition(encPos);
				prevEncPosition = encPos;
			} else {
				record.setAbsolutionPosition(prevEncPosition);
				prevOrgPosition += 1;
			}
			for (ReadHeader rh : record.mReadHeaders) {
				long encNextPos = filter.encryptPosition(rh
						.getNextAbsolutePosition());
				rh.setNextAbsolutionPosition(encNextPos);
			}
		}
		Timings.encryption += System.nanoTime() - nanoStart;

		// process all delta information for relative integer/long encoding
		long prevAbsolutePosition = secramRecords.get(0).getAbsolutePosition();
		int prevCoverage = secramRecords.get(0).mPosCigar.mCoverage;
		int prevQualLen = secramRecords.get(0).mQualityScores.length;
		for (SecramRecord record : secramRecords) {
			record.absolutePositionDelta = record.getAbsolutePosition()
					- prevAbsolutePosition;
			prevAbsolutePosition = record.getAbsolutePosition();
			record.coverageDelta = record.mPosCigar.mCoverage - prevCoverage;
			prevCoverage = record.mPosCigar.mCoverage;
			record.qualityLenDelta = record.mQualityScores.length - prevQualLen;
			prevQualLen = record.mQualityScores.length;
		}

		// initialize the block encryption for this container
		int containerID = containerFactory.getGlobalContainerCounter();
		long containerSalt = 0;
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			containerSalt = sr.nextLong();
			filter.initContainerEM(containerSalt, containerID);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		nanoStart = System.nanoTime();
		SecramContainer container = containerFactory.buildContainer(
				secramRecords, containerSalt);
		Timings.compression += System.nanoTime() - nanoStart;

		// encrypt the sensitive block (the first external block)
		SecramBlock sensitiveBlock = container.external
				.get(SecramCompressionHeaderFactory.SENSITIVE_FIELD_EXTERNAL_ID);
		nanoStart = System.nanoTime();
		byte[] encBlock = filter.encryptBlock(sensitiveBlock.getRawContent(),
				containerID);
		Timings.encryption += System.nanoTime() - nanoStart;
		sensitiveBlock.setContent(encBlock, encBlock);

		// write out the container, and log the index
		container.offset = offset;
		secramIndex.addTuple(container.absolutePosStart, container.offset);
		offset += SecramContainerIO.writeContainer(container, outputStream);

		secramRecords.clear();
	}

	private void writeHeader() throws IOException {
		// initialize the order-preserving encryption (ope) for the whole file
		long opeSalt = 0;
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			opeSalt = sr.nextLong();
			opeSalt = -275065164286408096L;
			filter.initPositionEM(opeSalt);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		secramHeader = new SecramHeader(secramFile.getName(), samFileHeader,
				opeSalt);
		offset = SecramIO.writeSecramHeader(secramHeader, outputStream);
	}
}
