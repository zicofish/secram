/**
 * @author zhihuang
 */
package com.sg.secram.structure;

import htsjdk.samtools.cram.encoding.ExternalCompressor;
import htsjdk.samtools.cram.io.ITF8;
import htsjdk.samtools.cram.io.InputStreamUtils;
import htsjdk.samtools.cram.structure.BlockCompressionMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Class representing SECRAM block concept and some methods to operate with
 * block content. SECRAM block is used to hold some (usually homogeneous) binary
 * data. An external compression can be applied to the content of a block. The
 * class provides some instantiation static methods, for example to read a block
 * from an input stream. Blocks can be written out to an output stream, this may
 * be considered as a way to serialize/deserialize blocks.
 */
public class SecramBlock {

	/**
	 * Compressor applied to this block's content
	 */
	private ExternalCompressor compressor;

	/**
	 * Identifies SECRAM content type of the block.
	 */
	private SecramBlockContentType contentType;
	/**
	 * A handle to bind the block with it's metadata.
	 */
	private int contentId;
	/**
	 * The size of the compressed content in bytes.
	 */
	private int compressedContentSize;
	/**
	 * The size of the uncompressed content in bytes.
	 */
	private int rawContentSize;

	/**
	 * Uncompressed and compressed contents respectively.
	 */
	private byte[] rawContent, compressedContent;

	/**
	 * Construct an empty block.
	 */
	public SecramBlock() {
	}

	/**
	 * Construct a block that will be compressed with the specified method.
	 * @param contentType Content type of this block.
	 * @param contentId External block id.  
	 * @param method Block compression method.
	 * @param rawContent Block content.
	 */
	public SecramBlock(SecramBlockContentType contentType, int contentId,
			BlockCompressionMethod method, byte[] rawContent) {
		this(contentType, contentId, ExternalCompressor
				.createExternalCompressor(method), rawContent);
	}

	/**
	 * Construct a block that will be compressed with the specified compressor.
	 * @param contentType Content type of this block.
	 * @param contentId External block id.  
	 * @param compressor Block compressor.
	 * @param rawContent Block content.
	 */
	public SecramBlock(SecramBlockContentType contentType, int contentId,
			ExternalCompressor compressor, byte[] rawContent) {
		this.contentType = contentType;
		this.contentId = contentId;
		this.compressor = compressor;
		setRawContent(rawContent);
	}

	/**
	 * Construct a block that is not compressed.
	 * @param contentType Content type of this block.
	 * @param rawContent Block content.
	 */
	private SecramBlock(final SecramBlockContentType contentType,
			final byte[] rawContent) {
		this(contentType, 0, ExternalCompressor.createRAW(), rawContent);
	}

	/**
	 * Deserialize the block from the {@link InputStream}. The reading is
	 * parametrized by the major CRAM version number.
	 *
	 * @param major
	 *            CRAM version major number
	 * @param inputStream
	 *            input stream to read the block from
	 * @return a new {@link SecramBlock} object with fields and content from the
	 *         input stream
	 * @throws IOException
	 *             as per java IO contract
	 */
	public static SecramBlock readFromInputStream(InputStream inputStream)
			throws IOException {
		final SecramBlock block = new SecramBlock();
		block.setMethod(BlockCompressionMethod.values()[inputStream.read()]);

		final int contentTypeId = inputStream.read();
		block.setContentType(SecramBlockContentType.values()[contentTypeId]);

		block.setContentId(ITF8.readUnsignedITF8(inputStream));
		block.compressedContentSize = ITF8.readUnsignedITF8(inputStream);
		block.rawContentSize = ITF8.readUnsignedITF8(inputStream);

		block.compressedContent = new byte[block.compressedContentSize];
		InputStreamUtils.readFully(inputStream, block.compressedContent, 0,
				block.compressedContent.length);

		block.uncompress();
		return block;
	}

	/**
	 * Write the block out to the the specified {@link OutputStream}.
	 *
	 * @param outputStream
	 *            output stream to write to
	 * @throws IOException
	 *             as per java IO contract
	 */
	public void write(final OutputStream outputStream) throws IOException {
		doWrite(outputStream);
	}

