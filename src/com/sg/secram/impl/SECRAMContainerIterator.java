package com.sg.secram.impl;

import htsjdk.samtools.cram.io.CountingInputStream;
import htsjdk.samtools.cram.structure.ContainerIO;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sg.secram.structure.SecramBlock;
import com.sg.secram.structure.SecramCompressionHeaderFactory;
import com.sg.secram.structure.SecramContainer;
import com.sg.secram.structure.SecramContainerIO;

public class SECRAMContainerIterator implements Iterator<SecramContainer> {
	private InputStream inputStream;
	private SECRAMSecurityFilter filter;
	private SecramContainer nextContainer = null;
	private boolean eof = false;
	
	public SECRAMContainerIterator(InputStream inputStream, SECRAMSecurityFilter filter){
		this.inputStream = inputStream;
		this.filter = filter;
	}
	
	void readNextContainer(){
		try {
            nextContainer = SecramContainerIO.readContainer(inputStream);
            
            //initialize the block encryption for this container, and decrypt the sensitive block
            filter.initContainerEM(nextContainer.containerSalt, nextContainer.containerID);
            SecramBlock sensitiveBlock = nextContainer.external.get(SecramCompressionHeaderFactory.SENSITIVE_FIELD_EXTERNAL_ID);
            byte[] orginalBlock = filter.decryptBlock(sensitiveBlock.getRawContent(), nextContainer.containerID);
            sensitiveBlock.setContent(orginalBlock, orginalBlock);
            		
        } catch (final IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if (null == nextContainer) eof = true;
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
		throw new NoSuchElementException("No more container. Please check with the method hasNext() before calling next().");
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
