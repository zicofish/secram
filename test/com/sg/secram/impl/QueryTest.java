package com.sg.secram.impl;

import java.io.IOException;
import java.util.Optional;

import com.sg.secram.impl.records.SecramRecord;

public class QueryTest {
	public static void main(String[] args) throws IOException {
		byte[] key = null;
		SECRAMFileReader reader = new SECRAMFileReader(
				"./data/SG10000001_S1_L001_R1_001.plaintext.secram", "./data/hs37d5.fa",
				key);
		SECRAMIterator secramIterator = reader.query("2", 29443771, 29443779);
		while (secramIterator.hasNext()) {
			Optional<SecramRecord> record = Optional.ofNullable(secramIterator
					.next());
			record.ifPresent(System.out::println); 
		}
	}
}
