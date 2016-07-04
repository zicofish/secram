/**
 * Copyright © 2013-2016 Swiss Federal Institute of Technology EPFL and Sophia Genetics SA
 * 
 * All rights reserved
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials provided 
 * with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used 
 * to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * PATENTS NOTICE: Sophia Genetics SA holds worldwide pending patent applications in relation with this 
 * software functionality. For more information and licensing conditions, you should contact Sophia Genetics SA 
 * at info@sophiagenetics.com. 
 */
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
 * @author zhihuang
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

	/**
	 * @return The number of reads that overlap this position
	 */
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

	/**
	 * Add "len" quality scores from the specified array at "offset" to the SECRAM record.
	 * @param score An array of quality scores.
	 * @param offset The offset to start adding scores. 
	 * @param len Number of scores to be added.
	 */
	public void updateScores(byte[] score, int offset, int len) {
		if (len > 0) {
			byte[] tmpArray = new byte[len];
			System.arraycopy(score, offset, tmpArray, 0, len);
			mTmpScores.add(tmpArray);
			mTmpScoreLen += len;
		}
	}

	/**
	 * Close this builder when all reads that cover this position are processed.
	 */
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
