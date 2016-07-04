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
package com.sg.secram.impl.records;

import java.util.LinkedList;
import java.util.List;

/**
 * SECRAM record of a position.
 * @author zhihuang 
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

	/**
	 * The base in the reference sequence at this position.
	 */
	private char mReferenceBase = '*';

	/**
	 * Construct an empty SECRAM record.
	 */
	public SecramRecord() {
		mQualityScores = new byte[0];
		mPosCigar = new PosCigar('*');
	}

	/**
	 * @param referenceIndex Reference index.
	 * @param position Relative position on the reference.
	 * @param referenceBase Reference base.
	 * @param readHeaders Headers of the reads that start at this position.
	 * @param qualityScores Quality scores of the bases on this position.
	 * @param posCigar {@link PosCigar} on this position.
	 */
	public SecramRecord(int referenceIndex, int position, char referenceBase,
			List<ReadHeader> readHeaders, byte[] qualityScores,
			PosCigar posCigar) {
		mReferenceIndex = referenceIndex;
		mPosition = position;
		mReadHeaders = readHeaders;
		mQualityScores = qualityScores;
		mPosCigar = posCigar;
		mReferenceBase = referenceBase;
	}

	/**
	 * Get the absolute position of this record. Chromosome ID is in the higher 32 bits,
	 * and chromosome position is in the lower 32 bits.
	 */
	public long getAbsolutePosition() {
		long result = mReferenceIndex;
		result <<= 32;
		result |= (0x00000000FFFFFFFFL & mPosition);

		return result;
	}

	/**
	 * Set the absolute position of this record. Chromosome ID is in the higher 32 bits,
	 * and chromosome position is in the lower 32 bits.
	 */
	public void setAbsolutionPosition(long ap) {
		mReferenceIndex = (int) (ap >> 32);
		mPosition = (int) ap;
	}

	public char getReferenceBase() {
		return mReferenceBase;
	}

	public void setReferenceBase(char base) {
		mReferenceBase = base;
		mPosCigar.setReferenceBase(base);
	}

	/**
	 * @return Number of reads that overlap this position
	 */
	public int getCoverage() {
		return mPosCigar.mCoverage;
	}

	@Override
	public String toString() {
		String result = "";

		result += "Reference: " + mReferenceIndex + "\n";
		result += "Position: " + mPosition + "\n";
		result += "Coverage: " + getCoverage() + "\n";
		result += "Pos Cigar: \"" + mPosCigar + "\"\n";
		result += "Quality Scores: [ ";

		String tmp = null;
		for (byte b : mQualityScores) {
			if (tmp == null) {
				tmp = b + "";
			} else {
				tmp += ", " + b;
			}
		}
		result += tmp + " ]\n";
		result += "# read headers: " + mReadHeaders.size() + "\n";

		for (ReadHeader header : mReadHeaders) {
			result += "   " + header.toString().replace("\n", "\n   ") + "\n";
			result += "   ----------------------------------------\n";
		}

		return result;
	}

}
