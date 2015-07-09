package com.sg.secram.impl.records;

import htsjdk.samtools.BAMRecord;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.sg.secram.SECRAMEncryptionFilter;
import com.sg.secram.avro.ReadHeaderAvro;
import com.sg.secram.avro.SecramRecordAvro;

/*
 * SECRAM record
 */
public class SECRAMRecord {

	//set to true to parse the M operator values with the reference sequence (when false, all M will be considered matches)
	private final static boolean USE_REF_SEQUENCE = true;
	
	//these fields are only used during conversion from BAM and should not be used when reading from a SECRAM file
	private LinkedList<ReadHeader> mReadHeaders = new LinkedList<ReadHeader>();
	private LinkedList<byte[]> mTmpScores = new LinkedList<byte[]>();
	private int mTmpScoreLen = 0;
	
	//the value in the reference sequence at this position (not stored in the file)
	private char mReferenceBase;
	
	private PosCigar mPosCigar = null;
	
	//when this is set to true, it means we are currently converting data from a bam file,
	//and the data in this record is corrently incomplete (because we haven't read all the read
	//at this position yet)
	private boolean reading = false;
	
	//the underlying avro object containing the data of this secram record
	private SecramRecordAvro mAvroRecord;
	
	
	//when converting from BAM
	public SECRAMRecord(long pos, char referenceBase) throws IOException {
		mPosCigar = new PosCigar();
		mAvroRecord = new SecramRecordAvro();
		reading = true;
		
		mAvroRecord.setPOS(pos);
		
		mReferenceBase = referenceBase;
	}
	
//	//when reading a SECRAM file
//	public SECRAMRecord(SecramRecordAvro avroRecord) throws IOException{
//		mAvroRecord = avroRecord;
//		mReference = file.getReferencePosition(getPOS());
//	}
//	public SECRAMRecord(SECRAMFileReader file, SecramRecordAvro avroRecord) throws IOException{
//		mAvroRecord = avroRecord;
//
////		mFile = file;
//		mReference = file.getReferencePosition(getPOS());
//	}
	
	public SECRAMRecord(SecramRecordAvro avroRecord, char referenceBase) throws IOException{
		mAvroRecord = avroRecord;
		mReferenceBase = referenceBase;
	}

	public void addPosCigarElement(char op, int length, String bases, boolean specialEnding, boolean incrementOrderBefore, boolean incrementOrderAfter) {
		//System.out.println("adding PosCigarElement to position "+getPosition()+": "+op+length+bases);
		if (!reading) return;
		
		if (USE_REF_SEQUENCE && op == 'M' && bases.charAt(0) != mReferenceBase) {
			//if the base in the read is different, then it is not a match
			op = 'X';
		}
		
		mPosCigar.addElement(op,length,bases,specialEnding,incrementOrderBefore,incrementOrderAfter);
	}
	public void addReadHeader(BAMRecord bamRecord) {
		if (!reading) return;
		
		ReadHeader header = new ReadHeader(bamRecord, mReadHeaders.size());
		mReadHeaders.add(header);
	}
	public void updateScores(byte[] score, int offset, int len) {
		if (!reading) return;
		
		if (len > 0) {
			byte[] tmpArray = new byte[len];
			System.arraycopy(score, offset, tmpArray, 0, len);
			mTmpScores.add(tmpArray);
			mTmpScoreLen += len;
		}
	}
	public void close() throws IOException {
		if (!reading) return;
		
		
		reading = false;
		
		byte[] qualityScores = new byte[mTmpScoreLen];
		int offset=0;
		for (byte[] array: mTmpScores) {
			System.arraycopy(array, 0, qualityScores, offset, array.length);
			offset += array.length;
		}
		mTmpScores.clear();
		mTmpScoreLen = 0;
		
		mAvroRecord.setQual(ByteBuffer.wrap(qualityScores));
//		mAvroRecord.setQual(ByteBuffer.wrap(new byte[0]));
		
		List<ReadHeaderAvro> tmpHeaders = new LinkedList<ReadHeaderAvro>();
		for (ReadHeader header : mReadHeaders) {
			tmpHeaders.add(header.getAvroRecord());
		}
		
		mAvroRecord.setReadHeaders(tmpHeaders);
		mAvroRecord.setPosCigar(ByteBuffer.wrap(mPosCigar.getBytes()));
	}
	
