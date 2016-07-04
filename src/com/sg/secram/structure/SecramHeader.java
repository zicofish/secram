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

import java.util.Arrays;

/**
 * A starting object when dealing with SECRAM files. A {@link SecramHeader}
 * holds 3 things: 
 * <ol>
 * <li>File format definition, including content id</li>
 * <li>A 64-bit random salt for order-preserving encryption</li>
 * <li>SAM file header</li>
 * </ol>
 * @author zhihuang
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
