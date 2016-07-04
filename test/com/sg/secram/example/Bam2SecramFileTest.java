package com.sg.secram.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.bouncycastle.util.encoders.Base64;

import com.sg.secram.encryption.SECRAMEncryptionFactory;

public class Bam2SecramFileTest {

	public static void main(String[] args) throws IOException {
		String bam = "./data/SG10000001_S1_L001_R1_001.bam", 
				secram = "./data/SG10000001_S1_L001_R1_001.secram", 
				ref = "./data/hs37d5.fa";
		
		/* Generate an encryption key and save it to file for future decryption. */
		String keyFileName = "secram.key";
		byte[] key = SECRAMEncryptionFactory.generateSecret(24);
		BufferedWriter writer = new BufferedWriter(new FileWriter(keyFileName));
		writer.write(new String(Base64.encode(key)));
		writer.close();
		
		/* Do the conversion */
		Bam2SecramFile.bam2secram(bam, secram, ref, key);
	}

}
