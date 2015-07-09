package com.sg.secram.impl.records;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PosCigar implements Iterable<PosCigarElement> {
	private LinkedList<PosCigarElement> elements = new LinkedList<PosCigarElement>();
	
	public final static char SPECIAL_ENDING_CHAR = '\\';

	
	//regex to parse cigar strings
	private final static Pattern CIGAR_PARSER = Pattern.compile("(\\d+)(\\D)(\\d*)(\\D*)\0");
	
	private int mNextOrder = 0;
	
	
	//when writing a file
	public PosCigar() {}
	
	
	//when reading from a SECRAM file
	public PosCigar(byte[] data, int nbRead, char ref) {

		Matcher matcher = CIGAR_PARSER.matcher(new String(data));
		
		String refBases = Character.toString(ref);
		
		
		int baseReadCovered = 0;
		int currentOrder = 0;
		
		
		while(matcher.find()) {

			String op = matcher.group(2);
			
			String lenStr = matcher.group(3);
			int len = lenStr.length()>0?Integer.valueOf(lenStr):0;
			
			String bases = matcher.group(4);
			
			if (op.equals("M")) {
				bases = refBases;
			}
			
			boolean specialEnding = false;
			
			if (bases.endsWith(String.valueOf(SPECIAL_ENDING_CHAR))) {
				specialEnding = true;
				bases = bases.substring(0, bases.length()-1);
			}
			
			
			PosCigarElement current = new PosCigarElement(
					Integer.valueOf(matcher.group(1)), //order
					op, //op
					Integer.valueOf(len), //length
					bases,
					specialEnding);
			
			int diff = current.getOrder()-currentOrder;
			
			for (int i=0;i<diff;++i) {
				PosCigarElement matchElement = new PosCigarElement(currentOrder++, 'M', 1, refBases, false);
				elements.add(matchElement);
				++baseReadCovered;
			}

			//TODO maybe this can be optimized...
			
			//if there is a match at this position, it would not have been added because operators that don't consume
			//the reference usually come before the position, hence we have to add it here
			if (specialEnding) {
				LinkedList<PosCigarElement> tmp = new LinkedList<PosCigarElement>();
				
				tmp.add(current);
				
				boolean looping = true;
				boolean noOp = true;
				
				//dequeue until we find the "main" operator for this position (the one that "consumes" the ref sequence),
				//or reach the start of the position for this read (in which case we have to add a M operator)
				while (!elements.isEmpty() && looping) {
					PosCigarElement tmpCurrent = elements.removeLast();
					
					if (tmpCurrent.getOrder() != current.getOrder()) {
						looping = false;
						elements.add(tmpCurrent);
					}
					else if (tmpCurrent.getOperator().consumesRef()) {
						looping = false;
						elements.add(tmpCurrent);
						noOp=false;
					}
					else {
						tmp.addFirst(tmpCurrent);
					}
				}
				
				if (noOp) {
					elements.add(new PosCigarElement(current.getOrder(), 'M', 1, refBases, false));
					++baseReadCovered;
				}
				
				//puts back the elements in the list
				elements.addAll(tmp);
			}
			else elements.add(current); //bases
			
			baseReadCovered+=current.getNbRead();
			
			if (current.getOperator().consumesRef() || specialEnding) {
				currentOrder = current.getOrder()+1;
			}
			else {
				currentOrder = current.getOrder();
			}
		}
		
		for (;baseReadCovered<nbRead;++baseReadCovered) {
			PosCigarElement matchElement = new PosCigarElement(currentOrder++, 'M', 1, refBases, false);
			elements.add(matchElement);
		}
	}
	
	public void addElement(char op, int length, String bases, boolean specialEnding, boolean incrementOrderBefore, boolean incrementOrderAfter) {
		

		if (incrementOrderBefore) --mNextOrder;
		elements.add(new PosCigarElement(mNextOrder, op, length, bases, specialEnding));
		if (incrementOrderAfter)
			++mNextOrder;
		
	}
	
	public String toString() {
		String result = "";
		
		for (PosCigarElement element : elements) {
			result+=element+" ";
		}
		return result;
	}
	
	public String getFileRepresentation() {
		String result = "";

		PosCigarElement elementBeforeLast=null;
		PosCigarElement lastElement=null;
		
		for (PosCigarElement element : elements) {
			
			//for this very rare case we have to encode the M operator
			if (
					!element.getOperator().consumesRef() &&
					elementBeforeLast != null &&
					!elementBeforeLast.getOperator().consumesRef() &&
					lastElement.getOperator() == PosCigarOperator.M &&
					elementBeforeLast.getOrder() == element.getOrder() &&
					lastElement.getOrder() == element.getOrder()) {
				//System.out.println(this);
				//System.out.println(element.specialEndingFlag());
				result+=lastElement.toString()+'\0';
			}
					
					
			
			result+=element.getFileRepresentation();

			//only appends \0 to actual cigar element (not empty strings that represent matches)
			if (result.length()>0 && !result.endsWith("\0")) {
				result+='\0';
			}
			
			elementBeforeLast = lastElement;
			lastElement = element;
		}
		
		return result;
	}
	
	//returns a value corresponding to the format specification
	public byte[] getBytes() {
		return getFileRepresentation().getBytes();
	}

	@Override
	public PosCigarIterator iterator() {
		return new PosCigarIterator(elements.iterator());
	}
	public boolean equals(Object obj) {
		if (!(obj instanceof PosCigar)) return false;
		return (elements.equals(((PosCigar)obj).elements));
	}
}
