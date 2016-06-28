package com.sg.secram.impl.converters;

import htsjdk.samtools.BAMRecord;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import com.sg.secram.impl.records.PosCigar;
import com.sg.secram.impl.records.PosCigarFeature;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;

/**
 * Build a SECRAM record when traversing reads that cover this position.
 */
public class SecramRecordBuilder {

	private int mReferenceIndex = -1;
	private int mPosition = -1;
	private LinkedList<ReadHeader> mReadHeaders = new LinkedList<ReadHeader>();
	private byte[] mQualityScores = null;
	private PosCigar mPosCigar = null;

	private LinkedList<byte[]> mTmpScores = new LinkedList<byte[]>();
	private int mTmpScoreLen = 0;

	/** The value in the reference sequence at this position */
	private char mReferenceBase;

	/**
	 * Construct a builder with the necessary information: reference index, position, and reference base.
	 * @param referenceIndex 
	 * 				Index of the reference sequence (in the SAM/BAM header).
	 * @param position 
	 * 				The position for which this builder is collecting information.
	 * @param referenceBase
	 * 				The base on the reference sequence.
	 * @throws IOException
	 */
	public SecramRecordBuilder(int referenceIndex, int position,
			char referenceBase) {
		mReferenceIndex = referenceIndex;
		mPosition = position;
		mPosCigar = new PosCigar(referenceBase);
		mReferenceBase = referenceBase;
	}

	public char getRefBase() {
		return mReferenceBase;
	}

	public int getCoverage() {
		return mPosCigar.mCoverage;
	}

	public void addFeaturesToNextRead(List<PosCigarFeature> features) {
		mPosCigar.mCoverage++;
		if (features.size() > 0)
			mPosCigar.setNonMatchFeaturesForRead(mPosCigar.mCoverage - 1,
					features);
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
		int offset = 0;
		for (byte[] array : mTmpScores) {
			System.arraycopy(array, 0, mQualityScores, offset, array.length);
			offset += array.length;
		}
		mTmpScores.clear();
		mTmpScoreLen = 0;

		return new SecramRecord(mReferenceIndex, mPosition, mReferenceBase,
				mReadHeaders, mQualityScores, mPosCigar);
	}

	public long getAbsolutePosition() {
		long result = mReferenceIndex;
		result <<= 32;
		result |= mPosition;

		return result;
	}
}
