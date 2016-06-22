package com.sg.secram.impl.records;

import htsjdk.samtools.BAMRecord;

public class ReadHeader {
	
	public int mReferenceLength = -1;
	public int mMappingQuality = -1;
	public String mReadName = null;
	public int mFlags = 0;
	public int mTemplateLength = -1;
	public int mNextReferenceIndex = -1;
	public int mNextPosition = -1;
	public byte[] mTags;
	
	public ReadHeader(){}
	
	public ReadHeader(BAMRecord bamRecord) {
		mReferenceLength = bamRecord.getCigar().getReferenceLength();
		mMappingQuality = bamRecord.getMappingQuality();
		mReadName = bamRecord.getReadName();
		mFlags = bamRecord.getFlags();
		mTemplateLength = bamRecord.getInferredInsertSize();
		mNextReferenceIndex = bamRecord.getMateReferenceIndex();
		mNextPosition = bamRecord.getMateAlignmentStart() - 1;
		mTags = get_auxiliary(bamRecord);		
	}
	
//	private static int get_bin_mq_nl(BAMRecord bamRecord){
//		int indexBin = 0;
////        if (bamRecord.getReferenceIndex() >= 0) {
////            if (bamRecord.getIndexingBin() != null) {
////                indexBin = bamRecord.getIndexingBin();
////            } else {
////                indexBin = bamRecord.computeIndexingBin();
////            }
////        }
//		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
//		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//		byteBuffer.put((byte)(bamRecord.getReadNameLength() + 1));
//        byteBuffer.put((byte)bamRecord.getMappingQuality());
//        byteBuffer.putShort((short)indexBin);
//        
//        byteBuffer.flip();
//        return byteBuffer.getInt();
//	}
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
	
	
//	private final static int HLEN_CONST_PART = 
//			2 //h_len
//			+2 //order
//			+2 //Ref_len
//			+8 //next_POS
//			+4 //bin_mq_nl
//			+1 //Read_name null terminator
//			+2 //h_len
//			+4; //Tlen
//	
//	public int getHLen() {
//		return HLEN_CONST_PART 
//				+ mAvroRecord.getReadName().length() //Read_name
//				+ mAvroRecord.getTags().array().length; //Tags
//	}

	public long getNextAbsolutePosition(){
		long result = mNextReferenceIndex;
		result <<= 32;
		result |= (0x00000000FFFFFFFFL & mNextPosition);
		return result;
	}
	
	public void setNextAbsolutionPosition(long nap){
		mNextReferenceIndex = (int) (nap >> 32);
		mNextPosition = (int) nap;
	}
			
	public String toString() {
		String result = "";
		result += "RefLen: "+ mReferenceLength +"\n";
		result += "Mapping quality: " + mMappingQuality + "\n";
		result += "Read name: "+ mReadName +"\n";
		result += "Flag: "+ mFlags +"\n";
		result += "Template Length: "+ mTemplateLength +"\n";
		result += "Next reference index: " + mNextReferenceIndex + "\n";
		result += "Next position: " + mNextPosition + "\n";
		result += "Auxiliary Data: \""+new String(mTags).replace("\n","\\n").replace("\0","\\0")+"\"";
		
		return result;
	}
}
