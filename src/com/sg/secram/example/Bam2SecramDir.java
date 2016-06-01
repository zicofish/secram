package com.sg.secram.example;

import java.io.File;
import java.io.FilenameFilter;

public class Bam2SecramDir {
	public static void main(String[] args) throws Exception{
		String refFileName = args[0];
		convertDirectory(args[1], refFileName);
//		String refFileName = "/Users/zhihuang/work/LCA1/CTI_Project/tools/data/references/NC_008253_14K.fa";
//		convertDirectory(new File("/Users/zhihuang/work/LCA1/CTI_Project/tools/data/simulated/single/"), refFileName);
//		convertDirectory(new File("/Users/zhihuang/work/LCA1/CTI_Project/tools/data/simulated/paired/"), refFileName);
	}
	
	public static void convertDirectory(String dirName, String refName) throws Exception{
		File dir = new File(dirName);
		File[] bamFiles = dir.listFiles(new BamNameFilter());
		for(File bam : bamFiles){
			String bamName = bam.getAbsolutePath();
			String secramName = bamName.substring(0, bamName.length()-4) + ".secram";
			Bam2SecramFile.bam2secram(bamName, secramName, refName);
		}
	}
	
	private static class BamNameFilter implements FilenameFilter{

		@Override
		public boolean accept(File dir, String name) {
			if(name.endsWith(".bam"))
				return true;
			return false;
		}
		
	}
}
