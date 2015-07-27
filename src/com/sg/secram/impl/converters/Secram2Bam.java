package com.sg.secram.impl.converters;

import htsjdk.samtools.BAMRecord;
import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sg.secram.SECRAMEncryptionFilter;
import com.sg.secram.SECRAMWriter;
import com.sg.secram.converters.SECRAMtoBAMConverter;
import com.sg.secram.header.SECRAMFileHeader;
import com.sg.secram.impl.SECRAMFileReader;
import com.sg.secram.impl.SECRAMIterator;
import com.sg.secram.impl.SECRAMSecurityFilter;
import com.sg.secram.impl.records.PosCigar;
import com.sg.secram.impl.records.PosCigarFeature;
import com.sg.secram.impl.records.PosCigarFeatureOld;
import com.sg.secram.impl.records.PosCigarFeatureIterator;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.ReadHeaderOld;
import com.sg.secram.impl.records.SecramRecord;
import com.sg.secram.impl.records.SecramRecordOld;
import com.sg.secram.records.SECRAMRecordCodec;
import com.sg.secram.util.ReferenceUtils;
import com.sg.secram.util.SECRAMUtils;

/**
 * 
 * @author zhicong
 *
 */
public class Secram2Bam {

	private static Log log = Log.getInstance(Secram2Bam.class);
	
	public SECRAMEncryptionFilter getEncryptionFilter(){
		return mEncryptionFilter;
	}
	
	private SECRAMEncryptionFilter mEncryptionFilter;
	
	private SAMFileHeader mSAMFileHeader;
	
	private ReferenceSequenceFile mRsf;
	
	private Secram2Bam(SAMFileHeader samFileHeader) throws IOException {
		this(samFileHeader, "./data/hs37d5.fa", null);
	}
	
	public Secram2Bam(SAMFileHeader samFileHeader, String referenceInput, SECRAMEncryptionFilter filter) throws IOException {
		this(samFileHeader, ReferenceUtils.findReferenceFile(referenceInput), filter);
	}
	
	private Secram2Bam(SAMFileHeader samFileHeader, ReferenceSequenceFile rsf, SECRAMEncryptionFilter filter) {
		mSAMFileHeader = samFileHeader;
		mRsf = rsf;
		mEncryptionFilter = filter;
	}
	
	public ReferenceSequenceFile getReferenceSequenceFile() {
		return mRsf;
	}
	
	public static void main(String[] args) throws Exception {
		
		String input = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.newencsecram";
		String output  = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415_output.bam";
		
		//small test
//		String input = "./data/chrom11_small_test.secram";
//		String output  = "./data/chrom11_small_test_output.bam";
		
		convertFile(input, output, "SECRET_1SECRET_2SECRET_3".getBytes());
//		convertFile(input, output, null);
	}
	
