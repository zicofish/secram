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
 * Enumeration of all fields for the encoding of a SECRAM record. These fields are:
 * <ul>
 * <li>AP_AbsolutePosition</li>
 * <li>NH_NumberOfReadHeaders</li>
 * <li>RL_ReferenceLength</li>
 * <li>MQ_MappingQualityScore</li>
 * <li>RN_ReadName</li>
 * <li>FG_Flag</li>
 * <li>TL_TemplateLength</li>
 * <li>NP_NextAbsolutePosition</li>
 * <li>TG_tags</li>
 * <li>SF_SensitiveField</li>
 * <li>CV_Coverage</li>
 * <li>NF_NumberOfFeatures</li>
 * <li>FO_FeatureOrder</li>
 * <li>FC_FeatureCode</li>
 * <li>FL_FeatureLength</li>
 * <li>QS_QualityScore</li>
 * <li>QL_QualityScoreLength</li>
 * @author zhihuang
 *
 */
public enum SecramEncodingKey {
	AP_AbsolutePosition, NH_NumberOfReadHeaders, RL_ReferenceLength, 
	MQ_MappingQualityScore, RN_ReadName, FG_Flag, TL_TemplateLength, 
	NP_NextAbsolutePosition, TG_tags, SF_SensitiveField, CV_Coverage, 
	NF_NumberOfFeatures, FO_FeatureOrder, FC_FeatureCode, 
	FL_FeatureLength, QS_QualityScore, QL_QualityScoreLength;

	/**
	 * Get the encoding key by the two-character short name.
	 * @param chars Short name of the key.
	 * @return The encoding key.
	 */
	public static SecramEncodingKey byFirstTwoChars(final String chars) {
		for (final SecramEncodingKey encodingKey : values()) {
			if (encodingKey.name().startsWith(chars))
				return encodingKey;
		}
		return null;
	}

	/**
	 * Convert a key to its short name.
	 * @param key An Encoding key.
	 * @return The short name of the key.
	 */
	public static byte[] toTwoBytes(final SecramEncodingKey key) {
		return new byte[] { (byte) key.name().charAt(0),
				(byte) key.name().charAt(1) };
	}
}
