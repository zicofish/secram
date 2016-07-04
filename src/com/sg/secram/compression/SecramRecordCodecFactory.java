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
package com.sg.secram.compression;

import htsjdk.samtools.cram.encoding.BitCodec;
import htsjdk.samtools.cram.encoding.DataSeriesType;
import htsjdk.samtools.cram.encoding.Encoding;
import htsjdk.samtools.cram.encoding.EncodingFactory;
import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.BitOutputStream;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;
import htsjdk.samtools.cram.structure.EncodingParams;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

import com.sg.secram.structure.SecramCompressionHeader;

/**
 * Builds an appropriate codec for each field of a secram record.
 * <p>
 * See also:
 * <ul>
 * <li>{@link SecramRecordCodec} for definitions of different field codecs.</li>
 * </ul>
 * 
 * @author zhihuang
 *
 */
public class SecramRecordCodecFactory {

	/**
	 * Creates the codec for each {@link SecramFieldCodec} in {@link SecramRecordCodec}.
	 * @param h A header defining the specific encoding method for each field.
	 * @param bitInputStream Input stream used for a non-external codec.
	 * @param bitOutputStream Output stream used for a non-external codec.
	 * @param inputMap Map from external block IDs to its input streams, for external codecs. 
	 * @param outputMap Map from external block IDs to its output streams, for external codecs.
	 * @return A codec for serializing / deserializing SECRAM records.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public SecramRecordCodec buildCodec(final SecramCompressionHeader h,
			BitInputStream bitInputStream, BitOutputStream bitOutputStream,
			Map<Integer, InputStream> inputMap,
			Map<Integer, ExposedByteArrayOutputStream> outputMap)
			throws IllegalArgumentException, IllegalAccessException {
		SecramRecordCodec recordCodec = new SecramRecordCodec(false);

		for (Field f : recordCodec.getClass().getFields()) {
			if (f.isAnnotationPresent(SecramDataSeries.class)) {
				SecramDataSeries sds = f.getAnnotation(SecramDataSeries.class);
				SecramEncodingKey key = sds.key();
				DataSeriesType type = sds.type();
				switch (key) {
				case FO_FeatureOrder:
				case FC_FeatureCode:
				case FL_FeatureLength:
					f.set(recordCodec,
							createFieldCodec(type, h.encodingMap.get(key),
									null, null, inputMap, outputMap));
					break;
				default:
					f.set(recordCodec,
							createFieldCodec(type, h.encodingMap.get(key),
									bitInputStream, bitOutputStream, inputMap,
									outputMap));
				}
			}
		}
		return recordCodec;
	}

	private <T> SecramFieldCodec<T> createFieldCodec(DataSeriesType valueType,
			EncodingParams params, BitInputStream bitInputStream,
			BitOutputStream bitOutputStream,
			Map<Integer, InputStream> inputMap,
			Map<Integer, ExposedByteArrayOutputStream> outputMap) {
		final EncodingFactory f = new EncodingFactory();
		final Encoding<T> encoding = f.createEncoding(valueType, params.id);
		if (encoding == null)
			throw new RuntimeException("Encoding not found: value type="
					+ valueType.name() + ", encoding id=" + params.id.name());

		encoding.fromByteArray(params.params);

		return new DefaultSecramFieldCodec<T>(encoding.buildCodec(inputMap,
				outputMap), bitInputStream, bitOutputStream);
	}

	private static class DefaultSecramFieldCodec<T> implements
			SecramFieldCodec<T> {
		private final BitCodec<T> codec;
		private BitOutputStream bitOutputStream;
		private BitInputStream bitInputStream;

		public DefaultSecramFieldCodec(final BitCodec<T> codec,
				final BitInputStream bitInputStream,
				final BitOutputStream bitOutputStream) {
			this.codec = codec;
			this.bitInputStream = bitInputStream;
			this.bitOutputStream = bitOutputStream;
		}

		@Override
		public long writeField(final T value) throws IOException {
			return codec.write(bitOutputStream, value);
		}

		@Override
		public void setBitInputStream(BitInputStream bitInputStream) {
			this.bitInputStream = bitInputStream;
		}

		@Override
		public void setBitOutputStream(BitOutputStream bitOutputStream) {
			this.bitOutputStream = bitOutputStream;
		}

		@Override
		public T readField() throws IOException {
			return codec.read(bitInputStream);
		}

		@Override
		public T readArrayField(int length) throws IOException {
			return codec.read(bitInputStream, length);
		}

	}
}
