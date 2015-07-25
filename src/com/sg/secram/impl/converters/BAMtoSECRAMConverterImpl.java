package com.sg.secram.impl.converters;

import htsjdk.samtools.BAMRecord;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sg.secram.SECRAMEncryptionFilter;
import com.sg.secram.SECRAMWriter;
import com.sg.secram.converters.BAMtoSECRAMConverter;
import com.sg.secram.header.SECRAMFileHeader;
import com.sg.secram.impl.SECRAMFileWriter;
import com.sg.secram.impl.SECRAMSecurityFilter;
import com.sg.secram.impl.records.SecramRecordOld;
import com.sg.secram.records.SECRAMRecordCodec;
import com.sg.secram.util.ReferenceUtils;
import com.sg.secram.util.SECRAMUtils;

/**
 * 
 * @author zhicong
 *
 */
public class BAMtoSECRAMConverterImpl implements BAMtoSECRAMConverter {

	private SECRAMEncryptionFilter mEncryptionFilter;
	
	//TODO handle unaligned reads
	//private int mNbUnalignedRead = 0;
	
	private ReferenceSequenceFile mRsf;
	
	private byte[] cachedRefSequence = null;
	private int cachedRefID = -1;
	
	private SAMFileHeader mSAMFileHeader;

	//For my local test invocation
	public BAMtoSECRAMConverterImpl(SAMFileHeader samFileHeader) throws IOException{
		this(samFileHeader, "./data/hs37d5.fa", null);
	}
	
	public BAMtoSECRAMConverterImpl(SAMFileHeader samFileHeader, String referenceInput, SECRAMEncryptionFilter filter) throws IOException {
		this(samFileHeader, ReferenceUtils.findReferenceFile(referenceInput), filter);
	}
	
	private BAMtoSECRAMConverterImpl(SAMFileHeader samFileHeader, ReferenceSequenceFile rsf, SECRAMEncryptionFilter filter) {
		mSAMFileHeader = samFileHeader;
		mRsf = rsf;
		mEncryptionFilter = filter;
	}
	
	public ReferenceSequenceFile getReferenceSequenceFile() {
		return mRsf;
	}

	public SECRAMEncryptionFilter getEncryptionFilter(){
		return mEncryptionFilter;
	}
	
	/**
	 * Returns the {@link SecramRecordOld} instance corresponding to this position. A new instance is created if the position
	 * is accessed for the first time.
	 * @param position The position we want to access
	 * @param secramRecords The map to search for the position
	 * @return the instance of {@link SecramRecordOld} corresponding to this position
	 * @throws IOException
	 */
	private SecramRecordOld getRecord(long position, Map<Long, SecramRecordOld> secramRecords) throws IOException{
		SecramRecordOld result;
		if (!secramRecords.containsKey(position)) {
			
			result = new SecramRecordOld(position, getReferenceBase(position));
			secramRecords.put(position,result);
		}
		else {
			result = secramRecords.get(position);
		}
		return result;
	}
	
