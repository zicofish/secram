/**
 * Copyright © 2013-2016 Swiss Federal Institute of Technology EPFL and Sophia Genetics SA
 * 
 * All rights reserved
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials provided 
 * with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used 
 * to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * PATENTS NOTICE: Sophia Genetics SA holds worldwide pending patent applications in relation with this 
 * software functionality. For more information and licensing conditions, you should contact Sophia Genetics SA 
 * at info@sophiagenetics.com. 
 */
package com.sg.secram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.util.encoders.Base64;

import com.sg.secram.encryption.SECRAMEncryptionFactory;
import com.sg.secram.example.Bam2SecramDir;
import com.sg.secram.example.Bam2SecramFile;
import com.sg.secram.example.Secram2BamFile;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Define entry points for some functionalities of this library:
 * <ul>
 * <li>Generate a key</li>
 * <li>Convert a BAM file to a SECRAM file</li>
 * <li>
 * Convert a SECRAM file to a BAM file</li>
 * <li>
 * Convert all BAM files in a directory to SECRAM files.</li>
 * </ul>
 * Run this class with option '--help' to see the help menu.
 * 
 * @author zhihuang
 *
 */
public class Main {
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	
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
		else{
			logger.log(Level.WARNING, "You are converting to a SECRAM file WITHOUT encryption. "
					+ "Please protect your data by specifying a key with the option --keyfile.");
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
