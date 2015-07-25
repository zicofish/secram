package com.sg.secram.compression;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import com.sg.secram.impl.SECRAMSecurityFilter;
import com.sg.secram.impl.records.PosCigarFeature;
import com.sg.secram.impl.records.PosCigarFeatureCode;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;
import com.sg.secram.impl.records.SecramRecordOld;

import htsjdk.samtools.cram.encoding.DataSeriesType;
import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;
import htsjdk.samtools.cram.structure.EncodingKey;

public class SecramRecordCodec {
	private Charset charset = Charset.forName("UTF8");
	private SecramRecord prevRecord;
	private boolean lossyQual;
	
	@SecramDataSeries(key = SecramEncodingKey.AP_AbsolutePosition, type = DataSeriesType.LONG)
	public SecramFieldCodec<Long> absolutePositionCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.NH_NumberOfReadHeaders, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> numberOfReadHeadersCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.RL_ReferenceLength, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> referenceLengthCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.MQ_MappingQualityScore, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> mappingQualityCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.RN_ReadName, type = DataSeriesType.BYTE_ARRAY)
	public SecramFieldCodec<byte[]> readNameCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.FG_Flag, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> flagCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.TL_TemplateLength, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> templateLengthCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.NP_NextAbsolutePosition, type = DataSeriesType.LONG)
	public SecramFieldCodec<Long> nextAbsolutePositionCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.TG_tags, type = DataSeriesType.BYTE_ARRAY)
	public SecramFieldCodec<byte[]> tagsCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.QS_QualityScore, type = DataSeriesType.BYTE_ARRAY)
	public SecramFieldCodec<byte[]> qualityScoreCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.QL_QualityScoreLength, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> qualityScoreLengthCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.CV_Coverage, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> coverageCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.NF_NumberOfFeatures, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> numberOfFeaturesCodec;
	
	@SecramDataSeries(key = SecramEncodingKey.SF_SensitiveField, type = DataSeriesType.BYTE_ARRAY)
	public SecramFieldCodec<byte[]> sensitiveFieldCodec;

	//Here begins the sub fields contained in sensitive field;
		
		@SecramDataSeries(key = SecramEncodingKey.FO_FeatureOrder, type = DataSeriesType.INT)
		public SecramFieldCodec<Integer> featureOrderCodec;
		
		@SecramDataSeries(key = SecramEncodingKey.FC_FeatureCode, type = DataSeriesType.BYTE)
		public SecramFieldCodec<Byte> featureCodeCodec;
		
		@SecramDataSeries(key = SecramEncodingKey.FL_FeatureLength, type = DataSeriesType.INT)
		public SecramFieldCodec<Integer> featureLengthCodec;
		
		@SecramDataSeries(key = SecramEncodingKey.FB_FeatureBase, type = DataSeriesType.BYTE)
		public SecramFieldCodec<Byte> featureBaseCodec;
		
		@SecramDataSeries(key = SecramEncodingKey.FB_FeatureBase, type = DataSeriesType.BYTE)
		public SecramFieldCodec<byte[]> featureBaseArrayCodec;
	
		
	private static PrintStream debugfile;
	private static int debugcounter=0;
	private static int nb = 0;
	private static boolean debug = true;
	public SecramRecordCodec(boolean lossyQual){
		this.lossyQual = lossyQual;
	}
		
	public void write(SecramRecord record) throws IOException{
		if(record.mPosition == -47508351)
			System.out.println("trap");
		absolutePositionCodec.writeField(record.absolutePositionDelta);
		numberOfReadHeadersCodec.writeField(record.mReadHeaders.size());
		for(ReadHeader rh : record.mReadHeaders){
			referenceLengthCodec.writeField(rh.mReferenceLength);
			mappingQualityCodec.writeField(rh.mMappingQuality);
			readNameCodec.writeField(rh.mReadName.getBytes(charset));
			flagCodec.writeField(rh.mFlags);
			templateLengthCodec.writeField(rh.mTemplateLength);
			long tmp = rh.getNextAbsolutePosition() - record.getAbsolutePosition();
			if(tmp == -10996190161618L)
				System.out.println("trap");
			nextAbsolutePositionCodec.writeField(tmp);
			tagsCodec.writeField(rh.mTags);
		}
        qualityScoreLengthCodec.writeField(record.qualityLenDelta);
        if(!lossyQual)
			qualityScoreCodec.writeField(record.mQualityScores);
        else
        	qualityScoreCodec.writeField(LossyQualityScore.packQS(record.mQualityScores));
        
		coverageCodec.writeField(record.coverageDelta);
		List<PosCigarFeature> features = record.mPosCigar.getNonMatchFeatures();
        numberOfFeaturesCodec.writeField(features.size());
		
        { // encode the sensitive field
			ExposedByteArrayOutputStream bitBAOS = new ExposedByteArrayOutputStream();
	        DefaultBitOutputStream bitOutputStream = new DefaultBitOutputStream(bitBAOS);
	        
	        featureOrderCodec.setBitOutputStream(bitOutputStream);
	        featureCodeCodec.setBitOutputStream(bitOutputStream);
	        featureLengthCodec.setBitOutputStream(bitOutputStream);
	        
	        for(PosCigarFeature f : features){
	        	featureOrderCodec.writeField(f.mOrder);
	        	featureCodeCodec.writeField((byte)f.mOP.getCharacter());
	        	switch(f.mOP){
	        		case F:
	        		case I:
	        		case S:
	        		case R:
	        			featureLengthCodec.writeField(f.mLength);
	        			for(byte b : BaseHalfByteMap.baseArray2HalfByteArray(f.mBases.getBytes()))
	        				bitOutputStream.write(b, 4);
	        			break;
	        		case G:
	        		case O:
	        		case H:
	        		case P:
	        			featureLengthCodec.writeField(f.mLength);
	        			break;
	        		case X:
	        			bitOutputStream.write(BaseHalfByteMap.base2HalfByteMap.get(f.mBases.getBytes()[0]), 4);
	        			break;
	        		case D:
	        		case N:
	        		case M:
	        			break;
	        	}
	        }
	        bitOutputStream.close();
	        
	        sensitiveFieldCodec.writeField(bitBAOS.toByteArray());
	        
//	        int numberOfBases = record.mPosCigar.getNumberOfBases();
//	        assert numberOfBases == record.mQualityScores.length : "Assert failed at position: "+record.mPosition;
        }
	}
	
	public void read(final SecramRecord record) throws IOException{
		record.absolutePositionDelta = absolutePositionCodec.readField();
		long absPos = prevRecord.getAbsolutePosition() + record.absolutePositionDelta;
		record.setAbsolutionPosition(absPos);
		if(absPos == -1004116979)
			System.out.println("trap");
		int numberOfReadHeaders = numberOfReadHeadersCodec.readField();
		for(int i = 0; i < numberOfReadHeaders; i++){
			ReadHeader rh = new ReadHeader();
			rh.mReferenceLength = referenceLengthCodec.readField();
			rh.mMappingQuality = mappingQualityCodec.readField();
			rh.mReadName = new String(readNameCodec.readField(), charset);
			rh.mFlags = flagCodec.readField();
			rh.mTemplateLength = templateLengthCodec.readField();
			long tmp = nextAbsolutePositionCodec.readField();
			rh.setNextAbsolutionPosition(tmp + record.getAbsolutePosition());
			if(rh.getNextAbsolutePosition() == -1004116979)
				System.out.println("trap");
			rh.mTags = tagsCodec.readField();
			
			record.mReadHeaders.add(rh);
		}
		record.qualityLenDelta = qualityScoreLengthCodec.readField();
		int qualLen = record.qualityLenDelta + prevRecord.mQualityScores.length;
		if(!lossyQual)
			record.mQualityScores = qualityScoreCodec.readArrayField(qualLen);
		else
			record.mQualityScores = LossyQualityScore.unpackQS(qualityScoreCodec.readArrayField((qualLen + 1) / 2), qualLen);
		
		record.coverageDelta = coverageCodec.readField();
		record.mPosCigar.mCoverage = prevRecord.mPosCigar.mCoverage + record.coverageDelta;
		
		int numberOfFeatures = numberOfFeaturesCodec.readField();
		byte[] sensitiveField = sensitiveFieldCodec.readField();
		{// decode the sensitive field
			ByteArrayInputStream bai = new ByteArrayInputStream(sensitiveField);
			DefaultBitInputStream bitInputStream = new DefaultBitInputStream(bai);
			featureOrderCodec.setBitInputStream(bitInputStream);
			featureCodeCodec.setBitInputStream(bitInputStream);
			featureLengthCodec.setBitInputStream(bitInputStream);
			
			List<PosCigarFeature> features = null;
			int currentOrder = -1;
			for(int i = 0; i < numberOfFeatures; i++){
				int order = featureOrderCodec.readField();
				byte code = featureCodeCodec.readField();
				int length = 0;
				String bases = "";
				PosCigarFeatureCode op = PosCigarFeatureCode.getOperator((char)code);
				switch(op){
					case F:
					case I:
					case S:
					case R:
						length = featureLengthCodec.readField();
						byte[] halfByteArray = new byte[length];
						for(int j = 0; j < length; j++)
							halfByteArray[j] = (byte) bitInputStream.readBits(4);
						bases = new String(BaseHalfByteMap.halfByteArray2BaseArray(halfByteArray));
						break;
					case G:
					case O:
					case H:
					case P:
						length = featureLengthCodec.readField();
						break;
					case X:
						byte b = BaseHalfByteMap.halfByte2BaseArray[bitInputStream.readBits(4)];
						length = 1;
						bases = new String(new byte[]{b});
						break;
					case D:
					case N:
						length = 1;
						break;
					case M:
						throw new IOException("Impossible to read a 'M' operator from a secram file.");
				}
				
				if(order != currentOrder){
					if(currentOrder >= 0)
						record.mPosCigar.setNonMatchFeaturesForRead(currentOrder, features);
					features = new LinkedList<PosCigarFeature>();
					currentOrder = order;
				}
				PosCigarFeature pcf = new PosCigarFeature(order, (char)code, length, bases);
				features.add(pcf);
			}
			if(null != features)
				record.mPosCigar.setNonMatchFeaturesForRead(currentOrder, features);
	
	//		int numberOfBases = record.mPosCigar.getNumberOfBases();
	//		assert numberOfBases == record.qualityLenDelta + prevRecord.mQualityScores.length : "Assert failed at position: "+record.mPosition;
		}
		
		prevRecord = record;
	}
	
	public void setPrevRecord(SecramRecord prevRecord){
		this.prevRecord = prevRecord;
	}
}
