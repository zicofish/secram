cramtools="./cramtools-2.1.jar"
prefix="./miniCaviar_IDT_NEB.runA.NA12878.bwa.chrom1"
reference="./hs37d5.fa"
cramFileName="miniCaviar_IDT_NEB.runA.NA12878.bwa.chrom1.cram"
java -jar $cramtools cram --capture-all-tags -Q -n \
				--input-bam-file $prefix.bam --reference-fasta-file $reference \
				--output-cram-file $cramFileName