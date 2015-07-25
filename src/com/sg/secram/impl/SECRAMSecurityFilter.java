package com.sg.secram.impl;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.sg.secram.SECRAMEncryptionMethod;
import com.sg.secram.avro.SecramRecordAvro;
import com.sg.secram.encryption.SECRAMEncryptionFactory;
import com.sg.secram.impl.records.SecramRecordOld;
import com.sg.secram.util.SECRAMUtils;

public class SECRAMSecurityFilter {
	
	private byte[] masterKey = null;
	
	public static int BLOCK_CIPHER_KEY_LEN = 24;
	private Map<Integer, SECRAMEncryptionMethod<byte[]> > containerEMs = new HashMap<Integer, SECRAMEncryptionMethod<byte[]> >();
	public static int OPE_KEY_LEN = 24;
	private SECRAMEncryptionMethod<Long> positionEM = null;
	
	/*
	 * TODO: We can also initialize this filter with accces control rights, e.g., permitted query range [lower_bound, upper_bound]
	 */
	
	public SECRAMSecurityFilter(byte[] mk){
		if(null == mk)
			return; //create a dummy filter without encryption
		
		masterKey = new byte[mk.length];
		System.arraycopy(mk, 0, masterKey, 0, mk.length);
	}
	
	public void initContainerEM(long salt, int containerID) throws NoSuchAlgorithmException{
		if(null == masterKey)
			containerEMs.put(containerID, SECRAMEncryptionFactory.createDummyContainerEM());
		else{
			byte[] newKey = SECRAMEncryptionFactory.deriveKey(masterKey, SECRAMUtils.longToBytes(salt), null, BLOCK_CIPHER_KEY_LEN);
			containerEMs.put(containerID, SECRAMEncryptionFactory.createContainerEM(newKey));
		}
	}
	
	public void initContainerEM(byte[] key, int containerID){
		containerEMs.put(containerID, SECRAMEncryptionFactory.createContainerEM(key));
	}
	
	public void initPositionEM(long salt){
		if(null == masterKey)
			positionEM = SECRAMEncryptionFactory.createDummyPositionEM();
		else{
			byte[] newKey = SECRAMEncryptionFactory.deriveKey(masterKey, SECRAMUtils.longToBytes(salt), null, BLOCK_CIPHER_KEY_LEN);
			positionEM = SECRAMEncryptionFactory.createPositionEM(newKey);
		}
	}
	
	public void initPositionEM(byte[] key){
		positionEM = SECRAMEncryptionFactory.createPositionEM(key);
	}
	
	public byte[] encryptBlock(byte[] block, int containerID){
		SECRAMEncryptionMethod<byte[]> cypher = containerEMs.get(containerID);
		byte[] encBlock = cypher.encrypt(block, null);
		return encBlock;
	}
	
	public byte[] decryptBlock(byte[] encBlock, int containerID){
		SECRAMEncryptionMethod<byte[]> decypher = containerEMs.get(containerID);
		byte[] block = decypher.decrypt(encBlock, null);
		return block;
	}
	
	public long encryptPosition(long pos){
		return positionEM.encrypt(pos, null);
	}
	
	public long decryptPosition(long encPos){
		return positionEM.decrypt(encPos, null);
	}
}
