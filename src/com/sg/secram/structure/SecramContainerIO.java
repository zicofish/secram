package com.sg.secram.structure;

import htsjdk.samtools.cram.io.CramInt;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;
import htsjdk.samtools.cram.io.ITF8;
import htsjdk.samtools.cram.io.LTF8;
import htsjdk.samtools.cram.structure.BlockCompressionMethod;
import htsjdk.samtools.util.Log;
import org.apache.commons.compress.utils.CountingOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Methods to read and write SECRAM containers.
 */
public class SecramContainerIO {
	private static final Log log = Log.getInstance(SecramContainerIO.class);

	/*
	 * Calculate the storage size of each type of information
	 */
	public static int containerHeaderSize = 0;
	public static int coreBlockSize = 0;
	public static int compressionHeaderSize = 0;
	public static int[] externalSizes = new int[12];

	/**
	 * Reads container header only from a {@link InputStream}.
	 *
	 * @param major
	 *            the CRAM version to assume
	 * @param inputStream
	 *            the input stream to read from
	 * @return a new {@link Container} object with container header values
	 *         filled out but empty body (no slices and blocks).
	 * @throws IOException
	 *             as per java IO contract
	 */
	public static boolean readContainerHeader(final SecramContainer container,
			final InputStream inputStream) throws IOException {
		final byte[] peek = new byte[4];
		int character = inputStream.read();
		if (character == -1)
			return false;

		peek[0] = (byte) character;
		for (int i = 1; i < peek.length; i++) {
			character = inputStream.read();
			if (character == -1)
				throw new RuntimeException("Incomplete or broken stream.");
			peek[i] = (byte) character;
		}

		container.containerByteSize = CramInt.int32(peek);
		container.containerID = ITF8.readUnsignedITF8(inputStream);
		container.containerSalt = LTF8.readUnsignedLTF8(inputStream);
		container.absolutePosStart = LTF8.readUnsignedLTF8(inputStream);
		container.absolutePosEnd = LTF8.readUnsignedLTF8(inputStream);
		container.coverageStart = ITF8.readUnsignedITF8(inputStream);
		container.qualityLenStart = ITF8.readUnsignedITF8(inputStream);
		container.nofRecords = ITF8.readUnsignedITF8(inputStream);
		container.globalRecordCounter = LTF8.readUnsignedLTF8(inputStream);
		container.blockCount = ITF8.readUnsignedITF8(inputStream);

		return true;
	}

	public static SecramContainer readContainer(final InputStream inputStream)
			throws IOException {

		final long time1 = System.nanoTime();
		final SecramContainer container = new SecramContainer();
		if (!readContainerHeader(container, inputStream)) {
			log.debug("End of stream. No more container.");
			return null;
		}

		SecramBlock block = SecramBlock.readFromInputStream(inputStream);
		if (block.getContentType() != SecramBlockContentType.COMPRESSION_HEADER)
			throw new RuntimeException("Content type does not match: "
					+ block.getContentType().name());
		container.compressionHeader = new SecramCompressionHeader();
		container.compressionHeader.read(block.getRawContent());

		container.external = new HashMap<Integer, SecramBlock>();
		for (int i = 1; i < container.blockCount; i++) {
			block = SecramBlock.readFromInputStream(inputStream);
			switch (block.getContentType()) {
			case CORE:
				container.coreBlock = block;
				break;
			case EXTERNAL:
				container.external.put(block.getContentId(), block);
				break;
			default:
				throw new RuntimeException(
						"Not a content block, content type id "
								+ block.getContentType().name());
			}
		}

		final long time2 = System.nanoTime();

		log.debug("READ CONTAINER: " + container.toString());
		container.readTime = time2 - time1;

		return container;
	}

