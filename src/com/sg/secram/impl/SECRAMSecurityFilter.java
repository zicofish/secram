package com.sg.secram.impl;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.sg.secram.encryption.SECRAMEncryptionMethod;
import com.sg.secram.encryption.OPE;
import com.sg.secram.encryption.SECRAMEncryptionFactory;

public class SECRAMSecurityFilter {

	// private byte[] masterKey = null; //create a dummy filter without
	// encryption
	private byte[] masterKey = "SECRET_1SECRET_2SECRET_3".getBytes();

	private Map<Integer, SECRAMEncryptionMethod<byte[]>> containerEMs = new HashMap<>();
	private SECRAMEncryptionMethod<Long> positionEM = null;
	private long[] lastOPEPair = new long[] { -1, -1 };

	/*
	 * TODO: We can also initialize this filter with accces control rights,
	 * e.g., permitted query range [lower_bound, upper_bound]
	 */
	private long lowerBound = OPE.MIN_PLAINTEXT;
	private long upperBound = OPE.MAX_PLAINTEXT;

	public SECRAMSecurityFilter(byte[] masterKey) {
		this.masterKey = masterKey;
	}

	public void initContainerEM(long salt, int containerID)
			throws NoSuchAlgorithmException {
		containerEMs.put(containerID,
				SECRAMEncryptionFactory.createContainerEM(masterKey, salt));
	}

	public void initPositionEM(long salt) {
		positionEM = SECRAMEncryptionFactory.createPositionEM(masterKey, salt);
		lastOPEPair[0] = lastOPEPair[1] = -1;
	}

	public void setBounds(long encLowerBound, long encUpperBound) {
		lowerBound = decryptPosition(encLowerBound);
		upperBound = decryptPosition(encUpperBound);
	}

	public byte[] encryptBlock(byte[] block, int containerID) {
		SECRAMEncryptionMethod<byte[]> cypher = containerEMs.get(containerID);
		byte[] encBlock = cypher.encrypt(block, null);
		return encBlock;
	}

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

	public boolean isContainerPermitted(long encContainerStart) {
		return encContainerStart <= encryptPosition(upperBound);
	}

	public boolean isRecordPermitted(long encStartPos, int offset) {
		long pos = offset + decryptPosition(encStartPos);
		return pos >= lowerBound && pos <= upperBound;
	}
}
