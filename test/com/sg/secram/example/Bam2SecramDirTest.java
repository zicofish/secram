package com.sg.secram.example;

public class Bam2SecramDirTest {
	public static void main(String[] args) throws Exception {
		String refFileName = args[0];
		byte[] key = "SECRET_1SECRET_2SECRET_3".getBytes();
		Bam2SecramDir.convertDirectory(args[1], refFileName, key);
	}
}
