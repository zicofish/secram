package htsjdk.samtools.cram.encoding;

import htsjdk.samtools.cram.encoding.rans.RANS.ORDER;
import htsjdk.samtools.cram.io.ExternalCompression;
import htsjdk.samtools.cram.structure.BlockCompressionMethod;

import java.io.IOException;

public abstract class ExternalCompressor {
	private final BlockCompressionMethod method;

	private ExternalCompressor(final BlockCompressionMethod method) {
		this.method = method;
	}

	public BlockCompressionMethod getMethod() {
		return method;
	}

	public static ExternalCompressor createExternalCompressor(
			BlockCompressionMethod method) {
		switch (method) {
		case RAW:
			return createRAW();
		case GZIP:
			return createGZIP();
		case LZMA:
			return createLZMA();
		case BZIP2:
			return createBZIP2();
		case DEFLATE:
			return createDEFLATE();
		default:
			return null;
		}
	}

	public abstract byte[] compress(byte[] data);

	public abstract byte[] uncompress(byte[] data);

	public static ExternalCompressor createRAW() {
		return new ExternalCompressor(BlockCompressionMethod.RAW) {

			@Override
			public byte[] compress(final byte[] data) {
				return data;
			}

			@Override
			public byte[] uncompress(byte[] data) {
				return data;
			}
		};
	}

	public static ExternalCompressor createGZIP() {
		return new ExternalCompressor(BlockCompressionMethod.GZIP) {

			@Override
			public byte[] compress(final byte[] data) {
				try {
					return ExternalCompression.gzip(data);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public byte[] uncompress(byte[] data) {
				try {
					return ExternalCompression.gunzip(data);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public static ExternalCompressor createLZMA() {
		return new ExternalCompressor(BlockCompressionMethod.LZMA) {

			@Override
			public byte[] compress(final byte[] data) {
				try {
					return ExternalCompression.xz(data);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public byte[] uncompress(byte[] data) {
				try {
					return ExternalCompression.unxz(data);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public static ExternalCompressor createBZIP2() {
		return new ExternalCompressor(BlockCompressionMethod.BZIP2) {

			@Override
			public byte[] compress(final byte[] data) {
				try {
					return ExternalCompression.bzip2(data);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public byte[] uncompress(byte[] data) {
				try {
					return ExternalCompression.unbzip2(data);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};

	}

	public static ExternalCompressor createRANS(final ORDER order) {
		return new ExternalCompressor(BlockCompressionMethod.RANS) {

			@Override
			public byte[] compress(final byte[] data) {
				return ExternalCompression.rans(data, order);
			}

			@Override
			public byte[] uncompress(byte[] data) {
				return ExternalCompression.unrans(data);
			}
		};
	}

	public static ExternalCompressor createDEFLATE() {
		return new ExternalCompressor(BlockCompressionMethod.DEFLATE) {

			@Override
			public byte[] compress(final byte[] data) {
				try {
					return ExternalCompression.deflate(data);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public byte[] uncompress(byte[] data) {
				try {
					return ExternalCompression.inflate(data);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
