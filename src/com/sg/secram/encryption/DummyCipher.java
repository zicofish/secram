package com.sg.secram.encryption;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sg.secram.encryption.SECRAMEncryptionMethod;

/**
 * A cipher that does nothing to plaintext.
 * 
 * @author zhihuang
 */
public class DummyCipher<T> implements SECRAMEncryptionMethod<T> {
	private static final Logger logger = Logger.getLogger(DummyCipher.class.getName());
	
	@Override
	public T encrypt(T objectToEncrypt, String key) {
		logger.log(Level.WARNING, "You are using a dummy cipher which does not encrypt the message. Please change to a real cipher.");
		return objectToEncrypt;
	}

	@Override
	public T decrypt(T objectToDecrypt, String key) {
		return objectToDecrypt;
	}

}
