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
package com.sg.secram.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * The index is simple a map from absolute positions to file offsets. Each file
 * offset points to the beginning of a container.
 * 
 * @author zhihuang
 *
 */
public class SecramIndex {
	private TreeMap<Long, Long> index = new TreeMap<Long, Long>();

	/**
	 * Construct a SECRAM index from an existing index file.
	 * @throws IOException
	 */
	SecramIndex(File indexFile) throws IOException {
		readIndexFromFile(indexFile);
	}

	/**
	 * Create an empty SECRAM index (used when writing a SECRAM file). 
	 */
	SecramIndex() {
	};

	/**
	 * Add an index record (position, offset).
	 */
	public void addTuple(long position, long offset) {
		index.put(position, offset);
	}

	/**
	 * Get the container which contains a position.
	 * @param position The position to be queried.
	 * @return Container offset in the SECRAM file.
	 */
	public long getContainerOffset(long position) {
		Map.Entry<Long, Long> entry = index.floorEntry(position);
		if (null == entry)
			return -1;
		return entry.getValue();
	}

	private void readIndexFromFile(File indexFile) throws IOException {
		BufferedReader bufReader = new BufferedReader(new FileReader(indexFile));
		String line = bufReader.readLine();
		while (null != line) {
			String[] pair = line.split("\t");
			index.put(Long.valueOf(pair[0]), Long.valueOf(pair[1]));
			line = bufReader.readLine();
		}
		bufReader.close();
	}

	/**
	 * Write out the index to a file.
	 */
	public void writeIndexToFile(File indexFile) throws IOException {
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(indexFile));
		Set<Entry<Long, Long>> entries = index.entrySet();
		entries.stream().forEach(
				(x) -> {
					try {
						bufWriter.write(String.format("%d\t%d\n", x.getKey(),
								x.getValue()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
		bufWriter.flush();
		bufWriter.close();
	}
}
