package com.sg.secram.compression;

import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.BitOutputStream;

import java.io.IOException;

/**
 * An interface for the codec of a SECRAM field, identified by 
 * its encoding key {@link SecramEncodingKey}.
 * <p>
 * See also:
 * <ul>
 * <li>{@link SecramRecordCodec} for usage of this interface.</li>
 * <li>{@link SecramRecordCodecFactory} for implementation of this interface,
 * and creation of all codecs in {@link SecramRecordCodec}</li>
 * </ul>
 * @param <T>
 * 			Data type of the field.
 * @author zhihuang
 *
 */
public interface SecramFieldCodec<T> {
	/**
	 * Set the input stream for reading this field
	 * <p>
	 * NOTE: This only has effect if this field codec is NOT an external codec, 
	 * because external codec has its own input stream.
	 * @param bitInputStream Where we will read this field for a non-external codec.
	 */
	void setBitInputStream(BitInputStream bitInputStream);

	/**
	 * Set the output stream for writing this field
	 * <p>
	 * NOTE: This only has effect if this field codec is NOT an external codec, 
	 * because external codec has its own output stream.
	 * @param bitOutputStream Where we will write this field for a non-external codec.
	 */
	void setBitOutputStream(BitOutputStream bitOutputStream);

	/**
	 * Write the value to the output stream of this codec.
	 * @param value The value to be written.
	 * @return The number of bits written out.
	 * @throws IOException
	 */
	long writeField(T value) throws IOException;

	/**
	 * Read this field from the input stream.
	 * @return The SECRAM field.
	 * @throws IOException
	 */
	T readField() throws IOException;

	/**
	 * This field is an array. Read an array of elements from the input stream.
	 * @param length The number of elements to read.
	 * @return The SECRAM field.
	 * @throws IOException
	 */
	T readArrayField(int length) throws IOException;
}
