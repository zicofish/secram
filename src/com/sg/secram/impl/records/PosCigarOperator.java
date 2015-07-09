package com.sg.secram.impl.records;

import java.util.HashMap;


/**
 * Used to represent the operator of one {@link PosCigarElement}.
 * @author Fabien Jolidon
 *
 */
public enum PosCigarOperator {
	X('X', 'M', "Single base substitution", false, true, true),
	I('I', 'I', "Insertion", true, true, false),
	D('D', 'D', "Deletion", false, false, true),
	N('N', 'N', "Skipping region", false, false, true),
	S('S', 'S', "Soft clipping", true, true, false),
	H('H', 'H', "Hard clipping", true, false, false),
	P('P', 'P', "Padding", false, false, false),
	M('M', 'M', "Match", false, true, true); //this is never used when writing a file, it is only used as a convenience to represent a match
	
	
	private PosCigarOperator(char character, char bamCharacter, String fullName, boolean hasLength, boolean hasBases, boolean consumesRef) {
		c = character;
		bamChar = bamCharacter;
		name = fullName;
		len = hasLength;
		bases = hasBases;
		mConsumesRef = consumesRef;
		
	}
	private final char c;
	private final char bamChar;
	private final String name;
	private final boolean len;
	private final boolean bases;
	private final boolean mConsumesRef;
	
	//maintains a map from the character to the corresponding operator
	private static HashMap<Character, PosCigarOperator> c2op = new HashMap<Character, PosCigarOperator>();
	static { 
		for (PosCigarOperator op : PosCigarOperator.values()) {
			c2op.put(op.c,op);
		}
	}
	
	public String getFullName() {
		return name;
	}
	
	public char getCharacter() {
		return c;
	}
	
	public char getBAMCharacter() {
		return bamChar;
	}
	
	public String toString() {
		if (this == M) {
			return "";
		}
		return c + " ("+getFullName()+")";
	}
	public static PosCigarOperator getOperator(char character) {
		return c2op.get(character);
	}
	
	public boolean hasLength() {
		return len;
	}
	public boolean hasBases() {
		return bases;
	}
	public boolean consumesRef() {
		return mConsumesRef;
	}
}