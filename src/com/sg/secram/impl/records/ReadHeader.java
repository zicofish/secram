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

import htsjdk.samtools.BAMRecord;

/**
 * Information related to the whole read, but not individual positions,
 * hence it doesn't include read bases and quality scores.
 * @author zhihuang
 *
 */
public class ReadHeader {

	public int mReferenceLength = -1;
	public int mMappingQuality = -1;
	public String mReadName = null;
	public int mFlags = 0;
	public int mTemplateLength = -1;
	public int mNextReferenceIndex = -1;
	public int mNextPosition = -1;
	public byte[] mTags;

	/**
	 * Construct an empty read header.
	 */
	public ReadHeader() {
	}

	/**
	 * Construct a read header by extracting information fro a BAM record.
	 */
	public ReadHeader(BAMRecord bamRecord) {
		mReferenceLength = bamRecord.getCigar().getReferenceLength();
		mMappingQuality = bamRecord.getMappingQuality();
		mReadName = bamRecord.getReadName();
		mFlags = bamRecord.getFlags();
		mTemplateLength = bamRecord.getInferredInsertSize();
		mNextReferenceIndex = bamRecord.getMateReferenceIndex();
		mNextPosition = bamRecord.getMateAlignmentStart() - 1;
		mTags = get_auxiliary(bamRecord);
	}

	/**
	 * @return The tags data after quality scores.
	 */
	private static byte[] get_auxiliary(BAMRecord bamRecord) {
		// Here I assume the BAMRecord has not been modified since we read it
		// from the BAM file
		byte[] variableBinaryData = bamRecord.getVariableBinaryRepresentation();
		int auxiliarySize = bamRecord.getAttributesBinarySize();
		if (null == variableBinaryData || -1 == auxiliarySize) {
			System.err
					.println("The BAMRecord has been modified, hence this returned auxiliary data is unreliable.");
			return null;
		}
		byte[] ret = new byte[auxiliarySize];
		System.arraycopy(variableBinaryData, variableBinaryData.length
				- auxiliarySize, ret, 0, auxiliarySize);
		return ret;
	}

	/**
	 * Get the absolute starting position of the paired read.
	 */
	public long getNextAbsolutePosition() {
		long result = mNextReferenceIndex;
		result <<= 32;
		result |= (0x00000000FFFFFFFFL & mNextPosition);
		return result;
	}

	/**
	 * Set the absolute starting position of the paired read.
	 */
	public void setNextAbsolutionPosition(long nap) {
		mNextReferenceIndex = (int) (nap >> 32);
		mNextPosition = (int) nap;
	}

	@Override
	public String toString() {
		String result = "";
		result += "RefLen: " + mReferenceLength + "\n";
		result += "Mapping quality: " + mMappingQuality + "\n";
		result += "Read name: " + mReadName + "\n";
		result += "Flag: " + mFlags + "\n";
		result += "Template Length: " + mTemplateLength + "\n";
		result += "Next reference index: " + mNextReferenceIndex + "\n";
		result += "Next position: " + mNextPosition + "\n";
		result += "Auxiliary Data: \""
				+ new String(mTags).replace("\n", "\\n").replace("\0", "\\0")
				+ "\"";

		return result;
	}
}
