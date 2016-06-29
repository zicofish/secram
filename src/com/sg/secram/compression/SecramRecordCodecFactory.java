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