	/**
	 * Writes a {@link Container} header information to a {@link OutputStream}.
	 *
	 * @param major
	 *            the CRAM version to assume
	 * @param container
	 *            the container holding the header to write
	 * @param outputStream
	 *            the stream to write to
	 * @return the number of bytes written
	 * @throws IOException
	 *             as per java IO contract
	 */
	public static int writeContainerHeader(final SecramContainer container,
			final OutputStream outputStream) throws IOException {

		int length = (CramInt.writeInt32(container.containerByteSize,
				outputStream) + 7) / 8;
		length += (ITF8.writeUnsignedITF8(container.containerID, outputStream) + 7) / 8;
		length += (LTF8
				.writeUnsignedLTF8(container.containerSalt, outputStream) + 7) / 8;
		length += (LTF8.writeUnsignedLTF8(container.absolutePosStart,
				outputStream) + 7) / 8;
		length += (LTF8.writeUnsignedLTF8(container.absolutePosEnd,
				outputStream) + 7) / 8;
		length += (ITF8
				.writeUnsignedITF8(container.coverageStart, outputStream) + 7) / 8;
		length += (ITF8.writeUnsignedITF8(container.qualityLenStart,
				outputStream) + 7) / 8;
		length += (ITF8.writeUnsignedITF8(container.nofRecords, outputStream) + 7) / 8;
		length += (LTF8.writeUnsignedLTF8(container.globalRecordCounter,
				outputStream) + 7) / 8;
		length += (ITF8.writeUnsignedITF8(container.blockCount, outputStream) + 7) / 8;

		return length;
	}

	/**
	 * Writes a complete {@link SecramContainer} with its header to a
	 * {@link OutputStream}.
	 *
	 * @param container
	 *            the container to write
	 * @param outputStream
	 *            the stream to write to
	 * @return the number of bytes written out
	 * @throws IOException
	 *             as per java IO contract
	 */
	public static int writeContainer(final SecramContainer container,
			final OutputStream outputStream) throws IOException {

		final long time1 = System.nanoTime();
		final ExposedByteArrayOutputStream byteArrayOutputStream = new ExposedByteArrayOutputStream();

		/* Write compression header block */
		final SecramBlock block = new SecramBlock();
		block.setContentType(SecramBlockContentType.COMPRESSION_HEADER);
		block.setContentId(0);
		block.setMethod(BlockCompressionMethod.RAW);
		final byte[] bytes = container.compressionHeader.toByteArray();
		block.setRawContent(bytes);
		block.write(byteArrayOutputStream);
		compressionHeaderSize += bytes.length;
		container.blockCount = 1;

		container.coreBlock.write(byteArrayOutputStream);
		coreBlockSize += container.coreBlock.getCompressedContentSize();
		container.blockCount++;
		for (final Entry<Integer, SecramBlock> entry : container.external.entrySet()) {
			((SecramBlock) entry.getValue()).write(byteArrayOutputStream);
			externalSizes[(int) entry.getKey()] += ((SecramBlock) entry
					.getValue()).getCompressedContentSize();
			container.blockCount++;
		}

		container.containerByteSize = byteArrayOutputStream.size();

		int length = writeContainerHeader(container, outputStream);
		containerHeaderSize += length;
		outputStream.write(byteArrayOutputStream.getBuffer(), 0,
				byteArrayOutputStream.size());
		length += byteArrayOutputStream.size();
		byteArrayOutputStream.close();

		final long time2 = System.nanoTime();

		log.debug("CONTAINER WRITTEN: " + container.toString());

		container.writeTime = time2 - time1;

		return length;
	}

	/**
	 * Calculates the byte size of a container.
	 *
	 * @param container
	 *            the container to be weighted
	 * @return the total number of bytes the container would occupy if written
	 *         out
	 */
	public static long getByteSize(final SecramContainer container) {
		final CountingOutputStream countingOutputStream = new CountingOutputStream(
				new OutputStream() {
					@Override
					public void write(final int b) throws IOException {
					}
				});

		try {
			writeContainer(container, countingOutputStream);
			countingOutputStream.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		return countingOutputStream.getBytesWritten();
	}
}
