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

import htsjdk.samtools.cram.build.CompressionHeaderFactory.HuffmanParamsCalculator;
import htsjdk.samtools.cram.build.CompressionHeaderFactory.IntegerEncodingCalculator;
import htsjdk.samtools.cram.encoding.ByteArrayLenEncoding;
import htsjdk.samtools.cram.encoding.Encoding;
import htsjdk.samtools.cram.encoding.ExternalByteArrayEncoding;
import htsjdk.samtools.cram.encoding.ExternalCompressor;
import htsjdk.samtools.cram.encoding.ExternalIntegerEncoding;
import htsjdk.samtools.cram.encoding.ExternalLongEncoding;
import htsjdk.samtools.cram.encoding.NullEncoding;
import htsjdk.samtools.cram.encoding.huffman.codec.HuffmanByteEncoding;
import htsjdk.samtools.cram.encoding.huffman.codec.HuffmanIntegerEncoding;
import htsjdk.samtools.cram.structure.EncodingParams;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.sg.secram.compression.SecramEncodingKey;
import com.sg.secram.impl.records.PosCigarFeature;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;

/**
 * Factory for building up the compression information for SECRAM records,
 * and put the information in a {@link SecramCompressionHeader}.
 * @author zhihuang
 *
 */
public class SecramCompressionHeaderFactory {
	public static int SENSITIVE_FIELD_EXTERNAL_ID = 0;

