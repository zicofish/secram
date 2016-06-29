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
