package com.sg.secram.util;

import java.util.Arrays;

public class Timings {
	public static long bam2secramConversion = 0;
	public static long transposition = 0;
	public static long compression = 0;
	public static long encryption = 0;
	
	public static long secram2bamConversion = 0;
	public static long invTransposition = 0;
	public static long decompression = 0;
	public static long decryption = 0;
	
	public static long retrieval = 0;
	public static long communication = 0;
	public static long locateQueryPosition = 0;
	public static long queryProcessing = 0;
	public static long IO = 0;
	
	public static void printTimings() {
		Arrays.stream(Timings.class.getFields())
			.forEach((x) ->{
				try {
					System.out.println(x.getName() + ": " + x.getLong(null));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
	}
	
	public static void reset(){
		Arrays.stream(Timings.class.getFields())
		.forEach((x) ->{
			try {
				x.setLong(null, 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
}
