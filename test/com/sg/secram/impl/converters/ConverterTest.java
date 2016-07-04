/**
 * Copyright © 2013-2016 Swiss Federal Institute of Technology EPFL and Sophia Genetics SA
 * 
 * All rights reserved
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials provided 
 * with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used 
 * to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * PATENTS NOTICE: Sophia Genetics SA holds worldwide pending patent applications in relation with this 
 * software functionality. For more information and licensing conditions, you should contact Sophia Genetics SA 
 * at info@sophiagenetics.com. 
 */
package com.sg.secram.impl.converters;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.sg.secram.example.Bam2SecramFile;
import com.sg.secram.example.Secram2BamFile;
import com.sg.secram.structure.SecramContainerIO;

public class ConverterTest {

	public static void main(String[] args) throws Exception {
		String bam = "./data/miniCaviar_IDT_NEB.runA.NA12878.bwa.bam", secram = "./data/miniCaviar_IDT_NEB.runA.NA12878.bwa.secram", newBam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram.bam", ref = "./data/hs37d5.fa";
		byte[] key = "SECRET_1SECRET_2SECRET_3".getBytes();
		bam2secram(bam, secram, ref, key);
		// secram2bam(secram, newBam, ref, key);
		// testBAMMatch(bam, newBam);
	}

	public static void bam2secram(String inName, String outName,
			String refName, byte[] key) throws Exception {
		Bam2SecramFile.bam2secram(inName, outName, refName, key);

		System.out.println("Container header size: "
				+ SecramContainerIO.containerHeaderSize / 1000.0);
		System.out.println("Compression header size: "
				+ SecramContainerIO.compressionHeaderSize / 1000.0);
		System.out.println("Core Block size: "
				+ SecramContainerIO.coreBlockSize / 1000.0);
		System.out.println("Sensitive Field block size: "
				+ SecramContainerIO.externalSizes[0] / 1000.0);
		System.out.println("Sensitive Field length block size: "
				+ SecramContainerIO.externalSizes[1] / 1000.0);
		System.out.println("Quality score block size: "
				+ SecramContainerIO.externalSizes[2] / 1000.0);
		System.out.println("Tags block size: "
				+ SecramContainerIO.externalSizes[3] / 1000.0);
		System.out.println("Absolute position block size: "
				+ SecramContainerIO.externalSizes[4] / 1000.0);
		System.out.println("Read name block size: "
				+ SecramContainerIO.externalSizes[5] / 1000.0);
		System.out.println("Template length block size: "
				+ SecramContainerIO.externalSizes[6] / 1000.0);
		System.out.println("Next absolute position block size: "
				+ SecramContainerIO.externalSizes[7] / 1000.0);
		System.out.println("Coverage size: "
				+ SecramContainerIO.externalSizes[8] / 1000.0);
		System.out.println("Number of read headers size: "
				+ SecramContainerIO.externalSizes[9] / 1000.0);
		System.out.println("Quality score length size: "
				+ SecramContainerIO.externalSizes[10] / 1000.0);
		System.out.println("Number of features size: "
				+ SecramContainerIO.externalSizes[11] / 1000.0);

	}

	public static void secram2bam(String inName, String outName,
			String refName, byte[] key) throws IOException {
		Secram2BamFile.secram2bam(inName, outName, refName, key);
	}

	public static void testBAMMatch(String bam1, String bam2) {
		SamReader file = SamReaderFactory.makeDefault()
				.validationStringency(ValidationStringency.SILENT)
				.open(new File(bam1));
		SamReader file2 = SamReaderFactory.makeDefault()
				.validationStringency(ValidationStringency.SILENT)
				.open(new File(bam2));
		Iterator<SAMRecord> iter = file2.iterator();

		int i = 0;
		int j = 0;

		boolean start = false;

		for (SAMRecord r : file) {
			if (r.getReadUnmappedFlag())
				continue;
			SAMRecord r2 = iter.next();
			if (!r.equals(r2)) {
				r.equals(r2);
				System.out.println(r);
				System.out.println("-----------------------------");
				System.out.println(r2);
				System.out.println("-----------------------------");
				System.out.println("-----------------------------");
				start = true;

				System.out.println(r.getAlignmentStart() - 1);
			}
			if (start && i++ > 100)
				return;
			++j;
		}

		System.out.println(j);
	}
}
