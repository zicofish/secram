package com.sg.secram.example;

import java.io.File;
import java.io.IOException;

import com.sg.secram.impl.converters.Secram2Bam;
import com.sg.secram.util.SECRAMUtils;

public class Secram2BamFile {

	/**
	 * Convert a SECRAM file to a BAM file, by referring to a reference genome file, and using a decryption key.
	 * @param inName
	 * 				SECRAM file name.
	 * @param outName
	 * 				BAM file name.
	 * @param refName
	 * 				Reference file name.
	 * @param key
	 * 				Decryption key.
	 */
	public static void secram2bam(String inName, String outName,
			String refName, byte[] key) {
		File input = new File(inName);
		File output = new File(outName);
		System.out.println("Start processing file  \"" + input + "\"");
		long startTime = System.currentTimeMillis();

		try{
			Secram2Bam.convertFile(input, output, refName, key);
		}
		catch(IOException e){
			e.printStackTrace();
		}

		long totalTime = System.currentTimeMillis() - startTime;

		System.out.println("Processing of file \"" + input + "\" complete.");
		System.out.println("Total time elapsed: "
				+ SECRAMUtils.timeString(totalTime));
	}
}
