package com.sg.secram.impl;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import htsjdk.samtools.util.StringLineReader;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.file.DataFileReader;

import com.sg.secram.avro.SecramRecordAvro;
import com.sg.secram.impl.records.SECRAMRecord;
import com.sg.secram.util.SECRAMUtils;



public class Test {
	public static void main(String[] args) throws IOException {
		
		//testRegex();
		

		//testRef();
		
		//if (1>0) return;

		
//		String input = "./data/HG00115.chrom20.ILLUMINA.bwa.GBR.low_coverage.20130415.secram";
//		String input2 = "./data/HG00115.chrom20.ILLUMINA.bwa.GBR.low_coverage.20130415_output.secram";

		
		long startTime = System.currentTimeMillis();
		
		//randomAccess(input);
		
//		readFile(input);
		
		//readFile2(input,input2);
		
		//testHeader();
		
		
		testBAMMatch();
		//testSECRAMMatch();
		
//		createSmallBamfromBigBam();
		
		System.out.println("Total time elapsed: "+SECRAMUtils.timeString(System.currentTimeMillis()-startTime));
		
	}
	
	public static void readFile(String input) throws IOException {
		SECRAMFileReader file = new SECRAMFileReader(input);
		
		System.out.println("Reading SECRAM file...");
		
		
		
		
		
		
		int i=0;
		
		for (SECRAMRecord r : file) {
			if (r.getPosCigar().toString().contains("I") || r.getPosCigar().toString().contains("S")) {
				System.out.println(r);
				/*for (PosCigarElement el: r.getPosCigar()) {
					System.out.print(el+" ");
				}*/
				System.out.println("\n-----------------------------------------");
				++i;
				
				if (i>100) break;
			}
			
			
		}
		
		file.close();
	}
	
	public static void readFile2(String input, String input2) throws IOException {
		SECRAMFileReader file = new SECRAMFileReader(input);
		SECRAMFileReader file2 = new SECRAMFileReader(input2);
		
		System.out.println("Reading SECRAM file...");
		
		
		
		
		
		Iterator<SECRAMRecord> iter = file2.iterator();
		
		
		int i=0;
		
		for (SECRAMRecord r : file) {
			SECRAMRecord r2 = iter.next();
			if (r.getPosCigar().toString().contains("I")) {
				System.out.println(r);
				/*for (PosCigarElement el: r.getPosCigar()) {
					System.out.print(el+" ");
				}*/
				System.out.println();
				System.out.println(r2);
				
				
				System.out.println("\n-----------------------------------------");
				++i;
				
				if (i>100) break;
			}
			
			
		}
		
		file.close();
	}
	
	public static void checkBlocks(DataFileReader<SecramRecordAvro> reader) throws IOException {
		long index=115012860;
		SecramRecordAvro record = null;
		while(true) {
			reader.sync(index);
			index = reader.tell();
			record = reader.next();
			SECRAMRecord secramRecord = new SECRAMRecord(record, 'N');
			
			System.out.println("Index="+index+" Position="+secramRecord.getPosition());
			
			index-=100000;
			
		}
	}
	
	
	public static void randomAccess(String input) throws IOException {
		SECRAMFileReader file = new SECRAMFileReader(input);
		
	
		
		
		System.out.println(file.get(81604452657L));
		System.out.println(file.get(81614452657L));
		System.out.println(file.get(81654452657L));
		System.out.println(file.get(81604452658L));
	}
	
	public static void testRegex() {
		String input = "4D";
		
		String regex = "(\\d+)(\\D)(\\d*)(\\D*)\0";
		
		System.out.println("\0001"+"lol");
		
		Pattern p = Pattern.compile(regex);
		
		Matcher m = p.matcher(input);
		
		while(m.find()) {

			for (int i=1;i<=m.groupCount();++i) {
				System.out.println("\""+m.group(i)+"\"");
			}
			System.out.println("----------------");
		}
		

	}
	
