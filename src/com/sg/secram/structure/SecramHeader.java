package com.sg.secram.structure;

import htsjdk.samtools.SAMFileHeader;

import java.util.Arrays;

/**
 * A starting object when dealing with SECRAM files. A {@link SecramHeader}
 * holds 3 things: 1. File format definition, including content id 2. A 64-bit
 * random salt for order-preserving encryption 3. SAM file header
 */
public final class SecramHeader {
	public static final byte[] MAGIC = "SECRAM".getBytes();

	private final byte[] id = new byte[20];

	{
		Arrays.fill(id, (byte) 0);
	}

	private long opeSalt;

	private SAMFileHeader samFileHeader;

	/**
	 * Create a new {@link SecramHeader} empty object.
	 */
	public SecramHeader() {
	}

	/**
	 * Create a new {@link SecramHeader} object with id and SAM file header. The
	 * id field by default is guaranteed to be byte[20].
	 *
	 * @param id
	 *            an identifier of the content associated with this header
	 * @param samFileHeader
	 *            the SAM file header
	 */
	public SecramHeader(final String id, final SAMFileHeader samFileHeader,
			final long opeSalt) {
		if (id != null)
			System.arraycopy(id.getBytes(), 0, this.id, 0,
					Math.min(id.length(), this.id.length));
		this.samFileHeader = samFileHeader;
		this.opeSalt = opeSalt;
	}

	/**
	 * Set the id of the header. A typical use is for example file name to be
	 * used when streaming or a checksum of the data contained in the file.
	 *
	 * @param stringID
	 *            a new id; only first 20 bytes from byte representation of java
	 *            {@link String} will be used.
	 */
	public void setID(final String stringID) {
		System.arraycopy(stringID.getBytes(), 0, this.id, 0,
				Math.min(this.id.length, stringID.length()));
	}

	public void setOpeSalt(final long opeSalt) {
		this.opeSalt = opeSalt;
	}

	/**
	 * Copy the SECRAM header into a new {@link SecramHeader} object.
	 * 
	 * @return a complete copy of the header
	 */
	@SuppressWarnings("CloneDoesntCallSuperClone")
	@Override
	public SecramHeader clone() {
		final SecramHeader clone = new SecramHeader();
		System.arraycopy(id, 0, clone.id, 0, id.length);
		clone.samFileHeader = getSamFileHeader().clone();

		return clone;
	}

	/**
	 * Checks if content of a header is the same as this one.
	 * 
	 * @param obj
	 *            another header to compare to
	 * @return true if ids and SAM file header are exactly the same, false
	 *         otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof SecramHeader))
			return false;

		final SecramHeader header = (SecramHeader) obj;
		return Arrays.equals(id, header.id)
				&& getSamFileHeader().equals(header.getSamFileHeader());
	}

	/**
	 * Get the {@link SAMFileHeader} object associated with this SECRAM file
	 * header.
	 * 
	 * @return the SAM file header
	 */
	public SAMFileHeader getSamFileHeader() {
		return samFileHeader;
	}

	public byte[] getId() {
		return id;
	}

	public long getOpeSalt() {
		return opeSalt;
	}
}
