package com.sg.secram.compression;

import htsjdk.samtools.cram.encoding.BitCodec;
import htsjdk.samtools.cram.encoding.Encoding;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;
import htsjdk.samtools.cram.io.ITF8;
import htsjdk.samtools.cram.structure.EncodingID;
import htsjdk.samtools.cram.structure.EncodingParams;

import java.io.InputStream;
import java.util.Map;

public class HalfByteEncoding implements Encoding<Byte> {
	// The encodingId and contentId are not used in this class.
	private static final EncodingID encodingId = EncodingID.EXTERNAL;
	private int contentId = -1;

	public HalfByteEncoding() {
	}

	public static EncodingParams toParam(final int contentId) {
		final HalfByteEncoding halfByteEncoding = new HalfByteEncoding();
		halfByteEncoding.contentId = contentId;
		return new EncodingParams(encodingId, halfByteEncoding.toByteArray());
	}

	@Override
	public byte[] toByteArray() {
		return ITF8.writeUnsignedITF8(contentId);
	}

	@Override
	public void fromByteArray(final byte[] data) {
		contentId = ITF8.readUnsignedITF8(data);
	}

	@Override
	public BitCodec<Byte> buildCodec(final Map<Integer, InputStream> inputMap,
			final Map<Integer, ExposedByteArrayOutputStream> outputMap) {
		return new HalfByteCodec();
	}

	@Override
	public EncodingID id() {
		return encodingId;
	}

}
