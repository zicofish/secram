package com.sg.secram.impl;

import java.io.IOException;
import java.util.Optional;

import com.sg.secram.encryption.OPE;
import com.sg.secram.encryption.SECRAMEncryptionFactory;
import com.sg.secram.impl.records.SecramRecord;
import com.sg.secram.util.SECRAMUtils;

public class CoverageTest {
	public static void main(String[] args) throws Exception {
		checkCFTRCoverage();
	}

	public static void checkCFTRCoverage() throws Exception {
		String secram = "./data/miniCaviar_IDT_NEB.runA.NA12878.bwa.secram";
		byte[] key = "SECRET_1SECRET_2SECRET_3".getBytes();
		String CFTR_ref = "7";
		int CFTR_start = 117120017, CFTR_end = 117308718;

		SECRAMFileReader secramReader = new SECRAMFileReader(secram,
				"./data/hs37d5.fa", key);
		OPE ope = (OPE) SECRAMEncryptionFactory.createPositionEM(
				"SECRET_1SECRET_2SECRET_3".getBytes(), secramReader
						.getSecramHeader().getOpeSalt());
		long absStart = SECRAMUtils.getAbsolutePosition(CFTR_start,
				secramReader.getSAMFileHeader().getSequenceIndex(CFTR_ref));
		long absEnd = SECRAMUtils.getAbsolutePosition(CFTR_end, secramReader
				.getSAMFileHeader().getSequenceIndex(CFTR_ref));
		SECRAMIterator secramIterator = secramReader.query(
				ope.encrypt(absStart), ope.encrypt(absEnd));
		int minCov = 99999, maxCov = 0, numRecord = 0, totalCov = 0;
		while (secramIterator.hasNext()) {
			Optional<SecramRecord> record = Optional.ofNullable(secramIterator
					.next());
			if (record.isPresent()) {
				if (record.get().mPosCigar.mCoverage < minCov)
					minCov = record.get().mPosCigar.mCoverage;
				if (record.get().mPosCigar.mCoverage > maxCov)
					maxCov = record.get().mPosCigar.mCoverage;
				totalCov += record.get().mPosCigar.mCoverage;
				numRecord++;
			}
		}
		System.out.println("Min cov: " + minCov);
		System.out.println("Max cov: " + maxCov);
		System.out.println("Average cov: " + (totalCov / numRecord));
	}
}
