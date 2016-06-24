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

	public ReadHeader() {
	}

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
	 * @return Absolute starting position of the paired read.
	 */
	public long getNextAbsolutePosition() {
		long result = mNextReferenceIndex;
		result <<= 32;
		result |= (0x00000000FFFFFFFFL & mNextPosition);
		return result;
	}

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
