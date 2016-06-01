package com.sg.secram;

import com.sg.secram.example.Bam2SecramDir;
import com.sg.secram.example.Bam2SecramFile;
import com.sg.secram.example.Secram2BamFile;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
	public static void main(String[] args) throws Exception{
		ArgumentParser parser = ArgumentParsers.newArgumentParser("Main")
				.epilog("Example of use:\n"
						+ "Main bam2secram -r example.fa -i example.bam -o example.secram\n"
						+ "Main secram2bam -r example.fa -i example.secram -o example.bam\n"
						+ "Main bam2secramDir -r example.fa -i exampleFolder");
		parser.addArgument("executable").choices("bam2secram", "secram2bam", "bam2secramdDir")
		.help("bam2secram: convert a BAM file to a CERAM file;\n"
				+ "secram2bam: convert a CERAM file to a BAM file;\n"
				+ "bam2secramDir: convert all BAM files in a directory to CERAM files.");
		
		parser.addArgument("-r", "--reference").required(true)
		.help("Path to the reference sequence file (.fa)");
		
		parser.addArgument("-i", "--input").required(true)
		.help("Path to the input file (or directory for bam2secramDir)");
		
		parser.addArgument("-o", "--output")
		.help("Path to the output file (not needed for bam2secramDir)");
		
		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}
		
		if(ns.getString("executable").equals("bam2secram")){
			Bam2SecramFile.bam2secram(ns.getString("input"), ns.getString("output"), ns.getString("reference"));
		}
		else if(ns.get("executable").equals("secram2bam")){
			Secram2BamFile.secram2bam(ns.getString("input"), ns.getString("output"), ns.getString("reference"));
		}
		else{
			Bam2SecramDir.convertDirectory(ns.getString("input"), ns.getString("reference"));
		}
		
	}
}
