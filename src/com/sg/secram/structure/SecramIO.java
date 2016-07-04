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

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;
import htsjdk.samtools.cram.io.LTF8;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.BufferedLineReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A collection of methods to open and close SECRAM files.
 */
public class SecramIO {

	private static final int DEFINITION_LENGTH = SecramHeader.MAGIC.length + 20;

	/**
	 * Check if the file contains proper SECRAM header
	 *
	 * @param file
	 *            the SECRAM file to check
	 * @return true if the file is a valid SECRAM file
	 * @throws FileNotFoundException
	 * @throws IOException
	 *             as per java IO contract
	 */
	public static boolean checkHeader(final File file)
			throws FileNotFoundException {
		final SeekableStream seekableStream = new SeekableFileStream(file);
		try {
			readSecramHeader(seekableStream);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Writes SECRAM header into the specified {@link OutputStream}.
	 *
	 * @param cramHeader
	 *            the {@link SecramHeader} object to write
	 * @param outputStream
	 *            the output stream to write to
	 * @return the number of bytes written out
	 * @throws IOException
	 *             as per java IO contract
	 */
	public static long writeSecramHeader(final SecramHeader secramHeader,
			final OutputStream outputStream) throws IOException {
		outputStream.write(SecramHeader.MAGIC);
		outputStream.write(secramHeader.getId());
		for (int i = secramHeader.getId().length; i < 20; i++)
			outputStream.write(0);

		long length = SecramIO.writeSAMFileHeader(
				secramHeader.getSamFileHeader(), outputStream);
		length += (LTF8.writeUnsignedLTF8(secramHeader.getOpeSalt(),
				outputStream) + 7) / 8;

		return SecramIO.DEFINITION_LENGTH + length;
	}

	private static SecramHeader readFormatDefinition(
			final InputStream inputStream) throws IOException {
		for (final byte magicByte : SecramHeader.MAGIC) {
			if (magicByte != inputStream.read())
				throw new RuntimeException("Unknown file format.");
		}

		final SecramHeader header = new SecramHeader();

		final DataInputStream dataInputStream = new DataInputStream(inputStream);
		dataInputStream.readFully(header.getId());

		return header;
	}

	/**
	 * Read CRAM header from the given {@link InputStream}.
	 *
	 * @param inputStream
	 *            input stream to read from
	 * @return complete {@link SecramHeader} object
	 * @throws IOException
	 *             as per java IO contract
	 */
	public static SecramHeader readSecramHeader(final InputStream inputStream)
			throws IOException {
		final SecramHeader header = readFormatDefinition(inputStream);

		final SAMFileHeader samFileHeader = readSAMFileHeader(inputStream,
				new String(header.getId()));

		final long opeSalt = LTF8.readUnsignedLTF8(inputStream);

		return new SecramHeader(new String(header.getId()), samFileHeader,
				opeSalt);
	}

	private static byte[] toByteArray(final SAMFileHeader samFileHeader) {
		final ExposedByteArrayOutputStream headerBodyOS = new ExposedByteArrayOutputStream();
		final OutputStreamWriter outStreamWriter = new OutputStreamWriter(
				headerBodyOS);
		new SAMTextHeaderCodec().encode(outStreamWriter, samFileHeader);
		try {
			outStreamWriter.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		final ByteBuffer buf = ByteBuffer.allocate(4);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(headerBodyOS.size());
		buf.flip();
		final byte[] bytes = new byte[buf.limit()];
		buf.get(bytes);

		final ByteArrayOutputStream headerOS = new ByteArrayOutputStream();
		try {
			headerOS.write(bytes);
			headerOS.write(headerBodyOS.getBuffer(), 0, headerBodyOS.size());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		return headerOS.toByteArray();
	}

	private static long writeSAMFileHeader(final SAMFileHeader samFileHeader,
			final OutputStream os) throws IOException {
		final byte[] data = toByteArray(samFileHeader);
		final int length = Math.max(1024, data.length + data.length / 2);
		final byte[] blockContent = new byte[length];
		System.arraycopy(data, 0, blockContent, 0,
				Math.min(data.length, length));
		final SecramBlock block = SecramBlock
				.buildNewFileHeaderBlock(blockContent);

		final ExposedByteArrayOutputStream byteArrayOutputStream = new ExposedByteArrayOutputStream();
		block.write(byteArrayOutputStream);

		os.write(byteArrayOutputStream.getBuffer(), 0,
				byteArrayOutputStream.size());

		return byteArrayOutputStream.size();
	}

	private static SAMFileHeader readSAMFileHeader(InputStream inputStream,
			final String id) throws IOException {
		final SecramBlock block = SecramBlock.readFromInputStream(inputStream);

		inputStream = new ByteArrayInputStream(block.getRawContent());

		final ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < 4; i++)
			buffer.put((byte) inputStream.read());
		buffer.flip();
		final int size = buffer.asIntBuffer().get();

		final DataInputStream dataInputStream = new DataInputStream(inputStream);
		final byte[] bytes = new byte[size];
		dataInputStream.readFully(bytes);

		final BufferedLineReader bufferedLineReader = new BufferedLineReader(
				new ByteArrayInputStream(bytes));
		final SAMTextHeaderCodec codec = new SAMTextHeaderCodec();
		return codec.decode(bufferedLineReader, id);
	}
}
