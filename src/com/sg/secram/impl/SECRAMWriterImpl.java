package com.sg.secram.impl;

import java.util.List;

import com.sg.secram.SECRAMEncryptionFilter;
import com.sg.secram.SECRAMWriter;
import com.sg.secram.header.SECRAMFileHeader;
import com.sg.secram.header.SECRAMReferenceGenome;
import com.sg.secram.records.SECRAMRecordCodec;

public class SECRAMWriterImpl implements SECRAMWriter {

	@Override
	public void addSECRAMHeader(SECRAMFileHeader arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSECRAMRecord(SECRAMRecordCodec arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createSECRAMIndex() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<SECRAMRecordCodec> getSECRAMRecords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEncryptionFilter(SECRAMEncryptionFilter arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReferenceGenome(SECRAMReferenceGenome arg0) {
		// TODO Auto-generated method stub

	}

}
