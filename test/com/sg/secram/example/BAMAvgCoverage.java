package com.sg.secram.example;

import java.io.File;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class BAMAvgCoverage {
	public static void main(String[] args) {
		String bam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.bam";
		SamReader reader = SamReaderFactory.makeDefault()
				.validationStringency(ValidationStringency.SILENT)
				.open(new File(bam));
		int length = 0;
		for (final SAMRecord record : reader) {
			if (record.getReadUnmappedFlag())
				continue;
			length += record.getReadLength();
		}

		ReferenceSequenceFile rsf = ReferenceSequenceFileFactory
				.getReferenceSequenceFile(new File("data/hs37d5.fa"));
		ReferenceSequence rs = rsf.getSequence("11");

		System.out.println(length * 1.0 / rs.length());
	}
}
