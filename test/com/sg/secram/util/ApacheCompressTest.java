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
package com.sg.secram.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

public class ApacheCompressTest {
	public static void main(String[] args) throws IOException {
		int[] intArray = new int[1000];
		for (int i = 0; i < 1000; i++) {
			intArray[i] = -300 + i;
		}
		ByteBuffer buf = ByteBuffer.allocate(50000000);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XZCompressorOutputStream dcos = new XZCompressorOutputStream(baos, 1);
		for (int i = 0; i < 1000; i++) {
			buf.putInt(intArray[i]);
		}
		buf.flip();
		byte[] bufBytes = new byte[buf.limit()];
		buf.get(bufBytes);
		dcos.write(bufBytes, 0, bufBytes.length);
		dcos.close();
		byte[] newBytes = baos.toByteArray();
		System.out
				.println("Deflate coverage size: " + newBytes.length / 1000.0);

		ByteArrayInputStream bais = new ByteArrayInputStream(newBytes);
		XZCompressorInputStream dcis = new XZCompressorInputStream(bais);
		byte[] newBufBytes = new byte[bufBytes.length];

		buf.clear();
		int n = -1;
		while (-1 != (n = dcis.read(newBufBytes)))
			buf.put(newBufBytes, 0, n);
		buf.flip();
		for (int i = 0; i < 1000; i++) {
			int tmp = buf.getInt();
			if (tmp != intArray[i]) {
				System.out.println("Wrong compression at " + i + ": " + tmp
						+ " " + intArray[i]);
				System.exit(1);
			}
		}
	}
}
