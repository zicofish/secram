package com.sg.secram.impl.converters;

import htsjdk.samtools.BAMRecord;
import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.reference.ReferenceSequenceFile;

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
import com.sg.secram.impl.SECRAMSecurityFilter;
import com.sg.secram.impl.records.PosCigarElement;
import com.sg.secram.impl.records.PosCigarIterator;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SECRAMRecord;
import com.sg.secram.records.SECRAMRecordCodec;
import com.sg.secram.util.ReferenceUtils;
import com.sg.secram.util.SECRAMUtils;

/**
 * Receives a SECRAM file as input and creates a BAM file using the genome and encryption filter specified.
 * 
 * @author jesusgarcia
 *
 */
public class SECRAMtoBAMConverterImpl implements SECRAMtoBAMConverter {

	public SECRAMEncryptionFilter getEncryptionFilter(){
		return mEncryptionFilter;
	}
	
	private SECRAMEncryptionFilter mEncryptionFilter;
	
	private SAMFileHeader mSAMFileHeader;
	
	private ReferenceSequenceFile mRsf;
	
	private SECRAMtoBAMConverterImpl(SAMFileHeader samFileHeader) throws IOException {
		this(samFileHeader, "./data/hs37d5.fa", null);
	}
	
	public SECRAMtoBAMConverterImpl(SAMFileHeader samFileHeader, String referenceInput, SECRAMEncryptionFilter filter) throws IOException {
		this(samFileHeader, ReferenceUtils.findReferenceFile(referenceInput), filter);
	}
	
	private SECRAMtoBAMConverterImpl(SAMFileHeader samFileHeader, ReferenceSequenceFile rsf, SECRAMEncryptionFilter filter) {
		mSAMFileHeader = samFileHeader;
		mRsf = rsf;
		mEncryptionFilter = filter;
	}
	
	public ReferenceSequenceFile getReferenceSequenceFile() {
		return mRsf;
	}
	
	private void addUnalignedRecords(byte[] unalignedRecords) {
		//TODO
	}
	
	public static void main(String[] args) throws Exception {
		
//		String input = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415.secram";
//		String output  = "./data/HG00115.chrom11.ILLUMINA.bwa.GBR.exome.20130415_output.bam";
		
		//small test
		String input = "./data/chrom11_small_test.secram";
		String output  = "./data/chrom11_small_test_output.bam";
		
		convertFile(input, output, "SECRET_1SECRET_2SECRET_3".getBytes());
	}
	
	/**
	 * Reads the input file in the SECRAM format and saves it to the output file in the BAM format.
	 * @param input The SECRAM file to read from
	 * @param output The new BAM file to create
	 * @throws IOException If an {@link IOException} occurs during the operation
	 */
	public static void convertFile(String input, String output, byte[] masterKey) throws IOException {
		
		SECRAMFileReader reader = new SECRAMFileReader(input, "./data/hs37d5.fa", new SECRAMSecurityFilter(masterKey));
//		SECRAMFileReader reader = new SECRAMFileReader(input);
		SAMFileWriter bamWriter = new SAMFileWriterFactory().makeBAMWriter(reader.getBAMHeader(), true, new File(output));
		
		long startTime = System.currentTimeMillis();
		
		SECRAMtoBAMConverterImpl converter = new SECRAMtoBAMConverterImpl(reader.getBAMHeader());
		
		converter.addUnalignedRecords(reader.getUnalignedRecords());
		LinkedList<IncompleteBAMRecord> incompleteReads = new LinkedList<IncompleteBAMRecord>();
		try {
			for (SECRAMRecord record : reader) {
				converter.addSECRAMRecordToIncompleteBAMRecords(record, incompleteReads);
				int oneBasedPosition =  record.getPosition() + 1;
				//Adds complete reads to the BAM file.
				//Sometimes even if a BAM read is complete, we must wait for any potential read that starts before this read but is longer
				//before writing it to the BAM file. Hence we stop the "while" loop when we see a read whose end is
				//bigger than "oneBasedPosition", even though there could be subsequent reads whose ends are not
				//bigger than "oneBasedPosition".
				while(!incompleteReads.isEmpty() && 
						incompleteReads.getFirst().getAlignmentEnd() <= oneBasedPosition) {
					bamWriter.addAlignment(incompleteReads.removeFirst().getRecord());
				}
			}
		}
		finally {
			//there shouldn't be any incomplete records left!
			if (incompleteReads.size() > 0) {
				System.err.println("WARNING: " + incompleteReads.size() + " incomplete read(s)!");
				for (IncompleteBAMRecord record: incompleteReads) {
					bamWriter.addAlignment(record.getRecord());
				}
			}
			bamWriter.close();
			long totalTime = System.currentTimeMillis()-startTime;
			System.out.println("Total time elapsed: "+SECRAMUtils.timeString(totalTime));
		}
	}
	
