package com.sg.secram.compression;

public enum SecramEncodingKey {
	AP_AbsolutePosition, NH_NumberOfReadHeaders, RL_ReferenceLength,  
	MQ_MappingQualityScore, RN_ReadName, FG_Flag, TL_TemplateLength, NP_NextAbsolutePosition, TG_tags,
	SF_SensitiveField, CV_Coverage, NF_NumberOfFeatures, FO_FeatureOrder, FC_FeatureCode, FL_FeatureLength, FB_FeatureBase,
	QS_QualityScore, QL_QualityScoreLength;
	
	public static SecramEncodingKey byFirstTwoChars(final String chars){
		for (final SecramEncodingKey encodingKey : values()) {
            if (encodingKey.name().startsWith(chars))
                return encodingKey;
        }
        return null;
	}
	
	public static byte[] toTwoBytes(final SecramEncodingKey key) {
		return new byte[]{(byte) key.name().charAt(0), (byte) key.name().charAt(1)};
	}
}