	public char getReferenceBase(long pos) throws ArrayIndexOutOfBoundsException,IOException {
		int refID = (int)(pos>>32);
		if (refID == cachedRefID){
			return (char)cachedRefSequence[(int)pos];
		}
		SAMSequenceRecord seq = mSAMFileHeader.getSequence(refID);
		ReferenceSequence rs = mRsf.getSequence(seq.getSequenceName());

		if (rs == null || rs.length() != seq.getSequenceLength()) {
			System.err.println("Could not find the reference sequence " + seq.getSequenceName() + " in the file");
			throw new IOException("No such sequence in file");
		}
		
		cachedRefID = refID;
		cachedRefSequence = rs.getBases();
		
		return (char)cachedRefSequence[(int)pos];
	}
	
	
	public static void main(String[] args) throws Exception {
		int compressionLevel = 9;
		int positionsPerBlock = 10000;
		
		File inputFolder = new File("./data");
		String[] files = inputFolder.list();

		
		PrintStream log = new PrintStream("./data/log.txt");
		
		AtomicInteger runningThreads = new AtomicInteger(1);
		
		for (final String f : files) {
			if (f.endsWith(".bam")) {
				File input = new File(inputFolder, f);
				File output = new File(inputFolder, f.replace(".bam", ".secram"));
				
				if (!output.exists()) {
					runningThreads.incrementAndGet();
					new Thread(new Runnable() {

						public void run() {
							try {
								System.out.println("Starting thread for file  \""+f+"\"!");
								long startTime = System.currentTimeMillis();
								
								convertFile(input, output, compressionLevel, positionsPerBlock, "SECRET_1SECRET_2SECRET_3".getBytes());
								
								long totalTime = System.currentTimeMillis()-startTime;
								
								long inputLen = input.length();
								long outputLen = output.length();
								double incr = 100*((((double)outputLen)/((double)inputLen))-1);
								
								synchronized(log) {
									log.println("Processing of file \""+f+"\" complete.");
									log.println("Total time elapsed: "+SECRAMUtils.timeString(totalTime));
									log.println("Input size: "+inputLen);
									log.println("Output size: "+outputLen);
									log.println("Storage increase: "+incr+"%\n");
									log.flush();
									if (runningThreads.decrementAndGet() <= 0) {
										log.close();
									}
								}
							}
							catch(Exception ex) {
								ex.printStackTrace();
							}
						}
						
					}).start();
				}
			}
		}
		
		synchronized(log) {
			if (runningThreads.decrementAndGet() <= 0) {
				log.close();
			}
		}
	}
	
	
	/**
	 * Reads the input file in the BAM format and saves it to the output file in the SECRAM format.
	 * @param input The BAM file to read from. The BAM records *SHOULD* be ordered by their starting positions in the file.
	 * @param output The new SECRAM file to create
	 * @param compression The level of compression applied to the file, should be a value bewteen 0 and 9 (0=no compression, 9=maximum compression)
	 * @param positionsPerBlock how many position are stored in each block
	 * @throws Exception 
	 * @throws {@link IOException} If an {@link IOException} occurs during the operation
	 */
	public static void convertFile(File input, File output, int compression, int positionsPerBlock, byte[] masterKey) throws Exception {
		SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(input);
		
		long startTime = System.currentTimeMillis();
		
		SAMFileHeader bamHeader = reader.getFileHeader();
		
		SECRAMSecurityFilter filter = new SECRAMSecurityFilter(masterKey);
		SECRAMFileWriter secramWriter = new SECRAMFileWriter(output, bamHeader, new byte[0], compression, positionsPerBlock, filter);
		
		BAMtoSECRAMConverterImpl converter = new BAMtoSECRAMConverterImpl(bamHeader);
		
		//a map that returns the record corresponding to this position
		TreeMap<Long, SecramRecordOld> secramRecords = new TreeMap<Long, SecramRecordOld>();
		try {
			for (final SAMRecord samRecord : reader) {
				
				BAMRecord bamRecord = (BAMRecord)samRecord;
				converter.addBAMRecordToSECRAMRecords(bamRecord, secramRecords);
				long startPosition = SECRAMUtils.getAbsolutePosition(bamRecord.getAlignmentStart()-1, bamRecord.getReferenceIndex());
				long pos;
				//Check for any position smaller than the start of this read. Having this loop is to make sure
				//we will write complete secram records out to disk, and thus will not run out of memory.
				while (!secramRecords.isEmpty() && (pos = secramRecords.firstKey()) < startPosition) {
					SecramRecordOld completedRecord = secramRecords.remove(pos);
					completedRecord.close(); //removes the position from the list, and saves it
					secramWriter.appendRecord(completedRecord);
				}
			}
		}
		finally {
			//Save the remaining SECRAM records
			while (!secramRecords.isEmpty()) {
				SecramRecordOld remainingRecord = secramRecords.remove(secramRecords.firstKey());
				remainingRecord.close();
				secramWriter.appendRecord(remainingRecord);
			}
			//Close the writer
			secramWriter.close();
			long totalTime = System.currentTimeMillis()-startTime;
			System.out.println("Total time elapsed: "+SECRAMUtils.timeString(totalTime));
		}
		
	}
	
	public Map<Long, SecramRecordOld> createSECRAMRecords(BAMRecord... records) throws IOException{
		TreeMap<Long, SecramRecordOld> secramRecords = new TreeMap<Long, SecramRecordOld>();
		for (int i = 0; i < records.length; i++)
			addBAMRecordToSECRAMRecords(records[i], secramRecords);
		return secramRecords;
	}
	
