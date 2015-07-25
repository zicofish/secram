package com.sg.secram.encryption;


import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.sg.secram.SECRAMEncryptionMethod;

/**
 * Implementation of AES in CTR mode
 * using bouncy castle lightweight api
 * @author zhicong
 */

public class BouncyCastle_AES_CTR implements SECRAMEncryptionMethod<byte[]>{
    SICBlockCipher encryptCipher = null;
    SICBlockCipher decryptCipher = null;

    // Buffer used to transport the bytes from one stream to another
    byte[] buf = new byte[16];              //input buffer
    byte[] obuf = new byte[512];            //output buffer
    // The key
    byte[] key = null;
    // The initialization vector needed by the CTR mode
    byte[] IV =  null;

    // The default block size
    public static int blockSize = 16;

    public BouncyCastle_AES_CTR(){
        //default 192 bit key
        this("SECRET_1SECRET_2SECRET_3".getBytes());
    }
    public BouncyCastle_AES_CTR(byte[] keyBytes){
        //default IV vector with all bytes to 0
        this(keyBytes, new byte[blockSize]);
    }

    public BouncyCastle_AES_CTR(byte[] keyBytes, byte[] iv){
        //get the key
        key = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0 , key, 0, keyBytes.length);

        //get the IV
        IV = new byte[blockSize];
        System.arraycopy(iv, 0 , IV, 0, iv.length);
        
        initCiphers();
    }
    
    public byte[] getKey(){
    	return key;
    }

    private void initCiphers(){
        //create the ciphers
        // AES block cipher in CTR mode
        encryptCipher = new SICBlockCipher(new AESEngine());

        decryptCipher =  new SICBlockCipher(new AESEngine());

        //create the IV parameter
        ParametersWithIV parameterIV =
                new ParametersWithIV(new KeyParameter(key),IV);

        encryptCipher.init(true, parameterIV);
        decryptCipher.init(false, parameterIV);
    }

    public void ResetCiphers() {
        if(encryptCipher!=null)
            encryptCipher.reset();
        if(decryptCipher!=null)
            decryptCipher.reset();
    }

	public byte[] CTREncrypt(byte[] in){
	    // Bytes written to out will be encrypted
	
		byte[] out = new byte[in.length];
		
		int noBytesProcessed = encryptCipher.processBytes(in, 0, in.length, out, 0);
		assert(noBytesProcessed == in.length);
	
	    return out;
	}
	
    public byte[] CTRDecrypt(byte[] in){
    	// Bytes written to out are plaintext
    	
        byte[] out = new byte[in.length];
        
        int noBytesProcessed = decryptCipher.processBytes(in, 0, in.length, out, 0);
		assert(noBytesProcessed == in.length);
	
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
