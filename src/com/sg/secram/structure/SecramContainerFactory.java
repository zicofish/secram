/**
 * ****************************************************************************
 * Copyright 2013 EMBL-EBI
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package com.sg.secram.structure;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sg.secram.compression.SecramRecordCodec;
import com.sg.secram.compression.SecramRecordCodecFactory;
import com.sg.secram.impl.records.SecramRecord;

/**
 * Factory for building a container of SECRAM records.
 * @author zhihuang
 *
 */
public class SecramContainerFactory {
	private int recordsPerContainer = SecramContainer.DEFATUL_RECORDS_PER_CONTAINER;
	private long globalRecordCounter = 0;
	private int globalContainerCounter = 0;

	public SecramContainerFactory(final SAMFileHeader samFileHeader,
			final int recordsPerContainer) {
		this.recordsPerContainer = recordsPerContainer;
	}

	public int getGlobalContainerCounter() {
		return globalContainerCounter;
	}

	/**
	 * Build a container for the list of SECRAM records.
	 * @param records SECRAM records
	 * @param containerSalt Random salt for the encryption
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public SecramContainer buildContainer(final List<SecramRecord> records,
			long containerSalt) throws IllegalArgumentException,
			IllegalAccessException, IOException {
		if (records.size() > recordsPerContainer) {
			throw new IllegalArgumentException("The number of records "
					+ records.size()
					+ " exceeds the predefined threshold recordsPerContaienr: "
					+ recordsPerContainer);
		}
		// get stats, create compression header and slices
		final SecramCompressionHeader compressionHeader = new SecramCompressionHeaderFactory()
				.build(records);

		final SecramContainer container = new SecramContainer();
		container.containerID = globalContainerCounter;
		container.containerSalt = containerSalt;
		container.absolutePosStart = records.get(0).getAbsolutePosition();
		container.absolutePosEnd = records.get(records.size() - 1)
				.getAbsolutePosition();
		container.coverageStart = records.get(0).mPosCigar.mCoverage;
		container.qualityLenStart = records.get(0).mQualityScores.length;
		container.compressionHeader = compressionHeader;
		container.nofRecords = records.size();
		container.globalRecordCounter = globalRecordCounter;
		container.blockCount = 0;

		final Map<Integer, ExposedByteArrayOutputStream> map = new HashMap<Integer, ExposedByteArrayOutputStream>();
		for (final int id : compressionHeader.externalIds) {
			map.put(id, new ExposedByteArrayOutputStream());
		}

		final SecramRecordCodecFactory recordCodecFactory = new SecramRecordCodecFactory();
		final ExposedByteArrayOutputStream bitBAOS = new ExposedByteArrayOutputStream();
		final DefaultBitOutputStream bitOutputStream = new DefaultBitOutputStream(
				bitBAOS);

		final SecramRecordCodec recordCodec = recordCodecFactory.buildCodec(
				compressionHeader, null, bitOutputStream, null, map);
		for (final SecramRecord record : records) {
			recordCodec.write(record);
		}

		bitOutputStream.close();
		container.coreBlock = SecramBlock.buildNewCore(bitBAOS.toByteArray());

		container.external = new HashMap<Integer, SecramBlock>();
		for (final Integer key : map.keySet()) {

			SecramBlock externalBlock = new SecramBlock(
					SecramBlockContentType.EXTERNAL, key,
					compressionHeader.externalCompressors.get(key), map
							.get(key).toByteArray());

			container.external.put(key, externalBlock);
		}

		globalContainerCounter++;
		globalRecordCounter += records.size();
		return container;
	}

	public long getGlobalRecordCounter() {
		return globalRecordCounter;
	}
}