	/**
	 * Reads the input file in the SECRAM format and saves it to the output file in the BAM format.
	 * @param input The SECRAM file to read from
	 * @param output The new BAM file to create
	 * @throws IOException If an {@link IOException} occurs during the operation
	 */
	public static void convertFile(String input, String output, byte[] masterKey) throws IOException {
		
		SECRAMFileReader reader = new SECRAMFileReader(input, "./data/hs37d5.fa", masterKey);
		SAMFileWriter bamWriter = new SAMFileWriterFactory().makeBAMWriter(reader.getSAMFileHeader(), true, new File(output));
		
		long startTime = System.currentTimeMillis();
		
		Secram2Bam converter = new Secram2Bam(reader.getSAMFileHeader());
		
		LinkedList<BAMRecordBuilder> incompleteReads = new LinkedList<BAMRecordBuilder>();
		try {
			SECRAMIterator secramIterator = reader.getIterator();
			while(secramIterator.hasNext()){
				SecramRecord record = secramIterator.next();
				int oneBasedPosition =  record.mPosition + 1;
				
				if(oneBasedPosition == 62051)
					System.out.println("trap");
				converter.addSECRAMRecordToIncompleteBAMRecords(record, incompleteReads);
				
				//Adds complete reads to the BAM file.
				//Sometimes even if a BAM read is complete, we must wait for any potential read that starts before this read but is longer
				//before writing it to the BAM file. Hence we stop the "while" loop when we see a read whose end is
				//bigger than "oneBasedPosition", even though there could be subsequent reads whose ends are not
				//bigger than "oneBasedPosition".
				while(!incompleteReads.isEmpty() && 
						incompleteReads.getFirst().getAlignmentEnd() <= oneBasedPosition) {
					bamWriter.addAlignment(incompleteReads.removeFirst().close());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
			//there shouldn't be any incomplete records left!
			if (incompleteReads.size() > 0) {
				log.error(incompleteReads.size() + " incomplete read(s)!");
				for (BAMRecordBuilder record: incompleteReads) {
					bamWriter.addAlignment(record.close());
				}
			}
			bamWriter.close();
			long totalTime = System.currentTimeMillis()-startTime;
			System.out.println("Total time elapsed: "+SECRAMUtils.timeString(totalTime));
		}
	}
	
	public List<BAMRecord> completeBAMRecordsWithException(List<BAMRecordBuilder> incompleteReads){
		List<BAMRecord> completeReads = new ArrayList<BAMRecord>();
		for(BAMRecordBuilder record: incompleteReads){
			if(!record.isComplete()){
				throw new IllegalArgumentException("The input secram records can't be used to create *COMPLETE* BAM reads.");
			}
			completeReads.add(record.close());
		}
		return completeReads;
	}
	
	/*
	 * NOTE: when using the following conversion methods, the caller is responsible for supplying the secram
	 * records in a consecutive manner for the BAM reads that he is trying to recover.
	 */
	
	/**
	 * The caller of this method is responsible for supplying secram records that can be used to
	 * create complete BAM records, otherwise no assumptions can be made about the returned BAM records
	 */
	public List<BAMRecord> createBAMRecords(SecramRecord... records){
		List<BAMRecordBuilder> incompleteReads = createIncompleteBAMRecord(records);
		return completeBAMRecordsWithException(incompleteReads);
	}
	
	/**
	 * The caller of this method is responsible for supplying secram records that contain BAM ReadHeaders
	 * for the BAM reads that cover these secram records, otherwise no assumptions can be made about the returned BAM records
	 */
	public List<BAMRecordBuilder> createIncompleteBAMRecord(SecramRecord... records){
		List<BAMRecordBuilder> incompleteReads = new ArrayList<BAMRecordBuilder>();
		for(SecramRecord record : records)
			addSECRAMRecordToIncompleteBAMRecords(record, incompleteReads);
		return incompleteReads;
	}
	
	public void addSECRAMRecordToIncompleteBAMRecords(SecramRecord record, List<BAMRecordBuilder> incompleteReads){
		int refIndex = record.mReferenceIndex;
		int position = record.mPosition;
		int alignmentStart =  position + 1;
		
		for (ReadHeader header : record.mReadHeaders) {
			BAMRecord bamRecord = DefaultSAMRecordFactory.getInstance().createBAMRecord(
					mSAMFileHeader,
					refIndex,
					alignmentStart,
					(short)0,//readNameLen - we set it to 0 for now, otherwise we have to add it to the variableLengthBlock
					(short)header.mMappingQuality,
					0, //indexingBin - it will need to be recomputed at the end, no need to store it now
					0, //cigarLen
					header.mFlags,
					0, //readLen
					header.mNextReferenceIndex, //mateReferenceSequenceIndex
					header.mNextPosition + 1, //mateAlignmentStart
					header.mTemplateLength,
					header.mTags);
			
			bamRecord.getAttribute((short)-1); //forces the computation of the tags from the variable length block

			bamRecord.setReadName(header.mReadName);
			
			int alignmentEnd = alignmentStart + header.mReferenceLength-1;
			
			incompleteReads.add(new BAMRecordBuilder(bamRecord, alignmentStart, alignmentEnd));
		}

		int scoreOffset=0;
		byte[] scores = record.mQualityScores;
		
		int order = 0; 
		
		for (BAMRecordBuilder builder : incompleteReads) {
			if (builder.isComplete()) continue;
			
			List<PosCigarFeature> features = record.mPosCigar.getCompleteFeaturesOfRead(order);
			String readStr="";
			for(PosCigarFeature f : features){
				switch(f.mOP){
					case M:
					case X:
					case F:
					case I:
					case R:
					case S:
						readStr += f.mBases;
						break;
					default:
				}
				builder.addElement(f, alignmentStart);
			}

			int scoreLen = readStr.length();
						
			if (scoreLen > 0) {
				builder.addScores(scores, scoreOffset, scoreLen, alignmentStart);
				builder.addReadElement(readStr, alignmentStart);
				scoreOffset += scoreLen;
			}
			builder.advance();
			order++;
		}
	}
}
