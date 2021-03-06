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
