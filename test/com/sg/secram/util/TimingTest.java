package com.sg.secram.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.sg.secram.example.Bam2SecramFile;
import com.sg.secram.example.Secram2BamFile;
import com.sg.secram.impl.SECRAMFileReader;
import com.sg.secram.impl.SECRAMIterator;
import com.sg.secram.impl.records.SecramRecord;

public class TimingTest {
	public static void main(String[] args) throws Exception{
//		simulatedFileTest();
//		realFileTest();
		retrievalTest();
	}
	
	public static void convertionTimingTest() throws Exception {
		String bam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.bam",
				secram = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram",
				newBam = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram.bam",
				ref = "./data/hs37d5.fa";
//		Bam2SecramFile.bam2secram(bam, secram, ref);
		Secram2BamFile.secram2bam(secram, newBam, ref);
	}
	
	public static void retrievalTest() throws IOException {
		String secram = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram";
		String secrai = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram.secrai";
		List<Long> indices = new ArrayList<>();
		BufferedReader indexReader = new BufferedReader(new FileReader(secrai));
		String line = indexReader.readLine();
		while(line != null){
			indices.add(Long.valueOf(line.split("\t")[0]));
			line = indexReader.readLine();
		}
		indexReader.close();
		
		int bases = 0;
		for(int i = 0; i < 100; i++){
			int r = new Random().nextInt(indices.size()-2);
			SECRAMFileReader reader = new SECRAMFileReader(secram, "./data/hs37d5.fa");
			SECRAMIterator secramIterator = reader.query(indices.get(r), indices.get(r+2));
			
			while(secramIterator.hasNext()){
				Optional<SecramRecord> record = Optional.ofNullable(secramIterator.next());
				bases += record.isPresent()? record.get().mPosCigar.mCoverage : 0;
			}
		}
		Timings.decompression /= 100;
		Timings.decryption /= 100;
		Timings.IO /= 100;
		Timings.locateQueryPosition /= 100;
		Timings.printTimings();
		System.out.println("Total bases: " + bases/100);
	}
	
	public static void simulatedFileTest() throws Exception {
		String refFileName = "/Users/zhihuang/work/LCA1/CTI_Project/tools/data/references/NC_008253_14K.fa";
		String[] coverage  = new String[]{"1", "50"};
		String[] error = new String[]{"0.0001", "0.01"};
		String[] breakDowns = new String[]{"transposition", "invTransposition", "compression", "decompression", "encryption", "decryption"};
		long[][] timings = new long[breakDowns.length][coverage.length*error.length];
		BufferedWriter bw = new BufferedWriter(new FileWriter("./data/Timing.txt"));
		
		String tmpSecram = "./data/timingtest.secram";
		for(int i = 0; i < coverage.length; i++)
			for(int j = 0; j < error.length; j++){
				String bam = "/Users/zhihuang/work/LCA1/CTI_Project/tools/data/simulated/paired/simulated_paired_100_" + coverage[i] + "_" + error[j] + ".bam";
				Timings.reset();
				Bam2SecramFile.bam2secram(bam, tmpSecram, refFileName);
				timings[0][i*2+j] = Timings.transposition;
				timings[2][i*2+j] = Timings.compression;
				timings[4][i*2+j] = Timings.encryption;
				Timings.reset();
				Secram2BamFile.secram2bam(tmpSecram, bam, refFileName);
				timings[1][i*2+j] = Timings.invTransposition;
				timings[3][i*2+j] = Timings.decompression;
				timings[5][i*2+j] = Timings.decryption;
			}
		String header = "type\t" + Arrays.stream(coverage)
			.<String>flatMap((x) -> Arrays.stream(error).map((y) -> String.format("cov(%s)|err(%s)", x, y)))
			.collect(Collectors.joining("\t"));
		bw.write(header + "\n");
		for(int i = 0; i < breakDowns.length; i++){
			bw.write(breakDowns[i] + "\t");
			bw.write(Arrays.stream(timings[i])
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\t")));
			bw.write("\n");
		}
		bw.flush();
		bw.close();
	}
}
