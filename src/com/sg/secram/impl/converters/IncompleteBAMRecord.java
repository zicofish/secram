package com.sg.secram.impl.converters;

import htsjdk.samtools.BAMRecord;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import com.sg.secram.impl.records.PosCigarElement;

public class IncompleteBAMRecord {
	private BAMRecord record;
	private int alignmentEnd;
	private LinkedList<PosCigarElement> cigar = new LinkedList<PosCigarElement>();
	
	private ByteArrayOutputStream qualityScores = new ByteArrayOutputStream(100);
	
	
	private String readString = "";
	
	private int expectedNext = -1;
	
	
	public IncompleteBAMRecord(BAMRecord record,
			int alignmentStart, int alignmentEnd) {
		this.record = record;
		record.setAlignmentStart(alignmentStart);
		this.alignmentEnd = alignmentEnd;
		expectedNext = alignmentStart;
	}
	
	public void addScores(byte[] b, int off, int len, int position) {
		check(position);
		try {
			qualityScores.write(b, off, len);
		}
		catch(IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	public int getExpectedNextPosition(){
		return expectedNext;
	}
	
	public int getAlignmentEnd(){
		return alignmentEnd;
	}
	
	/**
	 * Must call this method after all information of one position has been added to this BAM record.
	 */
	public void advance(){
		expectedNext++;
	}
	
	public boolean isComplete(){
		return expectedNext > alignmentEnd;
	}
	
	public void check(int position){
		if(expectedNext != position){
			throw new IllegalArgumentException("Expect position: " + expectedNext + ", but encounter psotion " + position);
		}
	}
	
	public void addElement(PosCigarElement element, int position) {
		check(position);
		cigar.add(element);
	}
	
	public void addReadElement(String readStr, int position) {
		check(position);
		readString+=readStr;
	}
	
	public BAMRecord getRecord() {
		record.setBaseQualities(qualityScores.toByteArray());
		record.setReadString(readString);
		
		if (cigar.size() == 0) {
			record.setCigarString("*");
		}
		else {
			char lastChar = 'M';
			int currentLen = 0;
			String finalString = "";
			for (PosCigarElement cigarElement: cigar) {
				char currentChar = cigarElement.getOperator().getBAMCharacter();
				if (lastChar != currentChar) {
					if (currentLen > 0) {
						finalString+=currentLen+String.valueOf(lastChar);
						currentLen=0;
					}
					lastChar = cigarElement.getOperator().getBAMCharacter();
					
				}
				
				switch(cigarElement.getOperator()) {
				case I:
				case S:
				case H:
				case P:
					currentLen+=cigarElement.getLength();
					break;
				default:
					++currentLen;
				}
			}
			finalString+=currentLen+String.valueOf(lastChar);
			record.setCigarString(finalString);
		}
		return record;
	}
}
