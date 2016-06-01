package com.sg.secram.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class SecramIndex {
	private TreeMap<Long, Long> index = new TreeMap<Long, Long>();
	SecramIndex(File indexFile) throws IOException {
		readIndexFromFile(indexFile);
	}
	SecramIndex() {};
	
	public void addTuple(long position, long offset){
		index.put(position, offset);
	}
	
	public long getContainerOffset(long position){
		Map.Entry<Long, Long> entry = index.floorEntry(position);
		if(null == entry) return -1;
		return entry.getValue();
	}
	
	private void readIndexFromFile(File indexFile) throws IOException{
		BufferedReader bufReader = new BufferedReader(new FileReader(indexFile));
		String line = bufReader.readLine();
		while(null != line){
			String[] pair = line.split("\t");
			index.put(Long.valueOf(pair[0]), Long.valueOf(pair[1]));
			line = bufReader.readLine();
		}
		bufReader.close();
	}
	
	public void writeIndexToFile(File indexFile) throws IOException{
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(indexFile));
		Set<Entry<Long, Long>> entries = index.entrySet();
		entries.stream()
			.forEach((x) -> {
				try {
					bufWriter.write(String.format("%d\t%d\n", x.getKey(), x.getValue()));
				} catch(IOException e){
					e.printStackTrace();
				}
			});
		bufWriter.flush();
		bufWriter.close();
	}
}
