package com.sg.secram.impl;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
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


public class SECRAMFileWriter {
	private Log log = Log.getInstance(SECRAMFileWriter.class);
	
	private String fileName;
	private int recordsPerContainer = SecramContainer.DEFATUL_RECORDS_PER_CONTAINER;
	private SecramContainerFactory containerFactory;
	private SAMFileHeader samFileHeader;
	private SECRAMSecurityFilter filter;
	private SecramHeader secramHeader;
	
	private final OutputStream outputStream;
	private long offset;
	
	private List<SecramRecord> secramRecords = new ArrayList<SecramRecord>();
	
	public SECRAMFileWriter(final OutputStream outputStream, final SAMFileHeader header, String fileName, byte[] masterKey) throws IOException {
		this.outputStream = outputStream;
		this.samFileHeader = header;
		this.filter = new SECRAMSecurityFilter(masterKey);
		this.fileName = fileName;
		this.containerFactory = new SecramContainerFactory(header, recordsPerContainer);
		
		writeHeader();
	}
	
	public SAMFileHeader getBAMHeader() {
		return samFileHeader;
	}
	
	public long getNumberOfWrittenRecords(){
		return this.containerFactory.getGlobalRecordCounter();
	}
	
	public void close() {
		try{
			if(!secramRecords.isEmpty())
				flushContainer();
			outputStream.flush();
			outputStream.close();
		} catch(Exception e){
			throw new RuntimeException(e);
		}
		System.out.println("opeSalt: " + secramHeader.getOpeSalt());
	}
	
	public boolean shouldFlushContainer(final SecramRecord nextRecord){
		return secramRecords.size() >= recordsPerContainer;
	}
	
	public void appendRecord(SecramRecord record) throws Exception {
		if(shouldFlushContainer(record)){
			flushContainer();
		}
		secramRecords.add(record);
	}
	
	public void flushContainer() throws IllegalArgumentException, IllegalAccessException, IOException{
		//encrypt the positions
		long prevOrgPosition = secramRecords.get(0).getAbsolutePosition();
		long prevEncPosition = -1;
		for(SecramRecord record : secramRecords){
			if(record.getAbsolutePosition() - prevOrgPosition != 1){
				long encPos = filter.encryptPosition(record.getAbsolutePosition());
				prevOrgPosition = record.getAbsolutePosition();
				record.setAbsolutionPosition(encPos);
				prevEncPosition = encPos;
			}
			else{
				record.setAbsolutionPosition(prevEncPosition);
				prevOrgPosition += 1;
			}
			for(ReadHeader rh : record.mReadHeaders){
				long encNextPos = filter.encryptPosition(rh.getNextAbsolutePosition());
				rh.setNextAbsolutionPosition(encNextPos);
			}
		}
		
		//process all delta information for relative integer/long encoding
		long prevAbsolutePosition = secramRecords.get(0).getAbsolutePosition();
		int prevCoverage = secramRecords.get(0).mPosCigar.mCoverage;
		int prevQualLen = secramRecords.get(0).mQualityScores.length;
		for(SecramRecord record : secramRecords){
			record.absolutePositionDelta = record.getAbsolutePosition() - prevAbsolutePosition;
			prevAbsolutePosition = record.getAbsolutePosition();
			record.coverageDelta = record.mPosCigar.mCoverage - prevCoverage;
			prevCoverage = record.mPosCigar.mCoverage;
			record.qualityLenDelta = record.mQualityScores.length - prevQualLen;
			prevQualLen = record.mQualityScores.length;
		}
		
		//initialize the block encryption for this container
		int containerID = containerFactory.getGlobalContainerCounter();
		long containerSalt = 0;
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			containerSalt = sr.nextLong();
			filter.initContainerEM(containerSalt, containerID);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		SecramContainer container = containerFactory.buildContainer(secramRecords, containerSalt);
		
		//encrypt the sensitive block (the first external block)
		SecramBlock sensitiveBlock = container.external.get(SecramCompressionHeaderFactory.SENSITIVE_FIELD_EXTERNAL_ID);
		byte[] encBlock = filter.encryptBlock(sensitiveBlock.getRawContent(), containerID);
		sensitiveBlock.setContent(encBlock, encBlock);
		
		//write out the container
		container.offset = offset;
		offset += SecramContainerIO.writeContainer(container, outputStream);
		
		secramRecords.clear();
	}

	private void writeHeader() throws IOException{
		//initialize the order-preserving encryption (ope) for the whole file
		long opeSalt = 0;
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			opeSalt = sr.nextLong();
			filter.initPositionEM(opeSalt);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		secramHeader = new SecramHeader(fileName, samFileHeader, opeSalt);
		offset = SecramIO.writeSecramHeader(secramHeader, outputStream);
	}
}
