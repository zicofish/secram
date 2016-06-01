# CERAM
A library for compressing and encrypting alignment data.

## Requirement for Running on 1000 Genomes Data
To successfully run the software on public real data, please follow the steps below.

Download the human reference genome in the link:

`ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/technical/reference/phase2_reference_assembly_sequence/hs37d5.fa.gz`

Decompress the file:

`gunzip hs37d5.fa.gz`

Create an index file:

`samtools faidx hs37d5.fa`

Put the two files `hs37d5.fa` and `hs37d5.fa.fai` in the folder `data/`.

Run the java class `com.sg.secram.impl.converters.Bam2Secram`.


## Usage

Show the help menu:

`java -cp ./bin:./lib/* com.sg.secram.Main --help`

Convert a BAM file (`my_bam_file.bam`) to a SECRAM file (`my_secram_file.secram`) by using a reference file (`my_reference.fa`):

`java -cp ./bin:./lib/* com.sg.secram.Main bam2secram -r my_reference.fa -i my_bam_file.bam -o my_secram_file.secram`

Convert a SECRAM file (`my_secram_file.secram`) to a BAM file (`my_bam_file.bam`) by using a reference file (`my_reference.fa`):

`java -cp ./bin:./lib/* com.sg.secram.Main secram2bam -r my_reference.fa -i my_secram_file.secram -o my_bam_file.bam`

