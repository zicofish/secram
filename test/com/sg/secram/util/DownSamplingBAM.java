package com.sg.secram.util;

import java.io.File;
import java.io.IOException;

import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class DownSamplingBAM {
	public static void main(String[] args) throws IOException {
		File bamFile = new File(
				"./data/miniCaviar_IDT_NEB.runA.NA12878.bwa.bam");
		File newBamFile = new File(
				"./data/miniCaviar_IDT_NEB.runA.NA12878.bwa.chrom1.bam");
		SAMFileWriter outputSam = null;
		final SamReader reader = SamReaderFactory.makeDefault()
				.validationStringency(ValidationStringency.SILENT)
				.open(bamFile);

		outputSam = new SAMFileWriterFactory().makeBAMWriter(
				reader.getFileHeader(), true, newBamFile);

		int currentReads = 0;
		for (final SAMRecord samRecord : reader) {
			if (samRecord.getReferenceIndex() == 0)
				writeBam(samRecord, outputSam);

		}

		reader.close();
		outputSam.close();
	}

	public static void writeBam(SAMRecord samRecord, SAMFileWriter outputSam) {
		outputSam.addAlignment(samRecord);
	}
}
