package com.sg.secram.impl;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
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

import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;

import com.sg.secram.avro.SecramHeaderAvro;
import com.sg.secram.avro.SecramRecordAvro;
import com.sg.secram.impl.records.SecramRecordOld;
import com.sg.secram.structure.SecramHeader;
import com.sg.secram.structure.SecramIO;
import com.sg.secram.util.ReferenceUtils;

public class SECRAMFileReader{
	private InputStream inputStream;
	private File secramFile;
	private SECRAMIterator secramIterator;
	private SecramHeader secramHeader;
	
	private ReferenceSequenceFile  mRsf;
	
	private SECRAMSecurityFilter mFilter = null;

	//For my local invocation
//	public SECRAMFileReader(String input) throws IOException{
//		this(input, "./data/hs37d5.fa");
//	}
	
	public SECRAMFileReader(String input, String referenceInput, byte[] masterKey) throws IOException{
		secramFile = new File(input);
		inputStream = new FileInputStream(secramFile);
		mRsf = ReferenceUtils.findReferenceFile(referenceInput);
		mFilter = new SECRAMSecurityFilter(masterKey);
		
		readHeader(inputStream);
		secramIterator = new SECRAMIterator(secramHeader, inputStream, mRsf, mFilter);
	}
	
	public void readHeader(InputStream inputStream) throws IOException{
		secramHeader =  SecramIO.readSecramHeader(inputStream);
		mFilter.initPositionEM(secramHeader.getOpeSalt());
	}
	
	public SAMFileHeader getSAMFileHeader() {
		return secramHeader.getSamFileHeader();
	}
	
	public SECRAMIterator getIterator(){
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
