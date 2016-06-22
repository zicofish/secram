package com.sg.secram.encryption;

import com.sg.secram.encryption.SECRAMEncryptionMethod;

public class DummyCipher<T> implements SECRAMEncryptionMethod<T> {

	@Override
	public T encrypt(T objectToEncrypt, String key) {
		// TODO Auto-generated method stub
		return objectToEncrypt;
	}

	@Override
	public T decrypt(T objectToDecrypt, String key) {
		// TODO Auto-generated method stub
		return objectToDecrypt;
	}

}
