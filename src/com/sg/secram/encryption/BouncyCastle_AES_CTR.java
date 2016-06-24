package com.sg.secram.encryption;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.sg.secram.encryption.SECRAMEncryptionMethod;

/**
 * Implementation of AES in CTR mode using bouncy castle lightweight api.
 * 
 * Note: Don't use the same (key, IV) pair to encrypt different messages.
 * 
 * @author zhicong
 */

public class BouncyCastle_AES_CTR implements SECRAMEncryptionMethod<byte[]> {
	SICBlockCipher encryptCipher = null;
	SICBlockCipher decryptCipher = null;

	/** The AES key */
	byte[] key = null;

	/** The initialization vector needed by the CTR mode */
	byte[] IV = null;

	// The default block size
	public static int blockSize = 16;

	public BouncyCastle_AES_CTR() {
		// default 192 bit key
		this("SECRET_1SECRET_2SECRET_3".getBytes());
	}

	/**
	 * This constructor uses zero IV. It is up to the user to make sure that he
	 * never uses the same key "keyBytes" to construct this cipher and then
	 * encrypts different messages.
	 */
	public BouncyCastle_AES_CTR(byte[] keyBytes) {
		// default IV vector with all bytes to 0
		this(keyBytes, new byte[blockSize]);
	}

	public BouncyCastle_AES_CTR(byte[] keyBytes, byte[] iv) {
		// get the key
		key = new byte[keyBytes.length];
		System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);

		// get the IV
		IV = new byte[blockSize];
		System.arraycopy(iv, 0, IV, 0, iv.length);

		initCiphers();
	}

	public byte[] getKey() {
		return key;
	}

	/**
	 * Create the AES block ciphers in CTR mode.
	 */
	private void initCiphers() {

		encryptCipher = new SICBlockCipher(new AESEngine());

		decryptCipher = new SICBlockCipher(new AESEngine());

		// create the IV parameter
		ParametersWithIV parameterIV = new ParametersWithIV(new KeyParameter(
				key), IV);

		encryptCipher.init(true, parameterIV);
		decryptCipher.init(false, parameterIV);
	}

	public void ResetCiphers() {
		if (encryptCipher != null)
			encryptCipher.reset();
		if (decryptCipher != null)
			decryptCipher.reset();
	}

	/**
	 * @param in
	 *            Array of bytes to be encrypted.
	 * @return Encrypted bytes under the stream cipher mode.
	 */
	public byte[] CTREncrypt(byte[] in) {
		// Bytes written to out will be encrypted

		byte[] out = new byte[in.length];

		int noBytesProcessed = encryptCipher.processBytes(in, 0, in.length,
				out, 0);
		assert (noBytesProcessed == in.length);

		return out;
	}

	/**
	 * @param in
	 *            Array of bytes to be decrypted
	 * @return Plaintext bytes.
	 */
	public byte[] CTRDecrypt(byte[] in) {
		// Bytes written to out are plaintext

		byte[] out = new byte[in.length];

		int noBytesProcessed = decryptCipher.processBytes(in, 0, in.length,
				out, 0);
		assert (noBytesProcessed == in.length);

		return out;
	}

	@Override
	public byte[] encrypt(byte[] objectToEncrypt, String key) {
		return CTREncrypt(objectToEncrypt);
	}

	@Override
	public byte[] decrypt(byte[] objectToDecrypt, String key) {
		return CTRDecrypt(objectToDecrypt);
	}
}
