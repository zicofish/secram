package com.sg.secram.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import com.sg.secram.structure.SecramBlock;
import com.sg.secram.structure.SecramCompressionHeaderFactory;
import com.sg.secram.structure.SecramContainer;
import com.sg.secram.structure.SecramContainerIO;
import com.sg.secram.util.Timings;

public class SECRAMContainerIterator implements Iterator<SecramContainer> {
	private InputStream inputStream;
	private SECRAMSecurityFilter filter;
	private SecramContainer nextContainer = null;
	private boolean eof = false;
	
	public SECRAMContainerIterator(InputStream inputStream, SECRAMSecurityFilter filter){
		this.inputStream = inputStream;
		this.filter = filter;
	}
	
	private void readNextContainer(){
		try {
			long nanoStart = System.nanoTime();
            nextContainer = SecramContainerIO.readContainer(inputStream);
            Timings.IO += System.nanoTime() - nanoStart;
            		
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        if (null == nextContainer || !filter.isContainerPermitted(nextContainer.absolutePosStart)) 
        	eof = true;
        else{
        	//initialize the block encryption for this container, and decrypt the sensitive block
            try {
				filter.initContainerEM(nextContainer.containerSalt, nextContainer.containerID);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
            SecramBlock sensitiveBlock = nextContainer.external.get(SecramCompressionHeaderFactory.SENSITIVE_FIELD_EXTERNAL_ID);
            long nanoStart = System.nanoTime();
            byte[] orginalBlock = filter.decryptBlock(sensitiveBlock.getRawContent(), nextContainer.containerID);
            Timings.decryption += System.nanoTime() - nanoStart;
            sensitiveBlock.setContent(orginalBlock, orginalBlock);
        }
	}
	
	@Override
	public boolean hasNext() {
		if(eof) return false;
		if(null == nextContainer) readNextContainer();
		return !eof;
	}

	@Override
	public SecramContainer next() {
		if(hasNext()){
			SecramContainer result = nextContainer;
			nextContainer = null;
			return result;
		}
		return null;
	}
	
	public void close(){
		nextContainer = null;
		try{
			inputStream.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
