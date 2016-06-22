package com.sg.secram.structure;

import java.util.Map;

import com.sg.secram.impl.SECRAMSecurityFilter;

public class SecramContainer {
	public static int DEFATUL_RECORDS_PER_CONTAINER = 100000;
    /**
     * Byte size of the content excluding header.
     */
    public int containerByteSize;
    public int containerID;
    public long containerSalt = 0;
    public long absolutePosStart = -1;
    public long absolutePosEnd = -1;
    public int coverageStart = -1;
    public int qualityLenStart = -1;
    public int nofRecords = -1;
    public long globalRecordCounter = -1;

    public int blockCount = -1;

    /**
     * Container data
     */
    public SecramBlock coreBlock;
    public Map<Integer, SecramBlock> external;

    public SecramCompressionHeader compressionHeader;

    // for performance measurement:
    public long buildHeaderTime;
    public long buildSlicesTime;
    public long writeTime;
    public long parseTime;
    public long readTime;

    // for indexing:
    /**
     * Container start in the stream.
     */
    public long offset;
    
    @Override
    public String toString() {
        return String
                .format("size=%f KBytes, seqID(start)=%d, start=%d, seqID(end)=%d, end=%d, span=%d, records=%d, startingCoverage=%d, blocks=%d, globalRecordCounter=%d.",
                        containerByteSize / 1000.0, (int) (absolutePosStart >> 32), (int)absolutePosStart, (int)(absolutePosEnd >> 32), (int)absolutePosEnd, absolutePosEnd - absolutePosStart,
                        nofRecords, coverageStart, blockCount, globalRecordCounter);
    }
}
