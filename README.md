# SECRAM
SECRAM is a library for compressing, encrypting, and querying alignment data. The library is designed to achieve three important features: high security/privacy, low storage cost, fast random access. SECRAM v1.0 consists of the following:
  * A converter from BAM file to SECRAM file
  * A converter from SECRAM file to BAM file
  * A basic query API for random access of SECRAM file

## System requirement
This library is tested on:
  * Java version 1.8.0_45
  * Mac OS X Yosemite version 10.10.5

## Basic usage
Some example uses of the library are in the package `com.sg.secram.example` (both the `src` and `test` folder.). There are also several basic functionalities provided in the class `com.sg.secram.Main`. Run the `Main` class with argument:

  * `--help`: Show the help menu.
  * `keygen -o my.key`: Generate a master key for the encryption/decryption.
  * `bam2secram -k my.key -r my_reference.fa -i my_bam_file.bam -o my_secram_file.secram`: Convert the BAM file (`my_bam_file.bam`) to the SECRAM file (`my_secram_file.secram`) by using the reference file (`my_reference.fa`) and the master key (`my.key`).
  * `secram2bam -k my.key -r my_reference.fa -i my_secram_file.secram -o my_bam_file.bam`: Convert the SECRAM file (`my_secram_file.secram`) to the BAM file (`my_bam_file.bam`) by using the reference file (`my_reference.fa`) and the master key (`my.key`).

## Requirement when running with reference genome GRCh37
To successfully run the code with reference genome GRCh37, please follow the steps below.

Download the human reference genome in the link:

`ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/technical/reference/phase2_reference_assembly_sequence/hs37d5.fa.gz`

Decompress the file:

`gunzip hs37d5.fa.gz`

Create an index file:

`samtools faidx hs37d5.fa`

Put the two files `hs37d5.fa` and `hs37d5.fa.fai` in the folder `data/`.


## Releases
  * Version 1.0

## Contact
Zhicong Huang (zhicong.huang@epfl.ch)