	/**
	 * Build up the compression information for a list of SECRAM records (in a container)
	 */
	public SecramCompressionHeader build(List<SecramRecord> records) {
		final SecramCompressionHeader header = new SecramCompressionHeader();
		header.externalIds = new ArrayList<Integer>();
		int exCounter = SENSITIVE_FIELD_EXTERNAL_ID;

		final int sensitiveFieldID = exCounter++; // 0 (Do not modify this. The
													// sensitiveField should
													// always be the first
													// block, with ID 0.)
		header.externalIds.add(sensitiveFieldID);
		header.externalCompressors.put(sensitiveFieldID,
				ExternalCompressor.createRAW());

		final int sensitiveFieldLengthID = exCounter++; // 1
		header.externalIds.add(sensitiveFieldLengthID);
		header.externalCompressors.put(sensitiveFieldLengthID,
				ExternalCompressor.createLZMA());

		final int qualityScoreID = exCounter++; // 2
		header.externalIds.add(qualityScoreID);
		header.externalCompressors.put(qualityScoreID,
				ExternalCompressor.createLZMA());

		final int tagsID = exCounter++; // 3
		header.externalIds.add(tagsID);
		header.externalCompressors
				.put(tagsID, ExternalCompressor.createBZIP2());

		final int absolutePositionID = exCounter++; // 4
		header.externalIds.add(absolutePositionID);
		header.externalCompressors.put(absolutePositionID,
				ExternalCompressor.createBZIP2());

		final int readNameID = exCounter++; // 5
		header.externalIds.add(readNameID);
		header.externalCompressors.put(readNameID,
				ExternalCompressor.createBZIP2());

		final int templateLengthID = exCounter++; // 6
		header.externalIds.add(templateLengthID);
		header.externalCompressors.put(templateLengthID,
				ExternalCompressor.createLZMA());

		final int nextAbsolutePositionID = exCounter++; // 7
		header.externalIds.add(nextAbsolutePositionID);
		header.externalCompressors.put(nextAbsolutePositionID,
				ExternalCompressor.createGZIP());

		final int coverageID = exCounter++; // 8
		header.externalIds.add(coverageID);
		header.externalCompressors.put(coverageID,
				ExternalCompressor.createLZMA());

		final int numOfReadHeadersID = exCounter++; // 9
		header.externalIds.add(numOfReadHeadersID);
		header.externalCompressors.put(numOfReadHeadersID,
				ExternalCompressor.createLZMA());

		final int qualityScoreLengthID = exCounter++; // 10
		header.externalIds.add(qualityScoreLengthID);
		header.externalCompressors.put(qualityScoreLengthID,
				ExternalCompressor.createLZMA());

		final int numberOfFeaturesID = exCounter++; // 11
		header.externalIds.add(numberOfFeaturesID);
		header.externalCompressors.put(numberOfFeaturesID,
				ExternalCompressor.createLZMA());

		header.encodingMap = new TreeMap<SecramEncodingKey, EncodingParams>();
		for (final SecramEncodingKey key : SecramEncodingKey.values())
			header.encodingMap.put(key, NullEncoding.toParam());

		{// absolute position
			header.encodingMap.put(SecramEncodingKey.AP_AbsolutePosition,
					ExternalLongEncoding.toParam(absolutePositionID));
		}

		{// number of read headers
			header.encodingMap.put(SecramEncodingKey.NH_NumberOfReadHeaders,
					ExternalIntegerEncoding.toParam(numOfReadHeadersID));
		}

		{// reference length
			final HuffmanParamsCalculator calculator = new HuffmanParamsCalculator();
			for (final SecramRecord record : records)
				for (final ReadHeader rh : record.mReadHeaders)
					calculator.add(rh.mReferenceLength);
			calculator.calculate();

			header.encodingMap.put(SecramEncodingKey.RL_ReferenceLength,
					HuffmanIntegerEncoding.toParam(calculator.values(),
							calculator.bitLens()));
		}

		{// mapping quality score
			final HuffmanParamsCalculator calculator = new HuffmanParamsCalculator();
			for (final SecramRecord record : records)
				for (final ReadHeader rh : record.mReadHeaders)
					calculator.add(rh.mMappingQuality);
			calculator.calculate();

			header.encodingMap.put(SecramEncodingKey.MQ_MappingQualityScore,
					HuffmanIntegerEncoding.toParam(calculator.values(),
							calculator.bitLens()));
		}

		{// read name
			final HuffmanParamsCalculator calculator = new HuffmanParamsCalculator();
			for (final SecramRecord record : records)
				for (final ReadHeader rh : record.mReadHeaders)
					calculator.add(rh.mReadName.length());
			calculator.calculate();

			header.encodingMap.put(SecramEncodingKey.RN_ReadName,
					ByteArrayLenEncoding.toParam(
							HuffmanIntegerEncoding.toParam(calculator.values(),
									calculator.bitLens()),
							ExternalByteArrayEncoding.toParam(readNameID)));
		}

		{// flag
			getOptimalIntegerEncoding(header, SecramEncodingKey.FG_Flag, 0,
					records);
		}

		{// template length
			header.encodingMap.put(SecramEncodingKey.TL_TemplateLength,
					ExternalIntegerEncoding.toParam(templateLengthID));
		}

		{// next absolute position
			header.encodingMap.put(SecramEncodingKey.NP_NextAbsolutePosition,
					ExternalLongEncoding.toParam(nextAbsolutePositionID));
		}

		{// tags
			final HuffmanParamsCalculator calculator = new HuffmanParamsCalculator();
			for (final SecramRecord record : records)
				for (final ReadHeader rh : record.mReadHeaders)
					calculator.add(rh.mTags.length);
			calculator.calculate();

			header.encodingMap.put(SecramEncodingKey.TG_tags,
					ByteArrayLenEncoding.toParam(
							HuffmanIntegerEncoding.toParam(calculator.values(),
									calculator.bitLens()),
							ExternalByteArrayEncoding.toParam(tagsID)));
		}

		{// quality scores
			header.encodingMap.put(SecramEncodingKey.QS_QualityScore,
					ExternalByteArrayEncoding.toParam(qualityScoreID));
		}

		{// quality scores length
			header.encodingMap.put(SecramEncodingKey.QL_QualityScoreLength,
					ExternalIntegerEncoding.toParam(qualityScoreLengthID));
		}

		{// coverage
			header.encodingMap.put(SecramEncodingKey.CV_Coverage,
					ExternalIntegerEncoding.toParam(coverageID));

		}

		{// number of features
			header.encodingMap.put(SecramEncodingKey.NF_NumberOfFeatures,
					ExternalIntegerEncoding.toParam(numberOfFeaturesID));
		}

		{// sensitive field
			header.encodingMap
					.put(SecramEncodingKey.SF_SensitiveField,
							ByteArrayLenEncoding.toParam(
									ExternalIntegerEncoding
											.toParam(sensitiveFieldLengthID),
									ExternalByteArrayEncoding
											.toParam(sensitiveFieldID)));
		}

		{// feature order
			final IntegerEncodingCalculator calculator = new IntegerEncodingCalculator(
					SecramEncodingKey.FO_FeatureOrder.name(), 0, 0);
			for (final SecramRecord record : records)
				for (final PosCigarFeature feature : record.mPosCigar
						.getNonMatchFeatures()) {
					calculator.addValue(feature.mOrder);
				}
			final Encoding<Integer> bestEncoding = calculator.getBestEncoding();

			header.encodingMap.put(
					SecramEncodingKey.FO_FeatureOrder,
					new EncodingParams(bestEncoding.id(), bestEncoding
							.toByteArray()));
		}

		{// feature code
			final HuffmanParamsCalculator calculator = new HuffmanParamsCalculator();
			for (final SecramRecord record : records)
				for (final PosCigarFeature feature : record.mPosCigar
						.getNonMatchFeatures())
					calculator.add(feature.mOP.getCharacter());
			calculator.calculate();

			header.encodingMap.put(SecramEncodingKey.FC_FeatureCode,
					HuffmanByteEncoding.toParam(calculator.valuesAsBytes(),
							calculator.bitLens()));
		}

		{// feature length
			final IntegerEncodingCalculator calculator = new IntegerEncodingCalculator(
					SecramEncodingKey.FL_FeatureLength.name(), 0);
			for (final SecramRecord record : records)
				for (final PosCigarFeature feature : record.mPosCigar
						.getNonMatchFeatures()) {
					switch (feature.mOP) {
					case F:
					case R:
					case G:
					case O:
					case I:
					case S:
					case H:
					case P:
						calculator.addValue(feature.mLength);
					default:
					}
				}

			final Encoding<Integer> bestEncoding = calculator.getBestEncoding();
			header.encodingMap.put(
					SecramEncodingKey.FL_FeatureLength,
					new EncodingParams(bestEncoding.id(), bestEncoding
							.toByteArray()));
		}

		return header;
	}

	private static int getValue(final SecramEncodingKey key, final ReadHeader rh) {
		switch (key) {
		case FG_Flag:
			return rh.mFlags;
		default:
			throw new RuntimeException("Unexpected encoding key: " + key.name());
		}
	}

	private static void getOptimalIntegerEncoding(
			final SecramCompressionHeader header, final SecramEncodingKey key,
			final int minValue, final List<SecramRecord> records) {
		final IntegerEncodingCalculator calc = new IntegerEncodingCalculator(
				key.name(), minValue);
		for (final SecramRecord record : records) {
			for (final ReadHeader rh : record.mReadHeaders) {
				final int value = getValue(key, rh);
				calc.addValue(value);
			}
		}

		final Encoding<Integer> bestEncoding = calc.getBestEncoding();
		header.encodingMap.put(key, new EncodingParams(bestEncoding.id(),
				bestEncoding.toByteArray()));
	}
}
