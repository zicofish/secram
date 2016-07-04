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
package com.sg.secram.impl;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.sg.secram.encryption.SECRAMEncryptionMethod;
import com.sg.secram.encryption.OPE;
import com.sg.secram.encryption.SECRAMEncryptionFactory;

/**
 * Defines everything related to protection of the data, including encryption / decryption and access control.
 * @author zhihuang
 *
 */
public class SECRAMSecurityFilter {

	private byte[] masterKey = null;

	private Map<Integer, SECRAMEncryptionMethod<byte[]>> containerEMs = new HashMap<>();
	private SECRAMEncryptionMethod<Long> positionEM = null;
	private long[] lastOPEPair = new long[] { -1, -1 };

	/**
	 * Lower bound of access right
	 */
	private long lowerBound = OPE.MIN_PLAINTEXT;
	/**
	 * Upper bound of access right
	 */
	private long upperBound = OPE.MAX_PLAINTEXT;

	public SECRAMSecurityFilter(byte[] masterKey) {
		this.masterKey = masterKey;
	}

	/**
	 * Initialize the encryption for a container with a salt.
	 * @param salt A random salt used for deriving a block cipher key for the container.
	 * @param containerID ID of the container.
	 * @throws NoSuchAlgorithmException
	 */
	public void initContainerEM(long salt, int containerID)
			throws NoSuchAlgorithmException {
		containerEMs.put(containerID,
				SECRAMEncryptionFactory.createContainerEM(masterKey, salt));
	}

	/**
	 * Initialized the encryption for positions.
	 * @param salt A random salt used for deriving an OPE key
	 */
	public void initPositionEM(long salt) {
		positionEM = SECRAMEncryptionFactory.createPositionEM(masterKey, salt);
		lastOPEPair[0] = lastOPEPair[1] = -1;
	}

	/**
	 * Set the access control bounds.
	 * @param encLowerBound OPE-encrypted lower bound.
	 * @param encUpperBound OPE-encrypted upper bound.
	 */
	public void setBounds(long encLowerBound, long encUpperBound) {
		lowerBound = decryptPosition(encLowerBound);
		upperBound = decryptPosition(encUpperBound);
	}

	/**
	 * Encrypt a block with the block encryption method of the specified container.
	 * @return Encrypted block.
	 */
	public byte[] encryptBlock(byte[] block, int containerID) {
		SECRAMEncryptionMethod<byte[]> cypher = containerEMs.get(containerID);
		byte[] encBlock = cypher.encrypt(block, null);
		return encBlock;
	}

	/**
	 * Decrypt a block with the block encryption method of the specified container.
	 * @return Plaintext block.
	 */
	public byte[] decryptBlock(byte[] encBlock, int containerID) {
		SECRAMEncryptionMethod<byte[]> decypher = containerEMs.get(containerID);
		byte[] block = decypher.decrypt(encBlock, null);
		return block;
	}

	public long encryptPosition(long pos) {
		if (lastOPEPair[0] != pos) {
			lastOPEPair[0] = pos;
			lastOPEPair[1] = positionEM.encrypt(pos, null);
		}
		return lastOPEPair[1];
	}

	public long decryptPosition(long encPos) {
		if (lastOPEPair[1] != encPos) {
			lastOPEPair[0] = positionEM.decrypt(encPos, null);
			lastOPEPair[1] = encPos;
		}
		return lastOPEPair[0];
	}

	/**
	 * Whether this container is after the permitted upper bound.
	 * @param encContainerStart Starting position of the container.
	 */
	public boolean isContainerPermitted(long encContainerStart) {
		return encContainerStart <= encryptPosition(upperBound);
	}

	/**
	 * Whether the SECRAM record is in the permitted range. 
	 * @param encStartPos OPE-encrypted starting position of the container that contains the record.
	 * @param offset The offset of the record relative to the (not encrypted) starting position of the container.
	 */
	public boolean isRecordPermitted(long encStartPos, int offset) {
		long pos = offset + decryptPosition(encStartPos);
		return pos >= lowerBound && pos <= upperBound;
	}
}
