package com.sg.secram.example;

import java.io.File;
import java.io.FilenameFilter;

public class Bam2SecramDir {

	/**
	 * Convert all BAM files (.bam) in a directory to corresponding SECRAM files (.secram), by referring 
	 * to a reference genome file, and using an encryption key.
	 * @param dirName
	 * 				Directory name.
	 * @param refName
	 * 				Reference file name.
	 * @param key
	 * 				Encryption key.
	 */
	public static void convertDirectory(String dirName, String refName,
			byte[] key) {
		File dir = new File(dirName);
		File[] bamFiles = dir.listFiles(new BamNameFilter());
		for (File bam : bamFiles) {
			String bamName = bam.getAbsolutePath();
			String secramName = bamName.substring(0, bamName.length() - 4)
					+ ".secram";
			Bam2SecramFile.bam2secram(bamName, secramName, refName, key);
		}
	}

	private static class BamNameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if (name.endsWith(".bam"))
				return true;
			return false;
		}

	}
}