	/**
	 * Write this block to outputStream. Things to be written are:
	 * <p>
	 * 1. Compression method
	 * <p>
	 * 2. Compressed content size
	 * <p>
	 * 3. Raw content size
	 * <p>
	 * 4. Compressed content
	 */
	private void doWrite(final OutputStream outputStream) throws IOException {
		if (!isCompressed())
			compress();
		if (!isUncompressed())
			uncompress();

		outputStream.write(getMethod().ordinal());
		outputStream.write(getContentType().ordinal());

		ITF8.writeUnsignedITF8(getContentId(), outputStream);
		ITF8.writeUnsignedITF8(compressedContentSize, outputStream);
		ITF8.writeUnsignedITF8(rawContentSize, outputStream);

		outputStream.write(getCompressedContent());
	}

	/**
	 * Create a new container header block with the given uncompressed content.
	 * The block wil have RAW (no compression) and CONTAINER_HEADER content
	 * type.
	 *
	 * @param rawContent
	 *            the content of the block
	 * @return a new container header block {@link SecramBlock} object
	 */
	public static SecramBlock buildNewContainerHeaderBlock(
			final byte[] rawContent) {
		return new SecramBlock(SecramBlockContentType.CONTAINER_HEADER,
				rawContent);
	}

	/**
	 * Create a new core block with the given uncompressed content. The block
	 * wil have RAW (no compression) and CORE content type.
	 *
	 * @param rawContent
	 *            the content of the block
	 * @return a new core {@link SecramBlock} object
	 */
	public static SecramBlock buildNewCore(final byte[] rawContent) {
		return new SecramBlock(SecramBlockContentType.CORE, rawContent);
	}

	/**
	 * Create a new file header block with the given uncompressed content. The
	 * block wil have RAW (no compression) and FILE_HEADER content type.
	 *
	 * @param rawContent
	 *            the content of the block
	 * @return a new file header block {@link SecramBlock} object
	 */
	public static SecramBlock buildNewFileHeaderBlock(final byte[] rawContent) {
		return new SecramBlock(SecramBlockContentType.FILE_HEADER, rawContent);
	}

	@Override
	public String toString() {
		final String raw = rawContent == null ? "NULL" : Arrays.toString(Arrays
				.copyOf(rawContent, Math.min(5, rawContent.length)));
		final String comp = compressedContent == null ? "NULL" : Arrays
				.toString(Arrays.copyOf(compressedContent,
						Math.min(5, compressedContent.length)));

		return String
				.format("method=%s, type=%s, id=%d, raw size=%d, compressed size=%d, raw=%s, comp=%s.",
						getMethod().name(), getContentType().name(),
						getContentId(), rawContentSize, compressedContentSize,
						raw, comp);
	}

	boolean isCompressed() {
		return compressedContent != null;
	}

	boolean isUncompressed() {
		return rawContent != null;
	}

	public void setRawContent(final byte[] raw) {
		rawContent = raw;
		rawContentSize = raw == null ? 0 : raw.length;

		compressedContent = null;
		compressedContentSize = 0;
	}

	public byte[] getRawContent() {
		if (rawContent == null)
			uncompress();
		return rawContent;
	}

	public int getRawContentSize() {
		return rawContentSize;
	}

	public void setContent(final byte[] raw, final byte[] compressed) {
		rawContent = raw;
		compressedContent = compressed;

		if (raw == null)
			rawContentSize = 0;
		else
			rawContentSize = raw.length;

		if (compressed == null)
			compressedContentSize = 0;
		else
			compressedContentSize = compressed.length;
	}

	void setCompressedContent(final byte[] compressed) {
		this.compressedContent = compressed;
		compressedContentSize = compressed == null ? 0 : compressed.length;

		rawContent = null;
		rawContentSize = 0;
	}

	byte[] getCompressedContent() {
		if (compressedContent == null)
			compress();
		return compressedContent;
	}

	public int getCompressedContentSize() {
		return compressedContentSize;
	}

	private void compress() {
		if (compressedContent != null || rawContent == null)
			return;

		compressedContent = compressor.compress(rawContent);
		compressedContentSize = compressedContent.length;
	}

	private void uncompress() {
		if (rawContent != null || compressedContent == null)
			return;

		rawContent = compressor.uncompress(compressedContent);
		rawContentSize = rawContent.length;
	}

	BlockCompressionMethod getMethod() {
		return compressor.getMethod();
	}

	public void setMethod(final BlockCompressionMethod method) {
		this.compressor = ExternalCompressor.createExternalCompressor(method);
	}

	public SecramBlockContentType getContentType() {
		return contentType;
	}

	public void setContentType(final SecramBlockContentType contentType) {
		this.contentType = contentType;
	}

	public int getContentId() {
		return contentId;
	}

	public void setContentId(final int contentId) {
		this.contentId = contentId;
	}
}
