package com.sg.secram.example;

import java.io.File;
import java.io.IOException;

import com.sg.secram.impl.converters.Secram2Bam;
import com.sg.secram.util.SECRAMUtils;

public class Secram2BamFile {
	public static void main(String[] args) throws Exception{
//		String bam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.bam",
//				secram = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram",
//				newBam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram.bam",
//				ref = "./data/hs37d5.fa";
		String ref = args[0],
				secram = args[1],
				bam = args[2];
		secram2bam(secram, bam, ref);
	}
	
	public static void secram2bam(String inName, String outName, String refName) throws IOException{
		File input = new File(inName);
		File output = new File(outName);
		System.out.println("Start processsing file  \""+ input +"\"");
		long startTime = System.currentTimeMillis();
		
		Secram2Bam.convertFile(input, output, refName);
		
		long totalTime = System.currentTimeMillis()-startTime;
		
		System.out.println("Processing of file \"" + input + "\" complete.");
		System.out.println("Total time elapsed: "+SECRAMUtils.timeString(totalTime));
	}
}
