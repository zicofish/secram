package com.sg.secram.encryption;

public interface SECRAMEncryptionMethod<T> {
	public T encrypt(T objectToEncrypt, String key);
	public T decrypt(T objectToDecrypt, String key);
}
