package com.sg.secram.impl.records;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PosCigarIterator implements Iterator<PosCigarElement> {
	private Iterator<PosCigarElement> mElements;
	private boolean nextOrderSame = false;
	
	private PosCigarElement nextElement = null;
	
	
	public PosCigarIterator(Iterator<PosCigarElement> elements) {
		mElements = elements;
		fetch();
	}
	
	private void fetch() {
		PosCigarElement tmp = nextElement;
		nextElement = mElements.hasNext()?mElements.next():null;
		
		if (tmp==null || nextElement==null) {
			nextOrderSame = false;
		}
		else {
			nextOrderSame = tmp.getOrder()==nextElement.getOrder();
		}
		
	}
	
	@Override
	public boolean hasNext() {
		return nextElement!=null;
	}

	@Override
	public PosCigarElement next() throws NoSuchElementException {
		if (nextElement == null) {
			throw new NoSuchElementException();
		}
		PosCigarElement r = nextElement;
		fetch();
		return r;
	}
	
	public boolean nextIsSameOrder() {
		return nextOrderSame;
	}

}
