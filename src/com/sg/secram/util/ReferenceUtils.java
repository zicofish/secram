package com.sg.secram.util;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

import java.io.File;
import java.io.FileNotFoundException;

public class ReferenceUtils {
	public static ReferenceSequenceFile findReferenceFile(String fileName) throws FileNotFoundException{
		ReferenceSequenceFile rsf;

		File refFile = new File(fileName);
	
		try {
			rsf= ReferenceSequenceFileFactory.getReferenceSequenceFile(refFile);
		}
		catch(SAMException ex) {
			System.err.println("Could not load reference sequence file \""+refFile+"\".");
			throw new FileNotFoundException(refFile.toString());
		}
		return rsf;
	}
}
