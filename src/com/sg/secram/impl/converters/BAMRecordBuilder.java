package com.sg.secram.impl.converters;

import htsjdk.samtools.BAMRecord;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import com.sg.secram.impl.records.PosCigarFeature;

/**
 * Builder for constructing a BAM record when collecting information from SECRAM records.
 * @author zhihuang
 *
 */
public class BAMRecordBuilder {
	private BAMRecord record;
	private int alignmentEnd;
	private LinkedList<PosCigarFeature> cigar = new LinkedList<PosCigarFeature>();

	private ByteArrayOutputStream qualityScores = new ByteArrayOutputStream(100);

	private String readString = "";

	private int expectedNext = -1;

	/**
	 * Construct a builder with an incomplete BAM record.
	 * @param record An incomplete BAM record.
	 * @param alignmentStart Alignment start of the record.
	 * @param alignmentEnd Alignment end of the record.
	 */
	public BAMRecordBuilder(BAMRecord record, int alignmentStart,
			int alignmentEnd) {
		this.record = record;
		record.setAlignmentStart(alignmentStart);
		this.alignmentEnd = alignmentEnd;
		expectedNext = alignmentStart;
	}

	/**
	 * Add "len" quality scores from the specified array at "offset" to the BAM record.
	 * @param b An array of quality scores.
	 * @param off The offset to start adding scores. 
	 * @param len Number of scores to be added.
	 * @param position The position which the quality scores belong to.
	 */
	public void addScores(byte[] b, int off, int len, int position) {
		check(position);
		try {
			qualityScores.write(b, off, len);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	public int getExpectedNextPosition() {
		return expectedNext;
	}

	public int getAlignmentEnd() {
		return alignmentEnd;
	}

	/**
	 * Advance the position by 1 because the current position is completed.
	 * Must call this method after all information of one position has been
	 * added to this BAM record.
	 */
	public void advance() {
		expectedNext++;
	}

	/**
	 * Whether all positions of the BAM record are complete.
	 */
	public boolean isComplete() {
		return expectedNext > alignmentEnd;
	}

	/**
	 * Check whether the specified position is the expected one. 
	 */
	private void check(int position) {
		if (expectedNext != position) {
			throw new IllegalArgumentException("Expect position: "
					+ expectedNext + ", but encounter psotion " + position);
		}
	}

	/**
	 * Add a PosCigar feature to the position.
	 */
	public void addElement(PosCigarFeature element, int position) {
		check(position);
		cigar.add(element);
	}

	/**
	 * Add read bases to the position.
	 */
	public void addReadElement(String readStr, int position) {
		check(position);
		readString += readStr;
	}

	/**
	 * Close this builder, and return a complete BAM record.
	 */
	public BAMRecord close() {
		record.setBaseQualities(qualityScores.toByteArray());
		record.setReadString(readString);

		if (cigar.size() == 0) {
			record.setCigarString("*");
		} else {
			char lastChar = 'M';
			int currentLen = 0;
			String finalString = "";
			for (PosCigarFeature feature : cigar) {
				char currentChar = feature.mOP.getBAMCharacter();
				if (lastChar != currentChar) {
					if (currentLen > 0) {
						finalString += currentLen + String.valueOf(lastChar);
						currentLen = 0;
					}
					lastChar = feature.mOP.getBAMCharacter();
				}

				switch (feature.mOP) {
				case F:
				case R:
				case G:
				case O:
				case I:
				case S:
				case H:
				case P:
					currentLen += feature.mLength;
					break;
				default:
					++currentLen;
				}
			}
			finalString += currentLen + String.valueOf(lastChar);
			record.setCigarString(finalString);
		}
		return record;
	}
}
