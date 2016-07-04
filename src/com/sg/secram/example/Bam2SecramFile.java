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
package com.sg.secram.example;

import java.io.File;

import com.sg.secram.impl.converters.Bam2Secram;
import com.sg.secram.util.SECRAMUtils;

/**
 * Example use for converting a BAM file to a SECRAM file.
 * @author zhihuang
 *
 */
public class Bam2SecramFile {
	
	/**
	 * Convert a BAM file to a SECRAM file, by referring to a reference genome file, and using an encryption key.
	 * @param inName
	 * 				BAM file name.
	 * @param outName
	 * 				SECRAM file name.
	 * @param refName
	 * 				Reference file name.
	 * @param key
	 * 				Encryption key.
	 */
	public static void bam2secram(String inName, String outName,
			String refName, byte[] key){
		File input = new File(inName);
		File output = new File(outName);
		System.out.println("Start processing file  \"" + input + "\"");
		long startTime = System.currentTimeMillis();

		try{
			Bam2Secram.convertFile(input, output, refName, key);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		long totalTime = System.currentTimeMillis() - startTime;

		long inputLen = input.length();
		long outputLen = output.length();
		double incr = 100 * ((((double) outputLen) / ((double) inputLen)) - 1);

		System.out.println("Processing of file \"" + input + "\" complete.");
		System.out.println("Total time elapsed: "
				+ SECRAMUtils.timeString(totalTime));
		System.out.println("Input size: " + inputLen);
		System.out.println("Output size: " + outputLen);
		System.out.println("Storage increase: " + incr + "%\n");
	}
}
