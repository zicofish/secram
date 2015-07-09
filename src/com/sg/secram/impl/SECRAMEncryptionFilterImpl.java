package com.sg.secram.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.sg.secram.SECRAMEncryptionFilter;
import com.sg.secram.SECRAMEncryptionMethod;
import com.sg.secram.encryption.SECRAMEncryptionFactory;
import com.sg.secram.records.SECRAMPosCigar;

public class SECRAMEncryptionFilterImpl implements SECRAMEncryptionFilter {
	
	
	@Override
	public void setPosCigarEncryptionMethod(
			SECRAMEncryptionMethod<SECRAMPosCigar> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPositionEncryptionMethod(SECRAMEncryptionMethod<Long> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public SECRAMEncryptionMethod<Long> getPositionEncryptionMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SECRAMEncryptionMethod<SECRAMPosCigar> getPosCigarEncryptionMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
