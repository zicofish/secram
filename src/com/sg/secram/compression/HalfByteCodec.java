package com.sg.secram.compression;

import htsjdk.samtools.cram.encoding.AbstractBitCodec;
import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.BitOutputStream;

import java.io.IOException;


public class HalfByteCodec extends AbstractBitCodec<Byte> {

    public HalfByteCodec() {
    }

    @Override
    public Byte read(final BitInputStream bitInputStream) throws IOException {
        return (byte) bitInputStream.readBits(4);
    }

    @Override
    public long write(final BitOutputStream bitOutputStream, final Byte object) throws IOException {
        bitOutputStream.write(object, 4);
        return 4;
    }

    @Override
    public long numberOfBits(final Byte object) {
        return 4;
    }

    @Override
    public Byte read(final BitInputStream bitInputStream, final int length) throws IOException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public void readInto(final BitInputStream bitInputStream, final byte[] array, final int offset,
                         final int valueLen) throws IOException {
    	throw new RuntimeException("Not implemented.");
    }
}
