package com.sg.secram.impl.converters;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.junit.Test;

import com.sg.secram.example.Bam2SecramFile;
import com.sg.secram.example.Secram2BamFile;

public class ConverterTest {
	
	@Test
	public void testCorrectnessOfConversion() throws Exception{
		String bam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.bam",
				secram = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram",
				newBam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram.bam",
				ref = "./data/hs37d5.fa";
		bam2secram(bam, secram, ref);
		secram2bam(secram, newBam, ref);
		testBAMMatch(bam, newBam);
	}
	
	public void bam2secram(String inName, String outName, String refName) throws Exception{
		Bam2SecramFile.bam2secram(inName, outName, refName);
	}
	
	public void secram2bam(String inName, String outName, String refName) throws IOException{
		Secram2BamFile.secram2bam(inName, outName, refName);
	}
	
	public void testBAMMatch(String bam1, String bam2){		
		SamReader file = SamReaderFactory.makeDefault()
				.validationStringency(ValidationStringency.SILENT)
				.open(new File(bam1));
		SamReader file2 = SamReaderFactory.makeDefault()
				.validationStringency(ValidationStringency.SILENT)
				.open(new File(bam2));
		Iterator<SAMRecord> iter = file2.iterator();
		
		
		int i=0;
		int j=0;
		
		boolean start = false;
		
		for (SAMRecord r : file) {
			if(r.getReadUnmappedFlag()) continue;
			SAMRecord r2 = iter.next();
			if (!r.equals(r2)) {
				r.equals(r2);
				System.out.println(r);
				System.out.println("-----------------------------");
				System.out.println(r2);
				System.out.println("-----------------------------");
				System.out.println("-----------------------------");
				start=true;
				
				System.out.println(r.getAlignmentStart()-1);
			}
			if (start && i++ > 100) return;
			++j;
		}
		
		System.out.println(j);
	}
}
