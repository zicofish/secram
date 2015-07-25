package com.sg.secram.compression;

public class LossyQualityScore {
	//rescale the quality score from [0, 63] (any quality score that is bigger than 63 will be treated like 63) 
	//to [0, 15]
	
	public static byte[] toLossyQS(byte[] scores){
		byte[] lowResolutionScores = new byte[scores.length];
		for(int i = 0; i < scores.length; i++){
			if(scores[i] > 63)
				lowResolutionScores[i] = 15;
			else
				lowResolutionScores[i] = (byte)(scores[i] / 4);
		}
		return lowResolutionScores;
	}
	
	public static byte[] packQS(byte[] scores){
		byte[] lowResolutionScores = toLossyQS(scores);
		byte[] compactScores = new byte[(lowResolutionScores.length + 1) / 2];
		for(int i = 0; i < lowResolutionScores.length; i++){
			if(i % 2 == 0)
				compactScores[i/2] = (byte)(lowResolutionScores[i] & 0x0F);
			else
				compactScores[i/2] = (byte)(compactScores[i/2] | (lowResolutionScores[i] << 4));
		}
		return compactScores;
	}
	
	public static byte[] toOriginalResolutionQS(byte[] lossyScores){
		byte[] originalResolutionScores = new byte[lossyScores.length];
		for(int i = 0; i < lossyScores.length; i++){
			originalResolutionScores[i] = (byte) (lossyScores[i] * 4);
		}
		return originalResolutionScores;
 	}
	
	public static byte[] unpackQS(byte[] compactScores){
		return unpackQS(compactScores, compactScores.length * 2);
	}
	
	public static byte[] unpackQS(byte[] compactScores, int length){
		if(length != compactScores.length * 2 && length != (compactScores.length * 2 - 1)){
			throw new IllegalArgumentException("The size " + compactScores.length + " of input quality cores does not match the specified length " + length);
		}
		byte[] lowResolutionScores = new byte[compactScores.length * 2];
		for(int i = 0; i < compactScores.length; i++){
			lowResolutionScores[i] = (byte) (compactScores[i] & 0x0F);
			lowResolutionScores[i + 1] = (byte) ((compactScores[i] >> 4) & 0x0F);
		}
		return toOriginalResolutionQS(lowResolutionScores);
	}
}
