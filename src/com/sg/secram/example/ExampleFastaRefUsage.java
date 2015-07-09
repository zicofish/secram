package com.sg.secram.example;

import java.io.File;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class ExampleFastaRefUsage {
	
	public static void main(String[] args){
		ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(new File("data/hs37d5.fa"));
		SAMSequenceDictionary dict = rsf.getSequenceDictionary();
		ReferenceSequence rs1 = rsf.nextSequence(), rs2 = rsf.nextSequence();
		ReferenceSequence rs = rsf.getSequence("1");  //get reference sequence by sequence name
		System.out.println(rs.getName());
		System.out.println(rs.getContigIndex());
		System.out.println(rs.length());
		System.out.println(rs.getBases().length);
		System.out.println("===============");
		byte[] bases = rs.getBases();
		int count[] = new int[256];
		for(int i = 0; i < bases.length; i++){
			count[bases[i]]++;
		}
		for(int i = 0; i < count.length; i++){
			System.out.println((char)i + " " + count[i]);
		}
		
		
		rs = rsf.getSequence("22");
		System.out.println(rs.getName());
		System.out.println(rs.getContigIndex());
		System.out.println(rs.length());
		System.out.println(rs.getBases().length);
		System.out.println("===============");
		
		rs = rsf.getSequence("X");
		System.out.println(rs.getName());
		System.out.println(rs.getContigIndex());
		System.out.println(rs.length());
		System.out.println(rs.getBases().length);
		System.out.println(rs.getBases()[rs.length()-1]);
		
		rs = rsf.getSubsequenceAt("X", 0, 2);
		System.out.println(rs.getName());
		System.out.println(rs.getContigIndex());
		System.out.println(rs.length());
		System.out.println(rs.getBases().length);
		System.out.println(String.format("%02x", rs.getBases()[0]));
		
		
//		for (int i=0;i<bases.length;++i) {
//			int unsigned = bases[i]&0xff;
//			
//			System.out.println("\""+(char)(unsigned)+"\" ("+unsigned+")");
//		}
	}
}
