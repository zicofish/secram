package com.sg.secram.impl;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;

import com.sg.secram.SECRAMEncryptionFilter;
import com.sg.secram.avro.SecramHeaderAvro;
import com.sg.secram.avro.SecramRecordAvro;
import com.sg.secram.impl.records.SecramRecordOld;


public class SECRAMFileWriterOld {

	private byte[] mHeader;
	private byte[] mUnalignedRecords;
	
	private SAMFileHeader mSAMFileHeader;
	
	private File mFile;

	private final static int MIN_BLOCK_SIZE = 1000;
	private final static int MAX_BLOCK_SIZE = 500000;
	
	//when we have more than this value in the queue, the producer thread will block
	private final static int MAX_QUEUE_SIZE = 20;
	
	//private long writtenBytes = 0;
	private long startPosition = -1;
	private long lastPosition = 0;
	
	//when converting from BAM, the program uses 2 threads:
	//-one that will read from the bam file, and do the actual conversion to a SECRAM record
	//-one that will compress and write data to disk
	//as the compression clearly appears to be the bottleneck I tried to use 1 thread dedicated to this task
	
	//the 2 threads use a producer-consumer model, with a queue
	private LinkedBlockingQueue<SecramRecordOld> queue = new LinkedBlockingQueue<SecramRecordOld>(MAX_QUEUE_SIZE);
	
	//if something wrong happens on the consumer thread, we store the exception here to display it on the producer thread (the "main" thread)
	private volatile Exception error = null;
	
	//Runnable instance for the consumer thread
	private SECRAMWriter secramWriter;
	
	private SECRAMSecurityFilter mFilter;
	
	public SECRAMFileWriterOld(File outputFile, SAMFileHeader header, byte[] unalignedRecords, int compressionLevel, int positionsPerBlock, SECRAMSecurityFilter filter) throws IOException {
		
		mSAMFileHeader = header;
		mHeader = convertHeaderToByteArray(header);
		mUnalignedRecords = unalignedRecords;
		mFile = outputFile;
		mFilter = filter;
		
		if (positionsPerBlock > MAX_BLOCK_SIZE)
			positionsPerBlock = MAX_BLOCK_SIZE;
		else if (positionsPerBlock < MIN_BLOCK_SIZE)
			positionsPerBlock = MIN_BLOCK_SIZE;
		
		if (compressionLevel<0) compressionLevel = 0;
		else if (compressionLevel>9) compressionLevel = 9;
		
		//System.out.println("[DEBUG] Creating new SECRAM file "+file+".");
		
		DatumWriter<SecramRecordAvro> datumWriter = new SpecificDatumWriter<SecramRecordAvro>(SecramRecordAvro.class);
		DataFileWriter<SecramRecordAvro> writer = new DataFileWriter<SecramRecordAvro>(datumWriter);
		if (compressionLevel>0)
			writer.setCodec(CodecFactory.deflateCodec(compressionLevel));
		writer.create(SecramRecordAvro.SCHEMA$, mFile);
		
		secramWriter = new SECRAMWriter(positionsPerBlock, writer);
		
		new Thread(secramWriter).start();
		
	}
	
	public SAMFileHeader getBAMHeader() {
		return mSAMFileHeader;
	}
	
	public byte[] getUnalignedRecords() {
		return mUnalignedRecords;
	}
	public long getFirstPOS() {
		return startPosition;
	}
	public long getLastPOS() {
		return lastPosition;
	}
	
	public void close() throws IOException, InterruptedException {
		while(!queue.isEmpty()){
			Thread.sleep(1000);
		}
		secramWriter.close();
	}
	
	public void appendRecord(SecramRecordOld record) throws Exception {
		try {
			queue.put(record);
		}
		catch(InterruptedException ex) {
			ex.printStackTrace(); //should never happen, unless we kill the process from the OS
		}
		if (error != null) {
			throw error;
		}
	}
	
	//class for the "consumer" thread.
	//this thread will basically work on the Compression and Writing to Disk parts of the conversion
	private class SECRAMWriter implements Runnable {

		private int blockSize = 0;

		private List<Long> positions = new LinkedList<Long>();
		private List<Long> index = new LinkedList<Long>();
		private Long opeSalt = 0L;
		private List<Long> blockSalts = new LinkedList<Long>();
		private SecureRandom sr = null;

