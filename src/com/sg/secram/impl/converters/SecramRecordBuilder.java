package com.sg.secram.impl.converters;

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
import com.sg.secram.impl.records.PosCigar;
import com.sg.secram.impl.records.PosCigarFeature;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;

/*
 * SECRAM record
 */
public class SecramRecordBuilder {

	private int mReferenceIndex = -1;
	private int mPosition = -1;
	private LinkedList<ReadHeader> mReadHeaders = new LinkedList<ReadHeader>();
	private byte[] mQualityScores = null;
	private PosCigar mPosCigar = null;
	
	//set to true to parse the M operator values with the reference sequence (when false, all M will be considered matches)
	private final static boolean USE_REF_SEQUENCE = true;

	private LinkedList<byte[]> mTmpScores = new LinkedList<byte[]>();
	private int mTmpScoreLen = 0;
	
	//the value in the reference sequence at this position (not stored in the file)
	private char mReferenceBase;
	
	public SecramRecordBuilder(int referenceIndex, int position, char referenceBase) throws IOException {
		mReferenceIndex = referenceIndex;
		mPosition = position;
		mPosCigar = new PosCigar(referenceBase);
		mReferenceBase = referenceBase;
	}
	
	public char getRefBase(){
		return mReferenceBase;
	}
	
	public int getCoverage(){
		return mPosCigar.mCoverage;
	}
	
	public void addFeaturesToNextRead(List<PosCigarFeature> features){
		mPosCigar.mCoverage++;
		if(features.size() > 0)
			mPosCigar.setNonMatchFeaturesForRead(mPosCigar.mCoverage - 1, features);
	}
	
	public void addReadHeader(BAMRecord bamRecord) {
		ReadHeader header = new ReadHeader(bamRecord);
		mReadHeaders.add(header);
	}
	public void updateScores(byte[] score, int offset, int len) {
		if (len > 0) {
			byte[] tmpArray = new byte[len];
			System.arraycopy(score, offset, tmpArray, 0, len);
			mTmpScores.add(tmpArray);
			mTmpScoreLen += len;
		}
	}
	public SecramRecord close() throws IOException {
		
		mQualityScores = new byte[mTmpScoreLen];
		int offset=0;
		for (byte[] array: mTmpScores) {
			System.arraycopy(array, 0, mQualityScores, offset, array.length);
			offset += array.length;
		}
		mTmpScores.clear();
		mTmpScoreLen = 0;
		
		return new SecramRecord(mReferenceIndex,
				mPosition,
				mReferenceBase,
				mReadHeaders,
				mQualityScores,
				mPosCigar);
	}
	
	public long getAbsolutePosition() {
		long result = mReferenceIndex;
		result <<= 32;
		result |= mPosition;
		
		return result;
	}
}
