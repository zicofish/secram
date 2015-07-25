package com.sg.secram.compression;

/**
 * Data series types known to SECRAM.
 */
public enum Deprecated_SecramDataSeriesType {

	/**
	 * Half a byte (16 distinct values)
	 */
	HALF_BYTE,
	/**
	 * An array of HALF_BYTEs
	 */
	HALF_BYTE_ARRAY,
    /**
     * A single signed byte (256 distinct values)
     */
    BYTE,
    /**
     * A signed integer ~4 billions of them.
     */
    INT,
    /**
     * A signed long value, 64 bits, too many to count.
     */
    LONG,
    /**
     * An array of bytes.
     */
    BYTE_ARRAY
}
