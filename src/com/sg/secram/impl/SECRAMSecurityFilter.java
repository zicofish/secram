package com.sg.secram.impl;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.sg.secram.SECRAMEncryptionMethod;
import com.sg.secram.avro.SecramRecordAvro;
import com.sg.secram.encryption.SECRAMEncryptionFactory;
import com.sg.secram.impl.records.SECRAMRecord;
import com.sg.secram.util.SECRAMUtils;

public class SECRAMSecurityFilter {
	
	private byte[] masterKey = null;
	public static int BLOCK_CIPHER_KEY_LEN = 24;
	private SECRAMEncryptionMethod<byte[]> posCigarEM = null;
	public static int OPE_KEY_LEN = 24;
	private SECRAMEncryptionMethod<Long> positionEM = null;
	
	/**
	 * Dummy filter. No encryption.
	 */
	public SECRAMSecurityFilter(){
		posCigarEM = SECRAMEncryptionFactory.createDummyPosCigarEM();
		positionEM = SECRAMEncryptionFactory.createDummyPositionEM();
	}
	
	public SECRAMSecurityFilter(byte[] mk){
		masterKey = new byte[mk.length];
		System.arraycopy(mk, 0, masterKey, 0, mk.length);
		posCigarEM = SECRAMEncryptionFactory.createDummyPosCigarEM();
		positionEM = SECRAMEncryptionFactory.createDummyPositionEM();
	}
	
	public void initPosCigarEncryptionMethod(long salt) throws NoSuchAlgorithmException{
		if(null == masterKey)
			return;
		byte[] newKey = SECRAMEncryptionFactory.deriveKey(masterKey, SECRAMUtils.longToBytes(salt), null, BLOCK_CIPHER_KEY_LEN);
		posCigarEM = SECRAMEncryptionFactory.createPosCigarEM(newKey);
	}
	
	public void initPosCigarEncryptionMethod(byte[] key){
		posCigarEM = SECRAMEncryptionFactory.createPosCigarEM(key);
	}
	
	public void initPositionEncryptionMethod(long salt){
		if(null == masterKey)
			return;
		byte[] newKey = SECRAMEncryptionFactory.deriveKey(masterKey, SECRAMUtils.longToBytes(salt), null, BLOCK_CIPHER_KEY_LEN);
		positionEM = SECRAMEncryptionFactory.createPositionEM(newKey);
	}
	
	public void initPositionEncryptionMethod(byte[] key){
		positionEM = SECRAMEncryptionFactory.createPositionEM(key);
	}
	
	public void encryptRecord(SECRAMRecord record){
		SecramRecordAvro avroRecord = record.getAvroRecord();
		
		byte[] posCigarBytes = avroRecord.getPosCigar().array();
		Long position = avroRecord.getPOS();
		
		byte[] encPosCigar = posCigarEM.encrypt(posCigarBytes, null);
		Long encPos = positionEM.encrypt(position, null);
		
		avroRecord.setPOS(encPos);
//		avroRecord.setPOS(0L);
//		avroRecord.setPosCigar(ByteBuffer.wrap(encPosCigar));
		avroRecord.setPosCigar(ByteBuffer.wrap(new byte[0]));
	}
	
	public void decryptRecord(SECRAMRecord encRecord){
		SecramRecordAvro avroRecord = encRecord.getAvroRecord();
		
		byte[] encPosCigarBytes = avroRecord.getPosCigar().array();
		Long encPosition = avroRecord.getPOS();
		
		byte[] posCigar = posCigarEM.decrypt(encPosCigarBytes, null);
		Long pos = positionEM.decrypt(encPosition, null);
		
		avroRecord.setPOS(pos);
//		avroRecord.setPOS(0L);
		avroRecord.setPosCigar(ByteBuffer.wrap(posCigar));
	}
	
	public long encryptPosition(long pos){
		return positionEM.encrypt(pos, null);
	}
	
	public long decryptPosition(long encPos){
		return positionEM.decrypt(encPos, null);
	}
}
