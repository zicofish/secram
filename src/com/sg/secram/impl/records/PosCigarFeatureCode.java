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