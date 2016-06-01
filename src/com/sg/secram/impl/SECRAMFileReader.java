package com.sg.secram.impl;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.BufferedLineReader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.sg.secram.structure.SecramHeader;
import com.sg.secram.structure.SecramIO;
import com.sg.secram.util.ReferenceUtils;
import com.sg.secram.util.SECRAMUtils;
import com.sg.secram.util.Timings;

public class SECRAMFileReader{
	private SeekableStream inputStream;
	private File secramFile;
	private SecramHeader secramHeader;
	private ReferenceSequenceFile  mRsf;
	private SecramIndex secramIndex;
	
	public SECRAMFileReader(String input, String referenceInput) throws IOException{
		secramFile = new File(input);
		inputStream = new SeekableFileStream(secramFile);
		mRsf = ReferenceUtils.findReferenceFile(referenceInput);
		secramIndex = new SecramIndex(new File(secramFile.getAbsolutePath() + ".secrai"));
		
		readHeader();
	}
	
	public void readHeader() throws IOException{
		secramHeader =  SecramIO.readSecramHeader(inputStream);
	}
	
	public SAMFileHeader getSAMFileHeader() {
		return secramHeader.getSamFileHeader();
	}
	
	public SECRAMIterator getCompleteIterator(){
		SECRAMSecurityFilter filter = new SECRAMSecurityFilter();
		filter.initPositionEM(secramHeader.getOpeSalt());
		SECRAMIterator secramIterator = new SECRAMIterator(secramHeader, inputStream, mRsf, filter);
		return secramIterator;
	}
	
	/*
	 * This method is only used for non-encrypted secram file.
	 */
	public SECRAMIterator query(String ref, int start, int end) throws IOException{
		int refID = secramHeader.getSamFileHeader().getSequenceIndex(ref);
		long absoluteStart = SECRAMUtils.getAbsolutePosition(start, refID),
				absoluteEnd = SECRAMUtils.getAbsolutePosition(end, refID);
		return query(absoluteStart, absoluteEnd);
	}
	
	public SECRAMIterator query(long start, long end) throws IOException{
		long nanoStart = System.nanoTime();
		long offset = secramIndex.getContainerOffset(start);
		if(offset < 0)
			return null;
		inputStream.seek(offset);
		Timings.locateQueryPosition += System.nanoTime() - nanoStart;
		SECRAMSecurityFilter filter = new SECRAMSecurityFilter();
		filter.initPositionEM(secramHeader.getOpeSalt());
		filter.setBounds(start, end);
		SECRAMIterator secramIterator = new SECRAMIterator(secramHeader, inputStream, mRsf, filter);
		return secramIterator;
	}
	
	
//	//for random access
//	public SecramRecordOld get(long position) throws IOException {
//		//get the closest marker for this position
//		Entry<Long, Long> entry = pos2index.floorEntry(position);
//		
//		//if no such marker exists, this position isn't in the file
//		if (entry == null) return null;
//		
//		//moves the reader to the marker
//		randomAccessReader.sync(entry.getValue());
//		
//		//iterate to the correct position
//		SecramRecordOld record = null;
//		try {
//			do {
//				SecramRecordAvro avroRecord = randomAccessReader.next();
//				record = new SecramRecordOld(randomAccessReader.next(), getReferenceBase(avroRecord.getPOS()));
//			}
//			while(record.getPOS() < position);
//		}
//		catch(NoSuchElementException ex) {
//			return null; //if we reach the end of the file, the position isn't in the file
//		}
//		
//		return record;
//	}
//	
//	public char getReferenceBase(long pos) throws ArrayIndexOutOfBoundsException,IOException {
//		int refID = (int)(pos>>32);
//		if (refID == cachedRefID){
//			return (char)cachedRefSequence[(int)pos];
//		}
//		SAMSequenceRecord seq = mSAMFileHeader.getSequence(refID);
//		ReferenceSequence rs = mRsf.getSequence(seq.getSequenceName());
//
//		if (rs == null || rs.length() != seq.getSequenceLength()) {
//			System.err.println("Could not find the reference sequence " + seq.getSequenceName() + " in the file");
//			throw new IOException("No such sequence in file");
//		}
//		
//		cachedRefID = refID;
//		cachedRefSequence = rs.getBases();
//		
//		return (char)cachedRefSequence[(int)pos];
//	}
}
