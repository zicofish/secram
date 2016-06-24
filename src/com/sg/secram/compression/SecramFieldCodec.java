package com.sg.secram.compression;

import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.BitOutputStream;

import java.io.IOException;

public interface SecramFieldCodec<T> {
	void setBitInputStream(BitInputStream bitInputStream);

	void setBitOutputStream(BitOutputStream bitOutputStream);

	long writeField(T value) throws IOException;

	T readField() throws IOException;

	T readArrayField(int length) throws IOException;
}
