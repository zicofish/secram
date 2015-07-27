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
public class SecramRecord {

	public int mReferenceIndex = -1;
	public int mPosition = -1;
	public List<ReadHeader> mReadHeaders = new LinkedList<ReadHeader>();
	public byte[] mQualityScores = null;
	public PosCigar mPosCigar = null;
	
	public long absolutePositionDelta;
	public int coverageDelta;
	public int qualityLenDelta;
	
	//the value in the reference sequence at this position (not stored in the file)
	private char mReferenceBase = '*';
	
	public SecramRecord(){
		mQualityScores = new byte[0];
		mPosCigar = new PosCigar('*');
	}
	
	public SecramRecord(int referenceIndex, int position, char referenceBase, 
			List<ReadHeader> readHeaders, byte[] qualityScores, PosCigar posCigar){
		mReferenceIndex = referenceIndex;
		mPosition = position;
		mReadHeaders = readHeaders;
		mQualityScores = qualityScores;
		mPosCigar = posCigar;
		mReferenceBase = referenceBase;
	}
	
	
	public SecramRecord(int referenceIndex, int position, char referenceBase) throws IOException {
		mReferenceIndex = referenceIndex;
		mPosition = position;
		mPosCigar = new PosCigar(referenceBase);
		mReferenceBase = referenceBase;
	}
	
	public long getAbsolutePosition() {
		long result = mReferenceIndex;
		result <<= 32;
		result |= (0x00000000FFFFFFFFL & mPosition);
		
		return result;
	}
	
	public void setAbsolutionPosition(long ap){
		mReferenceIndex = (int) (ap >> 32);
		mPosition = (int) ap;
	}
	
	public char getReferenceBase(){
		return mReferenceBase;
	}
	
	public void setReferenceBase(char base){
		mReferenceBase = base;
		mPosCigar.setReferenceBase(base);
	}
	
	public String toString() {
		String result="";
		
		result+="Reference: "+ mReferenceIndex +"\n";
		result+="Position: "+ mPosition +"\n";
		result+="# read headers: "+ mReadHeaders.size() +"\n";
		
		for (ReadHeader header : mReadHeaders) {
			result += "   "+header.toString().replace("\n", "\n   ")+"\n";
			result+="   ----------------------------------------\n";
		}
		result+="Pos Cigar: \""+ mPosCigar +"\"\n";
		result+="Quality Scores: [ ";
		
		String tmp = null;
		for (byte b: mQualityScores) {
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
	
//	public boolean equals(Object obj) {
//		if (obj instanceof SecramRecord) {
//			SecramRecord that = (SecramRecord)obj;
//			
//			if (mAvroRecord.getReadHeaders().size()==0)
//				return that.mAvroRecord.equals(mAvroRecord);
//			
//			if (getPOS() != that.getPOS()) return false;
//			if (!mAvroRecord.getPosCigar().equals(that.mAvroRecord.getPosCigar())) return false;
//			if (!mAvroRecord.getQual().equals(that.mAvroRecord.getQual())) return false;
//			if (mAvroRecord.getReadHeaders().size() != that.mAvroRecord.getReadHeaders().size()) return false;
//			
//			Iterator<ReadHeader> iter = that.getReadHeaders().iterator();
//			
//			for (ReadHeader header : getReadHeaders()) {
//				ReadHeader thatHeader = iter.next();
//
//				if (header.getHLen() != thatHeader.getHLen()) return false;
//				if (header.getOrder() != thatHeader.getOrder()) return false;
//				if (header.getRefLen() != thatHeader.getRefLen()) return false;
//				if (header.getNextPOS() != thatHeader.getNextPOS()) return false;
//				if (header.getBinMqNl() != thatHeader.getBinMqNl()) return false;
//				if (!header.getReadName().equals(thatHeader.getReadName())) return false;
//				if (header.getFlag() != thatHeader.getFlag()) return false;
//				if (header.getBinMqNl() != thatHeader.getBinMqNl()) return false;
//
//				if (header.getTlen() != thatHeader.getTlen()) return false;
//				
//				//the tags are not always in the same order in the binary representation, so check length for now....
//				if (header.getTags().length != thatHeader.getTags().length) return false;
//				
//			}
//			
//			return true;
//		}
//		return false;
//	}
	
}
