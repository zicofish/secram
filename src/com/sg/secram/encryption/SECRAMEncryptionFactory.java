package com.sg.secram.encryption;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.sg.secram.SECRAMEncryptionMethod;

public class SECRAMEncryptionFactory {
	public static SECRAMEncryptionMethod<byte[]> createPosCigarEM(byte[] key){
		return new BouncyCastle_AES_CTR(key);
	}
	
	public static SECRAMEncryptionMethod<Long> createPositionEM(byte[] key){
		return new OPE(key);
	}
	
	public static SECRAMEncryptionMethod<Long> createDummyPositionEM(){
		return new DummyCipher<Long>();
	}
	
	public static SECRAMEncryptionMethod<byte[]> createDummyPosCigarEM(){
		return new DummyCipher<byte[]>();
	}
	
	public static byte[] deriveKey(byte[] ikm, byte[] salt, byte[] info, int outKeyLen){
		HKDFBytesGenerator kdf = new HKDFBytesGenerator(new SHA1Digest());
		kdf.init(new HKDFParameters(ikm, salt, info));
		byte[] outKey = new byte[outKeyLen];
		kdf.generateBytes(outKey, 0, outKeyLen);
		return outKey;
	}
}
