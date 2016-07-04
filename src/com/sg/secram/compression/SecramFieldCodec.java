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
