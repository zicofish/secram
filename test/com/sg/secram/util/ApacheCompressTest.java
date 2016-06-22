package com.sg.secram.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

public class ApacheCompressTest {
	public static void main(String[] args) throws IOException{
		int[] intArray = new int[1000];
		for(int i = 0; i < 1000; i++){
			intArray[i] = -300 + i;
		}
		ByteBuffer buf = ByteBuffer.allocate(50000000);
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XZCompressorOutputStream dcos = new XZCompressorOutputStream(baos, 1);
    	for(int i = 0; i < 1000; i++){
    		buf.putInt(intArray[i]);
    	}
    	buf.flip();
    	byte[] bufBytes = new byte[buf.limit()];
    	buf.get(bufBytes);
    	dcos.write(bufBytes, 0, bufBytes.length);
		dcos.close();
		byte[] newBytes = baos.toByteArray();
		System.out.println("Deflate coverage size: " + newBytes.length / 1000.0);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(newBytes);
		XZCompressorInputStream dcis = new XZCompressorInputStream(bais);
		byte[] newBufBytes = new byte[bufBytes.length];
		
		buf.clear();
		int n = -1;
		while( -1 != (n = dcis.read(newBufBytes)))
			buf.put(newBufBytes, 0, n);
		buf.flip();
		for(int i = 0; i < 1000; i++){
			int tmp = buf.getInt();
			if(tmp != intArray[i]){
				System.out.println("Wrong compression at " + i + ": " + tmp + " " +  intArray[i]);
				System.exit(1);
			}
		}
	}
}