	public void addBAMRecordToSECRAMRecords(BAMRecord bamRecord, Map<Long, SecramRecordOld> secramRecords) throws IOException{
		
		long startPosition = SECRAMUtils.getAbsolutePosition(bamRecord.getAlignmentStart()-1, bamRecord.getReferenceIndex());
		
		long lastPosition = startPosition + (bamRecord.getCigar().getReferenceLength() - 1);
		
		long pos = startPosition;
		
		SecramRecordOld currentRecord  = getRecord(pos, secramRecords);
		
		currentRecord.addReadHeader(bamRecord);
		
		String seq = bamRecord.getReadString();
		
		byte[] qualityScores = bamRecord.getBaseQualities();
		int qualityOffset = 0;
		boolean specialEnding = false;
		boolean specialEndingStart = false;
		boolean specialEndingEnd = false;
		
		Iterator<CigarElement> iter = bamRecord.getCigar().getCigarElements().iterator();
		CigarElement element;
		
		while(iter.hasNext()) {
			element = iter.next();

			//when a clipping operator is at the end of a Cigar, we end up trying to write it outside of the area covered by the read
			if (pos>lastPosition) {
				pos=lastPosition;
				currentRecord = getRecord(pos, secramRecords);
				specialEnding = true;
				specialEndingStart=true;
				specialEndingEnd = !iter.hasNext();
			}
			else if (specialEnding) {
				specialEndingStart = false;
				specialEndingEnd = !iter.hasNext();
			}

			CigarOperator op = element.getOperator();
			int opLength = element.getLength();

			String subSeq; //sub sequence from the read string
			
			//updates the position in the read sequence according to the operator
			switch (op) {
	            case M:
	            case I:
	            case S:
	            case EQ:
	            case X:
	            	subSeq = seq.substring(0,opLength); //retrieve the sub sequence for this operator
	            	seq = seq.substring(opLength); //updates remaining string
	            	break;
	            default:
	            	subSeq = "";
	        }

			char opChar;
			
			switch (op) {
			case I:
				opChar = 'I';
				break;
			case S:
				opChar = 'S';
				break;
			case H:
				opChar = 'H';
				break;
			case D:
				opChar = 'D';
				break;
			case N:
				opChar = 'N';
				break;
			case X:
				opChar = 'X';
				break;
			case P:
				opChar = 'P';
				break;
			default:
				opChar = 'M';
			}
			
			switch (op) {
			
			//op that don't change the pos in the ref sequence
			case I:
			case S:
			case H:
			case P:
				//for those element we don't want to increment the order in the PosCigar, because another operator
				//from the same read will come at the same position, UNLESS this element is the last element of this Cigar.
				
				currentRecord.addPosCigarElement(opChar, opLength, subSeq, specialEndingEnd, specialEndingStart, specialEndingEnd);
				currentRecord.updateScores(qualityScores, qualityOffset, element.getLength());
				qualityOffset += element.getLength();
				break;
				
			//op that change the pos in the ref sequence but not in the read sequence
			case D:
			case N:

			//op that change the pos in both sequences
			case M:
			case EQ:
			case X:
				//moves forward in the ref sequence, and retrieves the corresponding SECRAMRecord instance for each step
				String str = "";
				for (long max=(pos+element.getLength());pos<max;currentRecord = getRecord(++pos, secramRecords)) {
					
					//for substitutions, we read the bases 1 by 1, for the other operators,
					//we either have no bases in the read seq (D and N) or we don't store it in
					//the SECRAM format (M and EQ)
					if (op == CigarOperator.X || op == CigarOperator.M || op == CigarOperator.EQ) {
						str = subSeq.substring(0, 1);
						subSeq = subSeq.substring(1);
					}
					currentRecord.addPosCigarElement(opChar, str.length(), str, false, false, true);
					
					if (op != CigarOperator.D && op != CigarOperator.N) {
						currentRecord.updateScores(qualityScores, qualityOffset++, 1);

					}
				}
				break;
			}
			
		}
	}
	
	//The following list of methods come from interface BAMtoSECRAMConverter,
	//but they are not appropriate for the design.
	
	public List<SECRAMRecordCodec> convertSAMReadToSECRAMRecords(SAMRecord... record) {
		return null;
	}

	public List<SECRAMRecordCodec> addSAMReadToSECRAMRecords(SAMRecord record, SECRAMRecordCodec... secramRecords) {
		return null;
	}
	
	public void convert(File inputFile, SECRAMWriter writer) {
	}
	
	public SECRAMFileHeader convertSAMHeaderToSecramHeader(SAMFileHeader sam) {
		return null;
	}
}
