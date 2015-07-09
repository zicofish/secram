package com.sg.secram.impl.records;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.sg.secram.avro.ReadHeaderAvro;
import htsjdk.samtools.BAMRecord;

public class ReadHeader {
	
	
	private ReadHeaderAvro mAvroRecord;
	
	public ReadHeader(BAMRecord bamRecord, int order) {
		mAvroRecord = new ReadHeaderAvro();
		
		
		long nextPOS = bamRecord.getMateReferenceIndex();
		nextPOS<<=32;
		nextPOS|= (bamRecord.getMateAlignmentStart() - 1);
		
		mAvroRecord.setOrder(order);
		mAvroRecord.setRefLen(bamRecord.getCigar().getReferenceLength());
		mAvroRecord.setNextPOS(nextPOS);
		mAvroRecord.setBinMqNl(get_bin_mq_nl(bamRecord));
		mAvroRecord.setReadName(bamRecord.getReadName());
//		mAvroRecord.setReadName("");
		mAvroRecord.setFlag(bamRecord.getFlags());
		mAvroRecord.setTLen(bamRecord.getInferredInsertSize());
		mAvroRecord.setTags(ByteBuffer.wrap(get_auxiliary(bamRecord)));
//		mAvroRecord.setTags(ByteBuffer.wrap(new byte[0]));
		
	}
	public ReadHeader(ReadHeaderAvro avroRecord) {
		mAvroRecord = avroRecord;
	}
	
	private static int get_bin_mq_nl(BAMRecord bamRecord){
		int indexBin = 0;
//        if (bamRecord.getReferenceIndex() >= 0) {
//            if (bamRecord.getIndexingBin() != null) {
//                indexBin = bamRecord.getIndexingBin();
//            } else {
//                indexBin = bamRecord.computeIndexingBin();
//            }
//        }
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put((byte)(bamRecord.getReadNameLength() + 1));
        byteBuffer.put((byte)bamRecord.getMappingQuality());
        byteBuffer.putShort((short)indexBin);
        
        byteBuffer.flip();
        return byteBuffer.getInt();
	}
	private static byte[] get_auxiliary(BAMRecord bamRecord){
		//Here I assume the BAMRecord has not been modified since we read it from the BAM file
		byte[] variableBinaryData = bamRecord.getVariableBinaryRepresentation();
		int auxiliarySize = bamRecord.getAttributesBinarySize();
		if(null == variableBinaryData || -1 == auxiliarySize){
			System.err.println("The BAMRecord has been modified, hence this returned auxiliary data is unreliable.");
			return null;
		}
		byte[] ret = new byte[auxiliarySize];
		System.arraycopy(variableBinaryData, variableBinaryData.length - auxiliarySize, ret, 0, auxiliarySize);
		return ret;
	}
	
	
	private final static int HLEN_CONST_PART = 
			2 //h_len
			+2 //order
			+2 //Ref_len
			+8 //next_POS
			+4 //bin_mq_nl
			+1 //Read_name null terminator
			+2 //h_len
			+4; //Tlen
	
	public int getHLen() {
		return HLEN_CONST_PART 
				+ mAvroRecord.getReadName().length() //Read_name
				+ mAvroRecord.getTags().array().length; //Tags
	}
	public int getOrder() {
		return mAvroRecord.getOrder();
	}
	public int getRefLen() {
		return mAvroRecord.getRefLen();
	}
	public long getNextPOS() {
		return mAvroRecord.getNextPOS();
	}
	
	public int getBinMqNl() {
		return mAvroRecord.getBinMqNl();
	}
	
	public int getMappingQuality() {
		return (mAvroRecord.getBinMqNl()>>8)&0xff;
	}
	
	public int getIndexingBin() {
		return mAvroRecord.getBinMqNl()>>16;
	}
	
	public String getReadName() {
		return mAvroRecord.getReadName().toString();
	}
	public int getFlag() {
		return mAvroRecord.getFlag();
	}
	public int getTlen() {
		return mAvroRecord.getTLen();
	}
	public byte[] getTags() {
		return mAvroRecord.getTags().array();
	}
	
	public ReadHeaderAvro getAvroRecord() {
		return mAvroRecord;
	}

	
	public String toString() {
		String result = "";
		result += "Order: "+getOrder()+"\n";
		result += "RefLen: "+getRefLen()+"\n";
		result += "Next POS: "+getNextPOS()+"\n";
		result += "Bin_mq_nl: "+getBinMqNl()+"\n";
		result += "Read name: "+getReadName()+"\n";
		result += "Flag: "+getFlag()+"\n";
		result += "Template Length: "+getTlen()+"\n";
		result += "Auxiliary Data: \""+new String(getTags()).replace("\n","\\n").replace("\0","\\0")+"\"";
		
		return result;
	}
}
