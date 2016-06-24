package com.sg.secram.encoding;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/*
 * Similar to UTF-8 encoding
 * If v < 2^7, 0xxxxxxx
 * if 2^7 < v < 2^14, 10xxxxxx xxxxxxxx
 * if 2^14 < v < 2^21, 110xxxxx xxxxxxxx xxxxxxxx
 * if 2^21 < v < 2^28, 1110xxxx xxxxxxxx xxxxxxxx xxxxxxxx
 * if 2^28 < v < 2^32, 1111xxxx xxxxxxxx xxxxxxxx xxxxyyyy yyyyzzzz
 * 
 */
public class ITF8Encoding {

	public static byte[] encode(int value) {
		// TODO Auto-generated method stub
		ByteBuffer buf = ByteBuffer.allocate(8);
		if ((value >>> 7) == 0) {
			buf.put((byte) value);
		} else if ((value >>> 14) == 0) {
			buf.put((byte) ((value >> 8) | 128));
			buf.put((byte) (value & 0xFF));
		} else if ((value >>> 21) == 0) {
			buf.put((byte) ((value >> 16) | 192));
			buf.put((byte) ((value >> 8) & 0xFF));
			buf.put((byte) (value & 0xFF));
		} else if ((value >>> 28) == 0) {
			buf.put((byte) ((value >> 24) | 224));
			buf.put((byte) ((value >> 16) & 0xFF));
			buf.put((byte) ((value >> 8) & 0xFF));
			buf.put((byte) (value & 0xFF));
		} else {
			buf.put((byte) ((value >> 28) | 240));
			buf.put((byte) ((value >> 20) & 0xFF));
			buf.put((byte) ((value >> 12) & 0xFF));
			buf.put((byte) ((value >> 4) & 0xFF));
			buf.put((byte) (value & 0xFF));
		}

		buf.flip();
		byte[] array = new byte[buf.limit()];
		buf.get(array);
		buf.clear();

		return array;
	}

	public static int decode(ByteBuffer buf) {
		// TODO Auto-generated method stub
		int b1 = 0xFF & buf.get();

		if ((b1 & 128) == 0)
			return b1;

		if ((b1 & 64) == 0)
			return ((b1 & 127) << 8) | (0xFF & buf.get());

		if ((b1 & 32) == 0) {
			int b2 = 0xFF & buf.get();
			int b3 = 0xFF & buf.get();
			return ((b1 & 63) << 16) | (b2 << 8) | b3;
		}

		if ((b1 & 16) == 0) {
			return ((b1 & 31) << 24) | ((0xFF & buf.get()) << 16)
					| ((0xFF & buf.get()) << 8) | (0xFF & buf.get());
		}

		return ((b1 & 15) << 28) | ((0xFF & buf.get()) << 20)
				| ((0xFF & buf.get()) << 12) | ((0xFF & buf.get()) << 4)
				| (15 & buf.get());
	}

	public static int decode(byte[] code) {
		// TODO Auto-generated method stub
		ByteBuffer buf = ByteBuffer.wrap(code);
		int value = decode(buf);
		buf.clear();

		return value;
	}

	public int decode(InputStream is) throws IOException {
		// TODO Auto-generated method stub
		int b1 = is.read();
		if (b1 == -1)
			throw new EOFException();

		if ((b1 & 128) == 0)
			return b1;

		if ((b1 & 64) == 0)
			return ((b1 & 127) << 8) | is.read();

		if ((b1 & 32) == 0) {
			final int b2 = is.read();
			final int b3 = is.read();
			return ((b1 & 63) << 16) | b2 << 8 | b3;
		}

		if ((b1 & 16) == 0)
			return ((b1 & 31) << 24) | is.read() << 16 | is.read() << 8
					| is.read();

		return ((b1 & 15) << 28) | is.read() << 20 | is.read() << 12
				| is.read() << 4 | (15 & is.read());
	}

}
