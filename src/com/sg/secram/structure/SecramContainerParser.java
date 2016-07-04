/**
 * Copyright © 2013-2016 Swiss Federal Institute of Technology EPFL and Sophia Genetics SA
 * 
 * All rights reserved
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials provided 
 * with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used 
 * to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * PATENTS NOTICE: Sophia Genetics SA holds worldwide pending patent applications in relation with this 
 * software functionality. For more information and licensing conditions, you should contact Sophia Genetics SA 
 * at info@sophiagenetics.com. 
 */
package com.sg.secram.structure;

import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sg.secram.compression.SecramRecordCodec;
import com.sg.secram.compression.SecramRecordCodecFactory;
import com.sg.secram.impl.SECRAMSecurityFilter;
import com.sg.secram.impl.records.SecramRecord;

/**
 * Parser that translates the compressed and encrypted information of a container into SECRAM records. 
 * @author zhihuang
 *
 */
public class SecramContainerParser {
	private static final Log log = Log.getInstance(SecramContainerIO.class);

	/**
	 * Get SECRAM records in a container.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public List<SecramRecord> getRecords(SecramContainer container,
			SECRAMSecurityFilter filter) throws IllegalArgumentException,
			IllegalAccessException, IOException {
		SecramRecordCodecFactory codecFactory = new SecramRecordCodecFactory();
		Map<Integer, InputStream> inputMap = new HashMap<Integer, InputStream>();
		for (Integer exID : container.external.keySet()) {
			log.debug("Adding external data: " + exID);
			inputMap.put(exID,
					new ByteArrayInputStream(container.external.get(exID)
							.getRawContent()));
		}
		SecramRecordCodec recordCodec = codecFactory.buildCodec(
				container.compressionHeader,
				new DefaultBitInputStream(new ByteArrayInputStream(
						container.coreBlock.getRawContent())), null, inputMap,
				null);
		List<SecramRecord> records = new ArrayList<SecramRecord>(
				container.nofRecords);

		SecramRecord dummyRecord = new SecramRecord();
		dummyRecord.setAbsolutionPosition(container.absolutePosStart);
		dummyRecord.mPosCigar.mCoverage = container.coverageStart;
		dummyRecord.mQualityScores = new byte[container.qualityLenStart];
		recordCodec.setPrevRecord(dummyRecord);

		long start = System.currentTimeMillis();
		for (int i = 0; i < container.nofRecords; i++) {
			SecramRecord record = new SecramRecord();
			recordCodec.read(record);
			records.add(record);
		}
		log.debug("Container records read time: "
				+ (System.currentTimeMillis() - start) / 1000);

		return records;
	}
}
