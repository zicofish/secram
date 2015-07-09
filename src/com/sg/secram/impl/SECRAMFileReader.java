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
import java.io.IOException;
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
import com.sg.secram.impl.records.SECRAMRecord;
import com.sg.secram.util.ReferenceUtils;

public class SECRAMFileReader implements Iterable<SECRAMRecord>{
	private byte[] mHeader;
	private byte[] mUnalignedRecords;
	//private final static long MAX_FILE_SIZE = Long.MAX_VALUE;
	
	private SAMFileHeader mSAMFileHeader;
	
	private File mFile;
	private DataFileReader<SecramRecordAvro> randomAccessReader;
	
	//private long writtenBytes = 0;
	private long startPosition = -1;
	private long lastPosition = 0;
	
	private List<Long> positions = null;
	private List<Long> index = null;
	private List<Long> blockSalts = null;
	private Long opeSalt;
	//index that tells which block corresponds to which positions
	private TreeMap<Long,Long> pos2index = new TreeMap<>();
	//index that tells which salt corresponds to which block
	private TreeMap<Long, Long> index2Salt = new TreeMap<>();
	
	private byte[] cachedRefSequence = null;
	private int cachedRefID = -1;
	
	private ReferenceSequenceFile  mRsf;
	
	private SECRAMSecurityFilter mFilter = null;

	//For my local invocation
	public SECRAMFileReader(String input) throws IOException{
		this(input, "./data/hs37d5.fa");
	}
	
	public SECRAMFileReader(String input, String referenceInput) throws IOException {
		this(input, referenceInput, new SECRAMSecurityFilter());
	}
	
	public SECRAMFileReader(String input, String referenceInput, SECRAMSecurityFilter filter) throws IOException{
		mRsf = ReferenceUtils.findReferenceFile(referenceInput);
		mFilter = filter;
		
		mFile = new File(input);
		File headerFile = new File(input+"h");
		
		DatumReader<SecramHeaderAvro> dReader = new SpecificDatumReader<SecramHeaderAvro>(SecramHeaderAvro.class);
		DataFileReader<SecramHeaderAvro> reader = new DataFileReader<SecramHeaderAvro>(headerFile, dReader);
		
		SecramHeaderAvro header = reader.next();
		
		mHeader = header.getBAMHeader().array();
		mSAMFileHeader = convertByteArrayToHeader(mHeader);
		mUnalignedRecords = header.getUnalignedRecords().array();
		
		positions = header.getPositions();
		index = header.getIndex();
		opeSalt = header.getOpeSalt();
		blockSalts = header.getBlockSalts();
		
		Iterator<Long> iter1 = index.iterator();
		Iterator<Long> iter2 = blockSalts.iterator();
		
		for (long i: positions) {
			long j = iter1.next();
			long k = iter2.next();
					
			pos2index.put(i, j);
			index2Salt.put(j, k);
		}
		reader.close();
		
		DatumReader<SecramRecordAvro> recordReader = new SpecificDatumReader<SecramRecordAvro>(SecramRecordAvro.class);
		randomAccessReader = new DataFileReader<SecramRecordAvro>(mFile, recordReader);
		
		mFilter.initPositionEncryptionMethod(opeSalt);
	}
	
	public SAMFileHeader getBAMHeader() {
		return mSAMFileHeader;
	}
	
	public byte[] getUnalignedRecords() {
		return mUnalignedRecords;
	}
	public long getFirstPOS() {
		return startPosition;
	}
	public long getLastPOS() {
		return lastPosition;
	}
	
	private class RecordIterator implements Iterator<SECRAMRecord> {

		private DataFileReader<SecramRecordAvro> reader;
		private int currentBlockID = -1;
		
		private RecordIterator() throws IOException, NoSuchAlgorithmException {
			DatumReader<SecramRecordAvro> dReader = new SpecificDatumReader<SecramRecordAvro>(SecramRecordAvro.class);
			reader = new DataFileReader<SecramRecordAvro>(mFile, dReader);
			currentBlockID = 0;
			mFilter.initPosCigarEncryptionMethod(blockSalts.get(currentBlockID));
		}

		@Override
		public boolean hasNext() {
			if (reader.hasNext()) {
				return true;
			}
			else {
				close();
				return false;
			}
		}

		@Override
		public SECRAMRecord next() {
			SECRAMRecord record = null;
			try {
				SecramRecordAvro avroRecord = reader.next();
				long encPos = avroRecord.getPOS();
				if(currentBlockID < blockSalts.size() - 1 && encPos >= positions.get(currentBlockID+1)){
					currentBlockID++;
					mFilter.initPosCigarEncryptionMethod(blockSalts.get(currentBlockID));
				}
				long pos = mFilter.decryptPosition(encPos);
				record =  new SECRAMRecord(avroRecord, getReferenceBase(pos));
				mFilter.decryptRecord(record);
			}
			catch(NoSuchElementException | IOException | NoSuchAlgorithmException e){
				close();
				e.printStackTrace();
			}
			return record;
		}
		
		private void close() {
			try {
				reader.close();
			} 
			catch (IOException e) {}
		}
		
		
	}
	
	//to iterate over the file
	public Iterator<SECRAMRecord> iterator() {
		try {
			return new RecordIterator();
		}
		catch(IOException | NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			//if something wrong happens, we just return a dummy iterator
			return new Iterator<SECRAMRecord>() {
				@Override
				public boolean hasNext() { return false; }

				@Override
				public SECRAMRecord next() throws NoSuchElementException { throw new NoSuchElementException(); }
			};
		}
		
	}
	
	//for random access
	public SECRAMRecord get(long position) throws IOException {
		//get the closest marker for this position
		Entry<Long, Long> entry = pos2index.floorEntry(position);
		
		//if no such marker exists, this position isn't in the file
		if (entry == null) return null;
		
		//moves the reader to the marker
		randomAccessReader.sync(entry.getValue());
		
		//iterate to the correct position
		SECRAMRecord record = null;
		try {
			do {
				SecramRecordAvro avroRecord = randomAccessReader.next();
				record = new SECRAMRecord(randomAccessReader.next(), getReferenceBase(avroRecord.getPOS()));
			}
			while(record.getPOS() < position);
		}
		catch(NoSuchElementException ex) {
			return null; //if we reach the end of the file, the position isn't in the file
		}
		
		return record;
	}
	
	public void close() throws IOException {
		randomAccessReader.close();
	}
	
	public char getReferenceBase(long pos) throws ArrayIndexOutOfBoundsException,IOException {
		int refID = (int)(pos>>32);
		if (refID == cachedRefID){
			return (char)cachedRefSequence[(int)pos];
		}
		SAMSequenceRecord seq = mSAMFileHeader.getSequence(refID);
		ReferenceSequence rs = mRsf.getSequence(seq.getSequenceName());

		if (rs == null || rs.length() != seq.getSequenceLength()) {
			System.err.println("Could not find the reference sequence " + seq.getSequenceName() + " in the file");
			throw new IOException("No such sequence in file");
		}
		
		cachedRefID = refID;
		cachedRefSequence = rs.getBases();
		
		return (char)cachedRefSequence[(int)pos];
	}
	
	private static SAMFileHeader convertByteArrayToHeader(byte[] header) throws IOException {
		
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(header));
		
		in.readInt(); //bam magic number		
		
		SAMTextHeaderCodec codec = new SAMTextHeaderCodec();
		codec.setValidationStringency(ValidationStringency.SILENT);
		
		return codec.decode(new BufferedLineReader(in,in.readInt()), null);
	}
}
