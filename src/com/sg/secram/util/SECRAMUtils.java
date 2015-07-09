package com.sg.secram.util;

import java.nio.ByteBuffer;

public class SECRAMUtils {

	
	
	private static String formatTime(int value) {
		return value<10?"0"+value:""+value;
	}
	public static String timeString(long time) {
		int sec = (int)(time/1000);
		int min = sec/60;
		int hours = min/60;
		min %=60;
		sec %= 60;
		return formatTime(hours)+":"+formatTime(min)+":"+formatTime(sec);
	}
	
	public static long getPosition(int position, int refSequence) {
		long result = refSequence;
		result <<= 32;
		result |= position;
		
		return result;
	}
	
	public static byte[] longToBytes(long l){
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
		buf.putLong(l);
		return buf.array();
	}
}
