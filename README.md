# CERAM
A library for compressing and encrypting alignment data.

## Requirement
To successfully run the test, please follow the steps below.

Download the human reference genome in the link:

`ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/technical/reference/phase2_reference_assembly_sequence/hs37d5.fa.gz`

Decompress the file:

`gunzip hs37d5.fa.gz`

Create an index file:

`samtools faidx hs37d5.fa`

Put the two files `hs37d5.fa` and `hs37d5.fa.fai` in the folder `data/`.

Run the java class `com.sg.secram.impl.converters.Bam2Secram`.