		private int mPositionsPerBlock;

		private DataFileWriter<SecramRecordAvro> mWriter;
		
		private volatile boolean closeFlag = false;
		
		private SECRAMWriter(int positionsPerBlock, DataFileWriter<SecramRecordAvro> writer) {
			mPositionsPerBlock = positionsPerBlock;
			mWriter = writer;
			try {
				sr = SecureRandom.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			opeSalt = sr.nextLong();
			mFilter.initPositionEncryptionMethod(opeSalt);
		}
		
		@Override
		public void run() {
			
			try {
				SecramRecordOld record;
				while(!closeFlag) {
					record = null;
					try {
						record = queue.poll(1000, TimeUnit.MILLISECONDS);
					}
					catch(InterruptedException ex) {}
					
					if (record != null) {
						writeRecord(record);
					}
				}
				unsyncClose();
			}
			catch(IOException | NoSuchAlgorithmException ex) {
				error = ex;
			}
			finally {
				queue.clear(); //empty the queue so the producer thread does not wait forever in the put method
			}
		}
		
		private void writeRecord(SecramRecordOld record) throws IOException, NoSuchAlgorithmException {
			if (startPosition == -1) {
				startPosition = record.getPOS();
				
				positions.add(mFilter.encryptPosition(record.getPOS()));
				index.add(0L);
				long r = sr.nextLong();
				mFilter.initPosCigarEncryptionMethod(r);
				blockSalts.add(r);
			}
			
			if (blockSize>=mPositionsPerBlock) {
				index.add(mWriter.sync()); //end of block
				positions.add(mFilter.encryptPosition(record.getPOS()));
				long r = sr.nextLong();
				mFilter.initPosCigarEncryptionMethod(r);
				blockSalts.add(r);
				
				blockSize = 0;
			}
			
			lastPosition = record.getPOS();
			
			++blockSize;

			mFilter.encryptRecord(record);
			mWriter.append(record.getAvroRecord());
		}
		
		private void unsyncClose() throws IOException {
			mWriter.close();
			
			SecramHeaderAvro header = new SecramHeaderAvro();
			header.setBAMHeader(ByteBuffer.wrap(mHeader));
			header.setUnalignedRecords(ByteBuffer.wrap(mUnalignedRecords));
			header.setPositions(positions);
			header.setIndex(index);
			header.setOpeSalt(opeSalt);
			header.setBlockSalts(blockSalts);
			
			DatumWriter<SecramHeaderAvro> datumWriter = new SpecificDatumWriter<SecramHeaderAvro>(SecramHeaderAvro.class);
			DataFileWriter<SecramHeaderAvro> hWriter = new DataFileWriter<SecramHeaderAvro>(datumWriter);
			hWriter.create(SecramHeaderAvro.SCHEMA$, new File(mFile+"h"));
			hWriter.append(header);
			hWriter.close();
		}
		
		private void close() {
			closeFlag = true;
		}
	}
	
	private static byte[] convertHeaderToByteArray(SAMFileHeader samFileHeader) {
		
		String headerText = samFileHeader.getTextHeader();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//Please be aware that DataOutputStream is big-endian, which is different from BAM specification
		DataOutputStream dos = new DataOutputStream(baos);
		
		try{
			dos.write(SECRAMFileConstants.SECRAM_MAGIC, 0, SECRAMFileConstants.SECRAM_MAGIC.length);
	
	        // calculate and write the length of the SAM file header text and the header text
			dos.writeInt(headerText.length());
	        dos.writeBytes(headerText);
	
	        // write the sequences binarily.  This is redundant with the text header
	        dos.writeInt(samFileHeader.getSequenceDictionary().size());
	        for (final SAMSequenceRecord sequenceRecord: samFileHeader.getSequenceDictionary().getSequences()) {
	        	dos.writeInt(sequenceRecord.getSequenceName().length() + 1);
	            dos.writeBytes(sequenceRecord.getSequenceName() + '\0');
	            dos.writeInt(sequenceRecord.getSequenceLength());
	        }
		}
		catch (IOException e)
		{
			System.err.println("DataOutputStream write error in getBAMHeader");
		}
        return baos.toByteArray();
	}
}
