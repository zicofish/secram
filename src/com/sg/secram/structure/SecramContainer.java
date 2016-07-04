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

import java.util.Map;

/**
 * Container of a set of SECRAM records. It is organized into several blocks:
 * <ul>
 * <li>Container header (this is not a {@link SecramBlock}), that contains several pieces of general information
 * about this container, e.g., container size</li>
 * <li>Compression header</li>
 * <li>Core block</li>
 * <li>A list of external blocks</li>
 * </ul> 
 * @author zhihuang
 *
 */
public class SecramContainer {
	public static int DEFATUL_RECORDS_PER_CONTAINER = 100000;
	/**
	 * Byte size of the content excluding header.
	 */
	public int containerByteSize;
	public int containerID;
	public long containerSalt = 0;
	public long absolutePosStart = -1;
	public long absolutePosEnd = -1;
	public int coverageStart = -1;
	public int qualityLenStart = -1;
	public int nofRecords = -1;
	public long globalRecordCounter = -1;

	public int blockCount = -1;

	/**
	 * Container data
	 */
	public SecramBlock coreBlock;
	public Map<Integer, SecramBlock> external;

	public SecramCompressionHeader compressionHeader;

	// for performance measurement:
	public long buildHeaderTime;
	public long buildSlicesTime;
	public long writeTime;
	public long parseTime;
	public long readTime;

	// for indexing:
	/**
	 * Container start in the stream.
	 */
	public long offset;

	@Override
	public String toString() {
		return String
				.format("size=%f KBytes, seqID(start)=%d, start=%d, seqID(end)=%d, end=%d, span=%d, records=%d, startingCoverage=%d, blocks=%d, globalRecordCounter=%d.",
						containerByteSize / 1000.0,
						(int) (absolutePosStart >> 32), (int) absolutePosStart,
						(int) (absolutePosEnd >> 32), (int) absolutePosEnd,
						absolutePosEnd - absolutePosStart, nofRecords,
						coverageStart, blockCount, globalRecordCounter);
	}
}
