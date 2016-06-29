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
