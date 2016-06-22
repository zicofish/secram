package com.sg.secram.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.sg.secram.encryption.SECRAMEncryptionMethod;
import com.sg.secram.util.SECRAMUtils;

public class SECRAMEncryptionFactory {
	public static final int BLOCK_CIPHER_KEY_LEN = 24;
	public static final int OPE_KEY_LEN = 24;

	public static SECRAMEncryptionMethod<byte[]> createContainerEM(
			byte[] masterKey, long salt) {
		if (null == masterKey)
			return new DummyCipher<byte[]>();
		byte[] derivedKey = deriveKey(masterKey, SECRAMUtils.longToBytes(salt),
				null, BLOCK_CIPHER_KEY_LEN);
		return new BouncyCastle_AES_CTR(derivedKey);
	}

	public static SECRAMEncryptionMethod<Long> createPositionEM(
			byte[] masterKey, long salt) {
		if (null == masterKey)
			return new DummyCipher<Long>();
		byte[] derivedKey = deriveKey(masterKey, SECRAMUtils.longToBytes(salt),
				null, OPE_KEY_LEN);
		return new OPE(derivedKey);
	}

	public static byte[] deriveKey(byte[] ikm, byte[] salt, byte[] info,
			int outKeyLen) {
		HKDFBytesGenerator kdf = new HKDFBytesGenerator(new SHA1Digest());
		kdf.init(new HKDFParameters(ikm, salt, info));
		byte[] outKey = new byte[outKeyLen];
		kdf.generateBytes(outKey, 0, outKeyLen);
		return outKey;
	}

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
