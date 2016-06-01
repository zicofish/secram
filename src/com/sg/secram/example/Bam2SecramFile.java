package com.sg.secram.example;

import java.io.File;

import com.sg.secram.impl.converters.Bam2Secram;
import com.sg.secram.util.SECRAMUtils;

public class Bam2SecramFile {
	public static void main(String[] args) throws Exception{
		String bam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.bam",
				secram = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram",
				newBam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram.bam",
				ref = "./data/hs37d5.fa";
//		String ref = args[0], 
//				bam = args[1],
//				secram = args[2];
				
		bam2secram(bam, secram, ref);
	}
	
	public static void bam2secram(String inName, String outName, String refName) throws Exception{
		File input = new File(inName);
		File output = new File(outName);
		System.out.println("Start processsing file  \""+ input +"\"");
		long startTime = System.currentTimeMillis();
		
		Bam2Secram.convertFile(input, output, refName);
		
		long totalTime = System.currentTimeMillis()-startTime;
		
		long inputLen = input.length();
		long outputLen = output.length();
		double incr = 100*((((double)outputLen)/((double)inputLen))-1);
		
		System.out.println("Processing of file \"" + input + "\" complete.");
		System.out.println("Total time elapsed: "+SECRAMUtils.timeString(totalTime));
		System.out.println("Input size: "+inputLen);
		System.out.println("Output size: "+outputLen);
		System.out.println("Storage increase: "+incr+"%\n");
	}
}
