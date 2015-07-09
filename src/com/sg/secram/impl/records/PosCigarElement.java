package com.sg.secram.impl.records;




/**
 * Represents a single element of a {@link PosCigar}.
 * @author Fabien Jolidon
 *
 */
public class PosCigarElement {

	private int mOrder;
	private PosCigarOperator mOP;
	private int mLength;
	private String mBases = null;
	private boolean mSpecialEnding = false;
	
	
	
	/**
	 * 
	 * @param order The order of this element.
	 * @param op Operator
	 * @param length Length (this field is only necessary for some operators)
	 * @param bases A string containing one or more bases  (this field is only necessary for some operators)
	 * @param encode Whether or not we should encode this operator in the file (false for almost all M operators)
	 */
	public PosCigarElement(int order, char op, int length, String bases, boolean specialEnding) {
		mOrder = order;
		mOP = PosCigarOperator.getOperator(op);
		mLength = length;
		mBases = bases;
		mSpecialEnding = specialEnding;
	}
	
	
	/**
	 * 
	 * @param order The order of this element.
	 * @param op Operator
	 * @param length Length (this field is only necessary for some operators)
	 * @param bases A string containing one or more bases  (this field is only necessary for some operators)
	 */
	public PosCigarElement(int order, String op, int length, String bases, boolean encode) {
		this(order,op.charAt(0),length,bases,encode);
	}
	
	

	public int getOrder() {
		return mOrder;
	}

	public PosCigarOperator getOperator() {
		return mOP;
	}

	public int getLength() {
		return mLength;
	}

	public String getBases() {
		return mBases;
	}
	
	public String toString() {
		
		
		String result = mOrder+""+mOP.getCharacter();
		
		if (mOP.hasLength()) result+=mLength;
		if (mOP.hasBases() && mOP != PosCigarOperator.M) result+=mBases;
		
		
		return result;
	}
	
	public String getFileRepresentation() {
		if (mOP == PosCigarOperator.M) return "";
		
		String result = mOrder+""+mOP.getCharacter();
		
		if (mOP.hasLength()) result+=mLength;
		if (mOP.hasBases()) result+=mBases;
		
		if (specialEndingFlag()) {
			result+=PosCigar.SPECIAL_ENDING_CHAR;
		}
		
		
		return result;
	}

	//how many bases of the read sequence are covered by this element
	public int getNbRead() {
		switch(mOP) {
		case I:
		case S:
			return mLength;

		//op that change the pos in both sequences
		case M:
		case X:
			return 1;
			
		//H,P,D,N
		default:
			return 0;
		}
	}
	
	//if this element does not consume the reference, and is the last element of a read
	public boolean specialEndingFlag() {
		return mSpecialEnding;
	}
	
	
	public boolean equals(Object o) {
		if (o instanceof PosCigarElement) {
			PosCigarElement that = (PosCigarElement)o;
			if (
					that.mOrder == mOrder && 
					that.mLength == mLength &&
					that.mOP == mOP &&
					that.mBases.equals(mBases)) {
				return true;
			}
		}
		return false;
	}
	
}
