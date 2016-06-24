package com.sg.secram.encryption;

public interface SECRAMEncryptionMethod<T> {
	/**
	 * Encrypt "objectToEncrypt" with "key"
	 */
	public T encrypt(T objectToEncrypt, String key);

	/**
	 * Decrypt "objectToDecrypt" with "key"
	 */
	public T decrypt(T objectToDecrypt, String key);
}