	public List<BAMRecord> completeBAMRecordsWithException(List<IncompleteBAMRecord> incompleteReads){
		List<BAMRecord> completeReads = new ArrayList<BAMRecord>();
		for(IncompleteBAMRecord record: incompleteReads){
			if(!record.isComplete()){
				throw new IllegalArgumentException("The input secram records can't be used to create *COMPLETE* BAM reads.");
			}
			completeReads.add(record.getRecord());
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
	public List<BAMRecord> createBAMRecords(SECRAMRecord... records){
		List<IncompleteBAMRecord> incompleteReads = createIncompleteBAMRecord(records);
		return completeBAMRecordsWithException(incompleteReads);
	}
	
	/**
	 * The caller of this method is responsible for supplying secram records that contain BAM ReadHeaders
	 * for the BAM reads that cover these secram records, otherwise no assumptions can be made about the returned BAM records
	 */
	public List<IncompleteBAMRecord> createIncompleteBAMRecord(SECRAMRecord... records){
		List<IncompleteBAMRecord> incompleteReads = new ArrayList<IncompleteBAMRecord>();
		for(SECRAMRecord record : records)
			addSECRAMRecordToIncompleteBAMRecords(record, incompleteReads);
		return incompleteReads;
	}
	
	public void addSECRAMRecordToIncompleteBAMRecords(SECRAMRecord record, List<IncompleteBAMRecord> incompleteReads){
		int refIndex = (int)(record.getPOS()>>32);
		int position = record.getPosition();
		int alignmentStart =  position + 1;
		
		for (ReadHeader header : record.getReadHeaders()) {
			long nextPOS = header.getNextPOS();
			BAMRecord bamRecord = DefaultSAMRecordFactory.getInstance().createBAMRecord(
					mSAMFileHeader,
					refIndex,
					alignmentStart,
					(short)0,//readNameLen - we set it to 0 for now, otherwise we have to add it to the variableLengthBlock
					(short)header.getMappingQuality(),
					0, //indexingBin - it will need to be recomputed at the end, no need to store it now
					0, //cigarLen
					header.getFlag(),
					0, //readLen
					(int)(nextPOS>>32), //mateReferenceSequenceIndex
					1+(int)(nextPOS&0xffffffffL), //mateAlignmentStart
					header.getTlen(),
					header.getTags());
			
			bamRecord.getAttribute((short)-1); //forces the computation of the tags from the variable length block

			bamRecord.setReadName(header.getReadName());
			
			int alignmentEnd = alignmentStart+header.getRefLen()-1;
			
			incompleteReads.add(new IncompleteBAMRecord(bamRecord, alignmentStart, alignmentEnd));
		}

		int scoreOffset=0;
		byte[] scores = record.getQualityScores();
		
		PosCigarIterator cigars = record.getPosCigar().iterator();
		
		for (IncompleteBAMRecord bamRecord : incompleteReads) {
			if (bamRecord.isComplete()) continue;
			
			String readStr="";
			do {
				PosCigarElement cigar = cigars.next();
				
				switch(cigar.getOperator()) {
				case M:
				case X:
				case I:
				case S:
					readStr += cigar.getBases();
					break;
				default:
				}
				
				bamRecord.addElement(cigar, alignmentStart);
				
			}
			while(cigars.nextIsSameOrder());

			int scoreLen = readStr.length();
						
			if (scoreLen>0) {
				bamRecord.addScores(scores, scoreOffset, scoreLen, alignmentStart);
				bamRecord.addReadElement(readStr, alignmentStart);
				scoreOffset += scoreLen;
			}
			bamRecord.advance();
		}
	}
	
	//The following list of methods come from interface BAMtoSECRAMConverter,
	//but they are not appropriate for the design.

	public List<SAMRecord> convertSECRAMRecordToSAMReads(SECRAMRecordCodec record) {
		
		return null;
		
	}
	
	public List<SECRAMRecordCodec> addSECRAMRecordToSAMReads(SAMRecord record, List<SECRAMRecordCodec> secramRecords) {
		
		return null;
		
	}
	
	public void convert(File inputFile, SECRAMWriter writer) {
		
	}
	
	public SAMFileHeader convertSECRAMHeaderToSAMHeader(SECRAMFileHeader secram) {
		return null;
	}
}
