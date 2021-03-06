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

import htsjdk.samtools.cram.encoding.ExternalCompressor;
import htsjdk.samtools.cram.encoding.NullEncoding;
import htsjdk.samtools.cram.io.ITF8;
import htsjdk.samtools.cram.io.InputStreamUtils;
import htsjdk.samtools.cram.structure.EncodingID;
import htsjdk.samtools.cram.structure.EncodingParams;
import htsjdk.samtools.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sg.secram.compression.SecramEncodingKey;

/**
 * Header that maintains the compression information of a container, e.g., the block compression method
 * for an external block.
 * @author zhihuang
 *
 */
public class SecramCompressionHeader {
	public Map<SecramEncodingKey, EncodingParams> encodingMap;
	public final Map<Integer, ExternalCompressor> externalCompressors = new HashMap<Integer, ExternalCompressor>();

	public List<Integer> externalIds;

	private static final ByteBuffer mapBuffer = ByteBuffer
			.allocate(1024 * 5000);

	private static Log log = Log.getInstance(SecramCompressionHeader.class);

	public byte[] toByteArray() {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try{
			write(byteArrayOutputStream);
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * Write the header to the output stream.
	 * @throws IOException
	 */
	private void write(final OutputStream outputStream) throws IOException {

		{ // encoding map:
			int size = 0;
			for (final SecramEncodingKey encodingKey : encodingMap.keySet()) {
				if (encodingMap.get(encodingKey).id != EncodingID.NULL)
					size++;
			}

			// final ByteBuffer mapBuffer = ByteBuffer.allocate(1024 * 1000);
			mapBuffer.clear();
			ITF8.writeUnsignedITF8(size, mapBuffer);
			for (final SecramEncodingKey encodingKey : encodingMap.keySet()) {
				if (encodingMap.get(encodingKey).id == EncodingID.NULL)
					continue;

				mapBuffer.put((byte) encodingKey.name().charAt(0));
				mapBuffer.put((byte) encodingKey.name().charAt(1));

				final EncodingParams params = encodingMap.get(encodingKey);
				mapBuffer.put((byte) (0xFF & params.id.ordinal()));
				ITF8.writeUnsignedITF8(params.params.length, mapBuffer);
				mapBuffer.put(params.params);
			}
			mapBuffer.flip();
			final byte[] mapBytes = new byte[mapBuffer.limit()];
			mapBuffer.get(mapBytes);

			ITF8.writeUnsignedITF8(mapBytes.length, outputStream);
			outputStream.write(mapBytes);
		}
	}

	/**
	 * Read a header from the byte array.
	 */
	public void read(final byte[] data) {
		try {
			read(new ByteArrayInputStream(data));
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Read a header from the input stream
	 * @throws IOException
	 */
	private void read(final InputStream inputStream) throws IOException {
		{ // encoding map
			int byteSize = ITF8.readUnsignedITF8(inputStream);
			byte[] bytes = new byte[byteSize];
			InputStreamUtils.readFully(inputStream, bytes, 0, bytes.length);
			ByteBuffer buffer = ByteBuffer.wrap(bytes);

			int mapSize = ITF8.readUnsignedITF8(buffer);
			encodingMap = new TreeMap<SecramEncodingKey, EncodingParams>();
			for (SecramEncodingKey key : SecramEncodingKey.values())
				encodingMap.put(key, NullEncoding.toParam());

			for (int i = 0; i < mapSize; i++) {
				String shortKey = new String(new byte[] { buffer.get(),
						buffer.get() });
				SecramEncodingKey encodingKey = SecramEncodingKey
						.byFirstTwoChars(shortKey);
				if (null == encodingKey) {
					log.debug("Unknown encoding key: " + shortKey);
					continue;
				}
				EncodingID id = EncodingID.values()[buffer.get()];
				int paramLen = ITF8.readUnsignedITF8(buffer);
				byte[] paramBytes = new byte[paramLen];
				buffer.get(paramBytes);

				encodingMap
						.put(encodingKey, new EncodingParams(id, paramBytes));

				log.debug(String.format("FOUND ENCODING: %s, %s, %s.",
						encodingKey.name(), id.name(),
						Arrays.toString(Arrays.copyOf(paramBytes, 20))));
			}
		}
	}
}
