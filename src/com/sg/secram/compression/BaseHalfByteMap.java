package com.sg.secram.compression;

import java.util.HashMap;

public class BaseHalfByteMap {
	public static HashMap<Byte, Byte> base2HalfByteMap = new HashMap<Byte, Byte>();
	static {
		base2HalfByteMap.put((byte) '=', (byte) 0);
		base2HalfByteMap.put((byte) 'A', (byte) 1);
		base2HalfByteMap.put((byte) 'C', (byte) 2);
		base2HalfByteMap.put((byte) 'M', (byte) 3);
		base2HalfByteMap.put((byte) 'G', (byte) 4);
		base2HalfByteMap.put((byte) 'R', (byte) 5);
		base2HalfByteMap.put((byte) 'S', (byte) 6);
		base2HalfByteMap.put((byte) 'V', (byte) 7);
		base2HalfByteMap.put((byte) 'T', (byte) 8);
		base2HalfByteMap.put((byte) 'W', (byte) 9);
		base2HalfByteMap.put((byte) 'Y', (byte) 10);
		base2HalfByteMap.put((byte) 'H', (byte) 11);
		base2HalfByteMap.put((byte) 'K', (byte) 12);
		base2HalfByteMap.put((byte) 'D', (byte) 13);
		base2HalfByteMap.put((byte) 'B', (byte) 14);
		base2HalfByteMap.put((byte) 'N', (byte) 15);
	}
	public static byte[] halfByte2BaseArray = new byte[] { (byte) '=',
			(byte) 'A', (byte) 'C', (byte) 'M', (byte) 'G', (byte) 'R',
			(byte) 'S', (byte) 'V', (byte) 'T', (byte) 'W', (byte) 'Y',
			(byte) 'H', (byte) 'K', (byte) 'D', (byte) 'B', (byte) 'N' };

	public static byte[] baseArray2HalfByteArray(byte[] baseArray) {
		byte[] halfByteArray = new byte[baseArray.length];
		for (int i = 0; i < baseArray.length; i++)
			halfByteArray[i] = base2HalfByteMap.get(baseArray[i]);
		return halfByteArray;
	}

	public static byte[] halfByteArray2BaseArray(byte[] halfByteArray) {
		byte[] baseArray = new byte[halfByteArray.length];
		for (int i = 0; i < halfByteArray.length; i++)
			baseArray[i] = halfByte2BaseArray[halfByteArray[i]];
		return baseArray;
	}
}
