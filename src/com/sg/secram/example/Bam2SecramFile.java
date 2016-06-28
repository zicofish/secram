package com.sg.secram.example;

import java.io.File;

import com.sg.secram.impl.converters.Bam2Secram;
import com.sg.secram.util.SECRAMUtils;

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
