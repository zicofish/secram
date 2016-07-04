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
package com.sg.secram.compression;

/**
 * Rescale the quality score from [0, 63] to [0, 15]. Any quality score that is bigger
 * than 63 will be rescaled to 15. After rescaling, we only need 4 bits to represent a score.
 * @author zhihuang
 *
 */
public class LossyQualityScore {

	/**
	 * @param scores
	 *            Original scores in the range [0, 63]
	 * @return Lossy scores in the range [0, 15]
	 */
	public static byte[] toLossyQS(byte[] scores) {
		byte[] lowResolutionScores = new byte[scores.length];
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > 63)
				lowResolutionScores[i] = 15;
			else
				lowResolutionScores[i] = (byte) (scores[i] / 4);
		}
		return lowResolutionScores;
	}

	/**
	 * @param scores
	 *            Original scores in the range [0, 63]
	 * @return Array of lossy scores in the range [0, 15]. Each 8-bit original
	 *         score is converted to 4-bit lossy score, and then every two lossy
	 *         scores are packed into one byte.
	 */
	public static byte[] packQS(byte[] scores) {
		byte[] lowResolutionScores = toLossyQS(scores);
		byte[] compactScores = new byte[(lowResolutionScores.length + 1) / 2];
		for (int i = 0; i < lowResolutionScores.length; i++) {
			if (i % 2 == 0)
				compactScores[i / 2] = (byte) (lowResolutionScores[i] & 0x0F);
			else
				compactScores[i / 2] = (byte) (compactScores[i / 2] | (lowResolutionScores[i] << 4));
		}
		return compactScores;
	}

	/**
	 * @param lossyScores
	 *            Lossy scores in the range [0, 15]
	 * @return Scores in the original resolution. They are still lossy: a score
	 *         i is just converted to 4*i, to be compatible with the original
	 *         resolution in the range [0, 63].
	 */
	public static byte[] toOriginalResolutionQS(byte[] lossyScores) {
		byte[] originalResolutionScores = new byte[lossyScores.length];
		for (int i = 0; i < lossyScores.length; i++) {
			originalResolutionScores[i] = (byte) (lossyScores[i] * 4);
		}
		return originalResolutionScores;
	}

	/**
	 * @param compactScores
	 *            Array of compact scores. Each compact score is 4 bits.
	 * @return Array of 8-bit scores in the original resolution. A 4-bit compact
	 *         score is multiplied by 4 in order to convert to a 8-bit score.
	 */
	public static byte[] unpackQS(byte[] compactScores) {
		return unpackQS(compactScores, compactScores.length * 2);
	}

	/**
	 * @param compactScores
	 *            Array of compact scores. Each compact score is 4 bits.
	 * @param length
	 *            Number of scores. This is necessary because when the number is
	 *            odd, one compact score should be dropped.
	 * @return Array of 8-bit scores in the original resolution. A 4-bit compact
	 *         score is multiplied by 4 in order to convert to a 8-bit score.
	 */
	public static byte[] unpackQS(byte[] compactScores, int length) {
		if (length != compactScores.length * 2
				&& length != (compactScores.length * 2 - 1)) {
			throw new IllegalArgumentException(
					"The size "
							+ compactScores.length
							+ " of input quality cores does not match the specified length "
							+ length);
		}
		byte[] lowResolutionScores = new byte[compactScores.length * 2];
		for (int i = 0; i < compactScores.length; i++) {
			lowResolutionScores[i] = (byte) (compactScores[i] & 0x0F);
			lowResolutionScores[i + 1] = (byte) ((compactScores[i] >> 4) & 0x0F);
		}
		return toOriginalResolutionQS(lowResolutionScores);
	}
}