	public int getBlockSize() {
		int size = 8; //pos
		
		size += mAvroRecord.getQual().array().length;
		size += mAvroRecord.getPosCigar().array().length;
		
		for (ReadHeader header : getReadHeaders()) {
			size+=header.getHLen();
		}
		
		return size;
	}
	public long getPOS() {
		return mAvroRecord.getPOS();
	}
	public int getPosition() {
		return (int)(getPOS()&0xffffffffL);
	}
	public int getNbReadHeaders() {
		return mAvroRecord.getReadHeaders().size();
	}
	
	public Iterable<ReadHeader> getReadHeaders() {
		
		return new Iterable<ReadHeader>() {
			
			@Override
			public Iterator<ReadHeader> iterator() {
				return new Iterator<ReadHeader>() {
					private Iterator<ReadHeaderAvro> i = mAvroRecord.getReadHeaders().iterator();
					
					@Override
					public boolean hasNext() {
						return i.hasNext();
					}
					@Override
					public ReadHeader next() throws NoSuchElementException {
						return new ReadHeader(i.next());
					}
					@Override
					public void remove() throws UnsupportedOperationException {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	public PosCigar getPosCigar() {
		if (mPosCigar == null) {
			mPosCigar = new PosCigar(getPosCigarAsBytes(), mAvroRecord.getQual().array().length, mReferenceBase);
		}
		return mPosCigar;
	}
	
	
	public byte[] getPosCigarAsBytes() {
		return mAvroRecord.getPosCigar().array();
	}
	public byte[] getQualityScores() {
		return mAvroRecord.getQual().array();
	}
	
	public SecramRecordAvro getAvroRecord() {
		return mAvroRecord;
	}
	
	public String toString() {
		String result="";
		
		result+="Position: "+getPosition()+"\n";
		result+="POS: "+getPOS()+"\n";
		result+="# read headers: "+getNbReadHeaders()+"\n";
		
		for (ReadHeader header : getReadHeaders()) {
			result += "   "+header.toString().replace("\n", "\n   ")+"\n";
			result+="   ----------------------------------------\n";
		}
		result+="Pos Cigar: \""+new String(getPosCigarAsBytes())+"\"\n";
		result+="Quality Scores: [ ";
		byte[] scores = getQualityScores();
		
		String tmp = null;
		for (byte b: scores) {
			if (tmp == null) {
				tmp = b+"";
			}
			else {
				tmp += ", "+b;
			}
		}
		result += tmp+" ]";
				
		
		
		return result;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof SECRAMRecord) {
			SECRAMRecord that = (SECRAMRecord)obj;
			
			if (mAvroRecord.getReadHeaders().size()==0)
				return that.mAvroRecord.equals(mAvroRecord);
			
			if (getPOS() != that.getPOS()) return false;
			if (!mAvroRecord.getPosCigar().equals(that.mAvroRecord.getPosCigar())) return false;
			if (!mAvroRecord.getQual().equals(that.mAvroRecord.getQual())) return false;
			if (mAvroRecord.getReadHeaders().size() != that.mAvroRecord.getReadHeaders().size()) return false;
			
			Iterator<ReadHeader> iter = that.getReadHeaders().iterator();
			
			for (ReadHeader header : getReadHeaders()) {
				ReadHeader thatHeader = iter.next();

				if (header.getHLen() != thatHeader.getHLen()) return false;
				if (header.getOrder() != thatHeader.getOrder()) return false;
				if (header.getRefLen() != thatHeader.getRefLen()) return false;
				if (header.getNextPOS() != thatHeader.getNextPOS()) return false;
				if (header.getBinMqNl() != thatHeader.getBinMqNl()) return false;
				if (!header.getReadName().equals(thatHeader.getReadName())) return false;
				if (header.getFlag() != thatHeader.getFlag()) return false;
				if (header.getBinMqNl() != thatHeader.getBinMqNl()) return false;

				if (header.getTlen() != thatHeader.getTlen()) return false;
				
				//the tags are not always in the same order in the binary representation, so check length for now....
				if (header.getTags().length != thatHeader.getTags().length) return false;
				
			}
			
			return true;
		}
		return false;
	}
	
}
