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
 * distributed under the License inputStream distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package htsjdk.samtools.cram.encoding;

import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.BitOutputStream;
import htsjdk.samtools.cram.io.LTF8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//The original class has a bug. Please check "change.log" for details. I have modified it by referring to ExternalIntegerCodec.java.

class ExternalLongCodec extends AbstractBitCodec<Long> {
	private final OutputStream outputStream;
	private final InputStream inputStream;
	// ------new
	private final OutputStream nullOutputStream = new OutputStream() {

		@Override
		public void write(@SuppressWarnings("NullableProblems") final byte[] b)
				throws IOException {
		}

		@Override
		public void write(final int b) throws IOException {
		}

		@Override
		public void write(@SuppressWarnings("NullableProblems") final byte[] b,
				final int off, final int length) throws IOException {
		}
	};

	// -------end new

	public ExternalLongCodec(final OutputStream outputStream,
			final InputStream inputStream) {
		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}

	@Override
	public Long read(final BitInputStream bitInputStream) throws IOException {
		// long result = 0;
		// for (int i = 0; i < 8; i++) {
		// result <<= 8;
		// result |= inputStream.read();
		// }
		// return result;
		// ------new
		return LTF8.readUnsignedLTF8(inputStream);
		// ------end new
	}

	@Override
	public long write(final BitOutputStream bitOutputStream, Long value)
			throws IOException {
		// for (int i = 0; i < 8; i++) {
		// outputStream.write((int) (value & 0xFF));
		// value >>>= 8;
		// }
		// return 64;
		// -------new
		return LTF8.writeUnsignedLTF8(value, outputStream);
		// -------end new
	}

	@Override
	public long numberOfBits(final Long object) {
		// return 8;
		// --------new
		try {
			return LTF8.writeUnsignedLTF8(object, nullOutputStream);
		} catch (final IOException e) {
			// this should never happened but still:
			throw new RuntimeException(e);
		}
		// --------end new
	}

	@Override
	public Long read(final BitInputStream bitInputStream, final int length)
			throws IOException {
		throw new RuntimeException("Not implemented.");
	}
}
