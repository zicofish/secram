package com.sg.secram.impl;

import java.io.File;
import java.io.IOException;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class CheckSamHeader {
	public static void main(String[] args) throws IOException{
		SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File("./data/miniCaviar_IDT_NEB.runA.NA12878.bwa.chrom1.bam"));
		
		SAMFileHeader samFileHeader = reader.getFileHeader();
		
		System.out.println(samFileHeader.getTextHeader());
		
		int i = 0;
		for(SAMRecord record : reader){
			if( i < 100)
				System.out.println(record.getAlignmentStart());
			i++;
		}
		reader.close();
	}
}
