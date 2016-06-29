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
