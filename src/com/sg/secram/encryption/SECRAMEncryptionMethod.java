package com.sg.secram.encryption;

/**
 * Interface for encryption methods we use in SECRAM.
 * <p>
 * See also:
 * <ul>
 * <li>{@link BouncyCastle_AES_CTR}</li>
 * <li>{@link OPE}</li>
 * </ul>
 * @author zhihuang
 *
 * @param <T> The data type to be encrypted.
 */
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
