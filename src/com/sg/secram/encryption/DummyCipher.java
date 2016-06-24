package com.sg.secram.encryption;

import com.sg.secram.encryption.SECRAMEncryptionMethod;

/**
 * A cipher that does nothing to plaintext.
 * 
 * @author zhihuang
 */
public class DummyCipher<T> implements SECRAMEncryptionMethod<T> {

	@Override
	public T encrypt(T objectToEncrypt, String key) {
		return objectToEncrypt;
	}

	@Override
	public T decrypt(T objectToDecrypt, String key) {
		return objectToDecrypt;
	}

}
