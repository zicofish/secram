package com.sg.secram.impl.records;

import java.util.LinkedList;
import java.util.List;

/**
 * SECRAM record on a position.
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
	 * The value in the reference sequence at this position (not stored in the
	 * file)
	 */
	private char mReferenceBase = '*';

	public SecramRecord() {
		mQualityScores = new byte[0];
		mPosCigar = new PosCigar('*');
	}

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

	public long getAbsolutePosition() {
		long result = mReferenceIndex;
		result <<= 32;
		result |= (0x00000000FFFFFFFFL & mPosition);

		return result;
	}

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
