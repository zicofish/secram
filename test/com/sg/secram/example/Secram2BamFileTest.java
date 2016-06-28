package com.sg.secram.example;

import java.io.BufferedReader;
import java.io.FileReader;

import org.bouncycastle.util.encoders.Base64;


public class Secram2BamFileTest {
	public static void main(String[] args) throws Exception {
		String secram = "./data/SG10000001_S1_L001_R1_001.secram", 
				bam = "./data/SG10000001_S1_L001_R1_001.new.bam", 
				ref = "./data/hs37d5.fa";
		/* Load the decryption key. */
		String keyFileName = "secram.key";
		BufferedReader reader = new BufferedReader(new FileReader(
				keyFileName));
		byte[] key = Base64.decode(reader.readLine());
		reader.close();
		
		/* Do the conversion */
		Secram2BamFile.secram2bam(secram, bam, ref, key);
	}
}