	public static void testRef() {
		
		File input = new File("./data/HG00115.chrom20.ILLUMINA.bwa.GBR.low_coverage.20130415.bam");
		SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(input);
		
		
		
		for (SAMSequenceRecord seq : reader.getFileHeader().getSequenceDictionary().getSequences()) {
			System.out.println("id="+seq.getSequenceIndex());
			System.out.println("length="+seq.getSequenceLength());
			System.out.println("name="+seq.getSequenceName());
			for (Entry<String,String> e : seq.getAttributes()) {
				System.out.println(e.getKey()+" => "+e.getValue());
			}
			System.out.println("--------------------------------");
		}
		try {
			ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(new File("./data/hs37d5.fa"));
			
			htsjdk.samtools.reference.ReferenceSequence s;
			
			rsf.getSequence("20").getBases();
			
			
			while ((s = rsf.nextSequence())!=null) {
				System.out.println(s.getContigIndex()+"\t"+s.getName()+"\t"+s.length());
			}
		}
		catch(SAMException ex) {
			System.err.println("lol");
			ex.getCause().printStackTrace();
		}
		
	}
	
	
	public static void testHeader() {
		SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File("./data/HG00115.chrom20.ILLUMINA.bwa.GBR.low_coverage.20130415.bam"));

		
		//System.out.println(reader.getFileHeader().getTextHeader());
		
		SAMFileHeader header = reader.getFileHeader();
		
		
		
		SAMTextHeaderCodec codec = new SAMTextHeaderCodec();
		codec.setValidationStringency(ValidationStringency.SILENT);
		
		
		SAMFileHeader header2 = codec.decode(new StringLineReader(header.getTextHeader()), null);
		
		System.out.println(header2.equals(header));
		
		String[] lines = header.getTextHeader().split("\n");
		
		
		for (String line: lines) {
			if (!line.contains("PG")) {
				System.out.println(line);
			}
		}
		
		
		System.out.println(header.getVersion());
		
		//System.out.println(header.getVersion());
		
		
	
		
		
	}
	
	public static void testBAMMatch() throws IOException {
		
		
//		SamReader file = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File("./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.bam"));
//		SamReader file2 = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File("./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415_output.bam"));
		
		//small test
		SamReader file = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File("./data/chrom11_small_test.bam"));
		SamReader file2 = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File("./data/chrom11_small_test_output.bam"));
		
		Iterator<SAMRecord> iter = file2.iterator();
		
		
		int i=0;
		int j=0;
		
		boolean start = false;
		
		for (SAMRecord r : file) {
			SAMRecord r2 = iter.next();
			if (!r.getReadUnmappedFlag() && !r.equals(r2)) {
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
	public static void testSECRAMMatch() throws IOException {
		
		SECRAMFileReader file = new SECRAMFileReader("./data/HG00115.chrom20.ILLUMINA.bwa.GBR.low_coverage.20130415.secram");
		SECRAMFileReader file2 = new SECRAMFileReader("./data/HG00115.chrom20.ILLUMINA.bwa.GBR.low_coverage.20130415_output.secram");
		
		
		
		
		Iterator<SECRAMRecord> iter = file2.iterator();
		
		
		int i=0;
		
		boolean start = false;
		
		for (SECRAMRecord r : file) {
			SECRAMRecord r2 = iter.next();
			if (!r.equals(r2)) {
				System.out.println(r);
				System.out.println("-----------------------------");
				System.out.println(r2);
				System.out.println("-----------------------------");
				System.out.println("-----------------------------");
				start=true;
			}
			if (start && i++ > 100) return;
		}
		
		
	}
	
	public static void createSmallBamfromBigBam() throws IOException{
		SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File("./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.bam"));
		SAMFileWriter bamWriter = new SAMFileWriterFactory().makeBAMWriter(reader.getFileHeader(), true, new File("./data/chrom11_small_test.bam"));
		int i = 0;
		for (SAMRecord r: reader){
			bamWriter.addAlignment(r);
			if(++i > 1000){
				break;
			}
		}
		bamWriter.close();
		reader.close();
	}
}
