package com.sg.secram.impl;

/**
 * Constants used in reading & writing SECRAM files.
 * @author zhihuang
 */
public class SECRAMFileConstants {

	/**
	 * SECRAM file magic number.
	 */

	static final byte[] SECRAM_MAGIC = "SEC\1".getBytes();
	/**
	 * SECRAM index file magic number.
	 */
	static final byte[] SECRAN_INDEX_MAGIC = "SEI\1".getBytes();
}
