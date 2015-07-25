package com.sg.secram.impl;

import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;
import com.sg.secram.structure.SecramContainer;
import com.sg.secram.structure.SecramContainerParser;
import com.sg.secram.structure.SecramHeader;

public class SECRAMIterator implements Iterator<SecramRecord>{
	private SecramHeader secramHeader;
	private ReferenceSequenceFile mRsf;
	private List<SecramRecord> secramRecords = null;
	private Iterator<SecramContainer> containerIterator;
	private SecramContainer container;
	private SecramContainerParser parser;
	private SECRAMSecurityFilter filter;
	private Iterator<SecramRecord> iterator = Collections.<SecramRecord>emptyList().iterator();
	
	private byte[] cachedRefSequence = null;
	private int cachedRefID = -1;
	
	public SECRAMIterator(SecramHeader header, InputStream inputStream, ReferenceSequenceFile referenceFile, SECRAMSecurityFilter filter){
		this.secramHeader = header;
		this.mRsf = referenceFile;
		this.filter = filter;
		this.containerIterator = new SECRAMContainerIterator(inputStream, filter);
		this.parser = new SecramContainerParser();
	}
	
	private void nextContainer() throws IllegalArgumentException, IllegalAccessException, IOException{
		if(!containerIterator.hasNext()){
			container = null;
			secramRecords = null;
			return;
		}
		container = containerIterator.next();
		secramRecords = parser.getRecords(container, filter);
		iterator = secramRecords.iterator();
	}

	@Override
	public boolean hasNext() {
		if(!iterator.hasNext()){
			try {
				nextContainer();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return iterator.hasNext();
	}

	@Override
	public SecramRecord next() {
		if(hasNext()){
			SecramRecord record = iterator.next();
			{//decrypt the positions
				long originalPos = filter.decryptPosition(record.getAbsolutePosition());
				record.setAbsolutionPosition(originalPos);
				for(ReadHeader rh : record.mReadHeaders){
					if(rh.getNextAbsolutePosition() == -1004116979)
						System.out.println("trap");
					originalPos = filter.decryptPosition(rh.getNextAbsolutePosition());
					rh.setNextAbsolutionPosition(originalPos);
				}
			}
			try {
				record.setReferenceBase(getReferenceBase(record.getAbsolutePosition()));
			} catch (Exception e) {
				throw new RuntimeException("Error while getting reference base for position: " + record.getAbsolutePosition());
			}
			return record;
		}
		throw new NoSuchElementException("No more record. Please check with the method hasNext() before calling next().");
	}
	
	public char getReferenceBase(long pos) throws ArrayIndexOutOfBoundsException,IOException {
		int refID = (int)(pos>>32);
		if (refID == cachedRefID){
			return (char)cachedRefSequence[(int)pos];
		}
		SAMSequenceRecord seq = secramHeader.getSamFileHeader().getSequence(refID);
		ReferenceSequence rs = mRsf.getSequence(seq.getSequenceName());
	
		if (rs == null || rs.length() != seq.getSequenceLength()) {
			System.err.println("Could not find the reference sequence " + seq.getSequenceName() + " in the file");
			throw new IOException("No such sequence in file");
		}
		
		cachedRefID = refID;
		cachedRefSequence = rs.getBases();
		
		return (char)cachedRefSequence[(int)pos];
	}
}
