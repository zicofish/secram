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

import java.util.HashMap;

/**
 * A utility class defining the conversion between a base character and its integer encoding in 4 bits, for example:
 * <ul>
 * <li>'A' <-> 1</li>
 * <li>'C' <-> 2</li>
 * <li>'G' <-> 4</li>
 * <li>'T' <-> 8</li>
 * </ul>
 * There are some other rarely-used characters in BAM files. They are also preserved in SECRAM.
 * @author zhihuang
 *
 */
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
