/**
 * Copyright © 2013-2016 Swiss Federal Institute of Technology EPFL and Sophia Genetics SA
 * 
 * All rights reserved
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials provided 
 * with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used 
 * to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * PATENTS NOTICE: Sophia Genetics SA holds worldwide pending patent applications in relation with this 
 * software functionality. For more information and licensing conditions, you should contact Sophia Genetics SA 
 * at info@sophiagenetics.com. 
 */
package com.sg.secram.encryption;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.sg.secram.encryption.SECRAMEncryptionMethod;

/**
 * Implementation of AES in CTR mode using bouncy castle lightweight api.
 * <p>
 * Note: Don't use the same (key, IV) pair to encrypt different messages.
 * 
 * @author zhihuang
 */

public class BouncyCastle_AES_CTR implements SECRAMEncryptionMethod<byte[]> {

	private SICBlockCipher encryptCipher = null;
	private SICBlockCipher decryptCipher = null;

	/** The AES key */
	private byte[] key = null;

	/** The initialization vector needed by the CTR mode */
	private byte[] IV = null;

	/** The default block size is 16 bytes */
	private static final int blockSize = 16;

	/**
	 * This constructor uses zero IV. It is up to the user to make sure that he
	 * never uses the same key "keyBytes" to construct this cipher and then
	 * encrypts different messages.
	 * @param keyBytes
	 * 				AES key.
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

	/**
	 * @return AES key.
	 */
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
