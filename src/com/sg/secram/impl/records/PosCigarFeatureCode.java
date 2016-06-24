package com.sg.secram.impl.records;

import htsjdk.samtools.CigarOperator;

import java.util.HashMap;

/**
 * Used to represent the operator of one {@link PosCigarFeature}.
 * 
 * @author zhicong
 *
 */
public enum PosCigarFeatureCode {
	// features that attach before a position
	F('F', 'I', "Insertion before position", true, true, false), // only if there is insertion before the start of a read
	R('R', 'S', "Soft clipping before position", true, true, false), // only if there is soft clipping before the start of a read
	G('G', 'H', "Hard clipping before position", true, false, false), // only if there is hard clipping before the start of a read
	O('O', 'P', "Padding before position", false, false, false), // only if there is padding before the start of a read

	// features on a position
	X('X', 'M', "Single base substitution", false, true, true), 
	D('D', 'D', "Deletion", false, false, true), 
	N('N', 'N', "Skipping position", false, false, true), 
	M('M', 'M', "Match", false, true, true), // this is never used when writing a file, it is only used as a convenience to represent a match

	// features that attach after a position
	I('I', 'I', "Insertion after position", true, true, false), S('S', 'S',
			"Soft clipping after position", true, true, false), H('H', 'H',
			"Hard clipping after position", true, false, false), P('P', 'P',
			"Padding after position", false, false, false);

	private PosCigarFeatureCode(char character, char bamCharacter,
			String fullName, boolean hasLength, boolean hasBases,
			boolean consumesRef) {
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

	// maintains a map from the character to the corresponding operator
	private static HashMap<Character, PosCigarFeatureCode> c2op = new HashMap<Character, PosCigarFeatureCode>();
	static {
		for (PosCigarFeatureCode op : PosCigarFeatureCode.values()) {
			c2op.put(op.c, op);
		}
	}

	public static PosCigarFeatureCode getFeatureCode(CigarOperator op,
			boolean starting, boolean match) {
		switch (op) {
		case I:
			if (starting)
				return F;
			return I;
		case S:
			if (starting)
				return R;
			return S;
		case H:
			if (starting)
				return G;
			return H;
		case P:
			if (starting)
				return O;
			return P;
		case D:
			return D;
		case N:
			return N;
		default:
			if (match)
				return M;
			return X;
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

	@Override
	public String toString() {
		if (this == M) {
			return "";
		}
		return c + " (" + getFullName() + ")";
	}

	public static PosCigarFeatureCode getOperator(char character) {
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