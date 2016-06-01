package com.sg.secram.structure;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;
import htsjdk.samtools.cram.io.LTF8;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A collection of methods to open and close SECRAM files.
 */
public class SecramIO {

    private static final int DEFINITION_LENGTH = SecramHeader.MAGIC.length + 20;
    private static final Log log = Log.getInstance(SecramIO.class);

    private static byte[] bytesFromHex(final String string) {
        final String clean = string.replaceAll("[^0-9a-fA-F]", "");
        if (clean.length() % 2 != 0) throw new RuntimeException("Not a hex string: " + string);
        final byte[] data = new byte[clean.length() / 2];
        for (int i = 0; i < clean.length(); i += 2) {
            data[i / 2] = (Integer.decode("0x" + clean.charAt(i) + clean.charAt(i + 1))).byteValue();
        }
        return data;
    }

    /**
     * Check if the file contains proper SECRAM header
     *
     * @param file the SECRAM file to check
     * @return true if the file is a valid SECRAM file
     * @throws FileNotFoundException 
     * @throws IOException as per java IO contract
     */
    public static boolean checkHeader(final File file) throws FileNotFoundException{
        final SeekableStream seekableStream = new SeekableFileStream(file);
        try{
        	readSecramHeader(seekableStream);
        }
        catch(IOException e){
        	return false;
        }
        return true;
    }

    /**
     * Writes SECRAM header into the specified {@link OutputStream}.
     *
     * @param cramHeader the {@link SecramHeader} object to write
     * @param outputStream         the output stream to write to
     * @return the number of bytes written out
     * @throws IOException as per java IO contract
     */
    public static long writeSecramHeader(final SecramHeader secramHeader, final OutputStream outputStream) throws IOException {
        outputStream.write(SecramHeader.MAGIC);
        outputStream.write(secramHeader.getId());
        for (int i = secramHeader.getId().length; i < 20; i++)
            outputStream.write(0);

        long length = SecramIO.writeSAMFileHeader(secramHeader.getSamFileHeader(), outputStream);
        length += (LTF8.writeUnsignedLTF8(secramHeader.getOpeSalt(), outputStream) + 7) / 8;

        return SecramIO.DEFINITION_LENGTH + length;
    }

    private static SecramHeader readFormatDefinition(final InputStream inputStream) throws IOException {
        for (final byte magicByte : SecramHeader.MAGIC) {
            if (magicByte != inputStream.read()) throw new RuntimeException("Unknown file format.");
        }

        final SecramHeader header = new SecramHeader();

        final DataInputStream dataInputStream = new DataInputStream(inputStream);
        dataInputStream.readFully(header.getId());

        return header;
    }

    /**
     * Read CRAM header from the given {@link InputStream}.
     *
     * @param inputStream input stream to read from
     * @return complete {@link SecramHeader} object
     * @throws IOException as per java IO contract
     */
    public static SecramHeader readSecramHeader(final InputStream inputStream) throws IOException {
        final SecramHeader header = readFormatDefinition(inputStream);

        final SAMFileHeader samFileHeader = readSAMFileHeader(inputStream, new String(header.getId()));
        
        final long opeSalt = LTF8.readUnsignedLTF8(inputStream);

        return new SecramHeader(new String(header.getId()), samFileHeader, opeSalt);
    }

    private static byte[] toByteArray(final SAMFileHeader samFileHeader) {
        final ExposedByteArrayOutputStream headerBodyOS = new ExposedByteArrayOutputStream();
        final OutputStreamWriter outStreamWriter = new OutputStreamWriter(headerBodyOS);
        new SAMTextHeaderCodec().encode(outStreamWriter, samFileHeader);
        try {
            outStreamWriter.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(headerBodyOS.size());
        buf.flip();
        final byte[] bytes = new byte[buf.limit()];
        buf.get(bytes);

        final ByteArrayOutputStream headerOS = new ByteArrayOutputStream();
        try {
            headerOS.write(bytes);
            headerOS.write(headerBodyOS.getBuffer(), 0, headerBodyOS.size());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return headerOS.toByteArray();
    }

    private static long writeSAMFileHeader(final SAMFileHeader samFileHeader, final OutputStream os) throws IOException {
        final byte[] data = toByteArray(samFileHeader);
        final int length = Math.max(1024, data.length + data.length / 2);
        final byte[] blockContent = new byte[length];
        System.arraycopy(data, 0, blockContent, 0, Math.min(data.length, length));
        final SecramBlock block = SecramBlock.buildNewFileHeaderBlock(blockContent);

        final ExposedByteArrayOutputStream byteArrayOutputStream = new ExposedByteArrayOutputStream();
        block.write(byteArrayOutputStream);

        os.write(byteArrayOutputStream.getBuffer(), 0, byteArrayOutputStream.size());

        return byteArrayOutputStream.size();
    }

    private static SAMFileHeader readSAMFileHeader(InputStream inputStream, final String id) throws IOException {
        final SecramBlock block = SecramBlock.readFromInputStream(inputStream);

        inputStream = new ByteArrayInputStream(block.getRawContent());

        final ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 4; i++)
            buffer.put((byte) inputStream.read());
        buffer.flip();
        final int size = buffer.asIntBuffer().get();

        final DataInputStream dataInputStream = new DataInputStream(inputStream);
        final byte[] bytes = new byte[size];
        dataInputStream.readFully(bytes);

        final BufferedLineReader bufferedLineReader = new BufferedLineReader(new ByteArrayInputStream(bytes));
        final SAMTextHeaderCodec codec = new SAMTextHeaderCodec();
        return codec.decode(bufferedLineReader, id);
    }
//
//    /**
//     * Attempt to replace the SAM file header in the CRAM file. This will succeed only if there is sufficient space reserved in the existing
//     * CRAM header. The implementation re-writes the first FILE_HEADER block in the first container of the CRAM file using random file
//     * access.
//     *
//     * @param file      the CRAM file
//     * @param newHeader the new CramHeader container a new SAM file header
//     * @return true if successfully replaced the header, false otherwise
//     * @throws IOException as per java IO contract
//     */
//    public static boolean replaceCramHeader(final File file, final SecramHeader newHeader) throws IOException {
//
//        final CountingInputStream countingInputStream = new CountingInputStream(new FileInputStream(file));
//
//        final SecramHeader header = readFormatDefinition(countingInputStream);
//        final Container c = ContainerIO.readContainerHeader(header.getVersion().major, countingInputStream);
//        final long pos = countingInputStream.getCount();
//        countingInputStream.close();
//
//        final Block block = Block.buildNewFileHeaderBlock(toByteArray(newHeader.getSamFileHeader()));
//        final ExposedByteArrayOutputStream byteArrayOutputStream = new ExposedByteArrayOutputStream();
//        block.write(newHeader.getVersion().major, byteArrayOutputStream);
//        if (byteArrayOutputStream.size() > c.containerByteSize) {
//            log.error("Failed to replace CRAM header because the new header does not fit.");
//            return false;
//        }
//        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
//        randomAccessFile.seek(pos);
//        randomAccessFile.write(byteArrayOutputStream.getBuffer(), 0, byteArrayOutputStream.size());
//        randomAccessFile.close();
//        return true;
//    }
}
