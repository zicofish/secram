package com.sg.secram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import org.bouncycastle.util.encoders.Base64;

import com.sg.secram.encryption.SECRAMEncryptionFactory;
import com.sg.secram.example.Bam2SecramDir;
import com.sg.secram.example.Bam2SecramFile;
import com.sg.secram.example.Secram2BamFile;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
	public static void main(String[] args) throws Exception {
		ArgumentParser parser = ArgumentParsers
				.newArgumentParser("Main")
				.epilog("Example of use:\n"
						+ "Main keygen -o example.key\n"
						+ "Main bam2secram -k example.key -r example.fa -i example.bam -o example.secram\n"
						+ "Main secram2bam -k example.key -r example.fa -i example.secram -o example.bam\n"
						+ "Main bam2secramDir -k example.key -r example.fa -i exampleFolder");
		parser.addArgument("executable")
				.choices("keygen", "bam2secram", "secram2bam", "bam2secramdDir")
				.help("keygen: generate a symmetric encryption key;\n"
						+ "bam2secram: convert a BAM file to a SECRAM file;\n"
						+ "secram2bam: convert a SECRAM file to a BAM file;\n"
						+ "bam2secramDir: convert all BAM files in a directory to SECRAM files.");

		parser.addArgument("-k", "--keyfile")
				.help("Use encryption with the key in the specified file (the key inside should be base64-encoded.). "
						+ "If you don't want encryption, just don't use this option.");

		parser.addArgument("-r", "--reference").help(
				"Path to the reference sequence file (.fa)");

		parser.addArgument("-i", "--input").help(
				"Path to the input file (or directory for bam2secramDir)");

		parser.addArgument("-o", "--output").help(
				"Path to the output file (not needed for bam2secramDir)");

		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}
		String keyFileName = ns.getString("keyfile");
		byte[] key = null;
		if (keyFileName != null) {
			BufferedReader reader = new BufferedReader(new FileReader(
					keyFileName));
			key = Base64.decode(reader.readLine());
			reader.close();
		}
		if (ns.getString("executable").equals("keygen")) {
			key = SECRAMEncryptionFactory.generateSecret(24);
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					ns.getString("output")));
			writer.write(new String(Base64.encode(key)));
			writer.close();
		} else if (ns.getString("executable").equals("bam2secram")) {
			Bam2SecramFile.bam2secram(ns.getString("input"),
					ns.getString("output"), ns.getString("reference"), key);
		} else if (ns.get("executable").equals("secram2bam")) {
			Secram2BamFile.secram2bam(ns.getString("input"),
					ns.getString("output"), ns.getString("reference"), key);
		} else {
			Bam2SecramDir.convertDirectory(ns.getString("input"),
					ns.getString("reference"), key);
		}

	}
}
