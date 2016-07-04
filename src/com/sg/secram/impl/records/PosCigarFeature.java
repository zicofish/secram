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

/**
 * Represents a single element in a {@link PosCigar}.
 * 
 * @author zhihuang
 *
 */
public class PosCigarFeature {

	public int mOrder;
	public PosCigarFeatureCode mOP;
	public int mLength = 0;
	public String mBases = "";

	/**
	 * @param order Order that specifies a read on this position.
	 * @param op Operation code.
	 * @param length Number of bases that are related to this feature.
	 * @param bases Bases, if any, that belong to this feature. Some features don't have bases, e.g., deletion.
	 */
	public PosCigarFeature(int order, PosCigarFeatureCode op, int length,
			String bases) {
		mOrder = order;
		mOP = op;
		mLength = length;
		mBases = bases;
	}

	/**
	 * @param order
	 *            Order that specifies a read on this position.
	 * @param op
	 *            Operation code.
	 * @param length
	 *            Number of bases that are related to this feature.
	 * @param bases
	 *            Bases, if any, that belong to this feature. Some features don't have bases, e.g., deletion.
	 */
	public PosCigarFeature(int order, char op, int length, String bases) {
		this(order, PosCigarFeatureCode.getOperator(op), length, bases);
	}

	/**
	 * 
	 * @param order
	 *            Order that specifies a read on this position.
	 * @param op
	 *            Operation code.
	 * @param length
	 *            Number of bases that are related to this feature.
	 * @param bases
	 *            Bases, if any, that belong to this feature. Some features don't have bases, e.g., deletion.
	 */
	public PosCigarFeature(int order, String op, int length, String bases) {
		this(order, op.charAt(0), length, bases);
	}

	@Override
	public String toString() {

		String result = mOrder + "" + mOP.getCharacter();

		if (mOP.hasLength())
			result += mLength;
		if (mOP.hasBases() && mOP != PosCigarFeatureCode.M)
			result += mBases;

		return result;
	}

	/**
	 * Get the number of bases contained in this feature.
	 */
	public int getNbRead() {
		switch (mOP) {
		case I:
		case S:
			return mLength;

			// op that change the pos in both sequences
		case M:
		case X:
			return 1;

			// H,P,D,N
		default:
			return 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PosCigarFeature) {
			PosCigarFeature that = (PosCigarFeature) o;
			if (that.mOrder == mOrder && that.mLength == mLength
					&& that.mOP == mOP && that.mBases.equals(mBases)) {
				return true;
			}
		}
		return false;
	}

}
