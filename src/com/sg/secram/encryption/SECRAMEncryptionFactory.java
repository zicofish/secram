package com.sg.secram.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.sg.secram.encryption.SECRAMEncryptionMethod;
import com.sg.secram.util.SECRAMUtils;

/**
 * Factory for creating block cipher and OPE cipher, and generating key. 
 * @author zhihuang
 *
 */
public class SECRAMEncryptionFactory {
	public static final int BLOCK_CIPHER_KEY_LEN = 24;
	public static final int OPE_KEY_LEN = 24;

	/**
	 * Derive a key from the master key and the salt, and use the derived key to
	 * construct an AES block cipher.
	 * 
	 * @param masterKey
	 * @param salt
	 * @return An AES block cipher in CTR mode.
	 */
	public static SECRAMEncryptionMethod<byte[]> createContainerEM(
			byte[] masterKey, long salt) {
		if (null == masterKey)
			return new DummyCipher<byte[]>();
		byte[] derivedKey = deriveKey(masterKey, SECRAMUtils.longToBytes(salt),
				null, BLOCK_CIPHER_KEY_LEN);
		return new BouncyCastle_AES_CTR(derivedKey);
	}

	/**
	 * Derive a key from the master key and the salt, and use the derived key to
	 * construct an OPE cipher.
	 * 
	 * @param masterKey
	 * @param salt
	 * @return An OPE cipher.
	 */
	public static SECRAMEncryptionMethod<Long> createPositionEM(
			byte[] masterKey, long salt) {
		if (null == masterKey)
			return new DummyCipher<Long>();
		byte[] derivedKey = deriveKey(masterKey, SECRAMUtils.longToBytes(salt),
				null, OPE_KEY_LEN);
		return new OPE(derivedKey);
	}

	/**
	 * @param ikm
	 *            Master key
	 * @param salt
	 * @param info
	 *            Some extra (public) information.
	 * @param outKeyLen
	 *            The derived key length
	 * @return A derived key.
	 */
	public static byte[] deriveKey(byte[] ikm, byte[] salt, byte[] info,
			int outKeyLen) {
		HKDFBytesGenerator kdf = new HKDFBytesGenerator(new SHA1Digest());
		kdf.init(new HKDFParameters(ikm, salt, info));
		byte[] outKey = new byte[outKeyLen];
		kdf.generateBytes(outKey, 0, outKeyLen);
		return outKey;
	}

	/**
	 * Generate random bytes.
	 * @param keyLen Lenght of random bytes to be generated.
	 */
	public static byte[] generateSecret(int keyLen) {
		try {
			byte[] key = new byte[keyLen];
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.nextBytes(key);
			return key;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}
