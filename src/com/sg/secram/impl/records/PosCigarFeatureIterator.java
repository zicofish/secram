package com.sg.secram.impl.records;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PosCigarFeatureIterator implements Iterator<PosCigarFeature> {
	private Iterator<PosCigarFeature> mElements;
	private boolean nextOrderSame = false;

	private PosCigarFeature nextElement = null;

	public PosCigarFeatureIterator(Iterator<PosCigarFeature> elements) {
		mElements = elements;
		fetch();
	}

	private void fetch() {
		PosCigarFeature tmp = nextElement;
		nextElement = mElements.hasNext() ? mElements.next() : null;

		if (tmp == null || nextElement == null) {
			nextOrderSame = false;
		} else {
			nextOrderSame = tmp.mOrder == nextElement.mOrder;
		}

	}

	@Override
	public boolean hasNext() {
		return nextElement != null;
	}

	@Override
	public PosCigarFeature next() throws NoSuchElementException {
		if (nextElement == null) {
			throw new NoSuchElementException();
		}
		PosCigarFeature r = nextElement;
		fetch();
		return r;
	}

	public boolean nextIsSameOrder() {
		return nextOrderSame;
	}

}
