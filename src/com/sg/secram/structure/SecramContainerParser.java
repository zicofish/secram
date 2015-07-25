package com.sg.secram.structure;

import htsjdk.samtools.cram.io.DefaultBitInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mortbay.log.Log;

import com.sg.secram.compression.SecramRecordCodec;
import com.sg.secram.compression.SecramRecordCodecFactory;
import com.sg.secram.impl.SECRAMSecurityFilter;
import com.sg.secram.impl.records.SecramRecord;

public class SecramContainerParser {
	public SecramContainerParser(){
		
	}
	
	public List<SecramRecord> getRecords(SecramContainer container, SECRAMSecurityFilter filter) throws IllegalArgumentException, IllegalAccessException, IOException{
		SecramRecordCodecFactory codecFactory = new SecramRecordCodecFactory();
		Map<Integer, InputStream> inputMap = new HashMap<Integer, InputStream>();
		for(Integer exID : container.external.keySet()){
			Log.debug("Adding external data: " + exID);
			inputMap.put(exID, new ByteArrayInputStream(container.external.get(exID).getRawContent()));
		}
		SecramRecordCodec recordCodec = codecFactory.buildCodec(container.compressionHeader,
				new DefaultBitInputStream(new ByteArrayInputStream(container.coreBlock.getRawContent())), 
				null, inputMap, null);
		List<SecramRecord> records = new ArrayList<SecramRecord>(container.nofRecords);
		
		SecramRecord dummyRecord = new SecramRecord();
		dummyRecord.setAbsolutionPosition(container.absolutePosStart);
		dummyRecord.mPosCigar.mCoverage = container.coverageStart;
		dummyRecord.mQualityScores = new byte[container.qualityLenStart];
		recordCodec.setPrevRecord(dummyRecord);
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < container.nofRecords; i++){
			SecramRecord record = new SecramRecord();
			recordCodec.read(record);
			records.add(record);
		}
		Log.debug("Container records read time: " + (System.currentTimeMillis() - start) / 1000);
		
		return records;
	}
}
