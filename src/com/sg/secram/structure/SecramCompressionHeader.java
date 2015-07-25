package com.sg.secram.structure;

import htsjdk.samtools.cram.encoding.ExternalCompressor;
import htsjdk.samtools.cram.encoding.NullEncoding;
import htsjdk.samtools.cram.io.ITF8;
import htsjdk.samtools.cram.io.InputStreamUtils;
import htsjdk.samtools.cram.structure.EncodingID;
import htsjdk.samtools.cram.structure.EncodingParams;
import htsjdk.samtools.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sg.secram.compression.SecramEncodingKey;

public class SecramCompressionHeader {
	public Map<SecramEncodingKey, EncodingParams> encodingMap;
    public final Map<Integer, ExternalCompressor> externalCompressors = new HashMap<Integer, ExternalCompressor>();

    public List<Integer> externalIds;
    
    private static Log log = Log.getInstance(SecramCompressionHeader.class);
	
	public SecramCompressionHeader(){
	}
	
	public SecramCompressionHeader(final InputStream inputStream){
	}
	
    public byte[] toByteArray() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        write(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    void write(final OutputStream outputStream) throws IOException {

        { // encoding map:
            int size = 0;
            for (final SecramEncodingKey encodingKey : encodingMap.keySet()) {
                if (encodingMap.get(encodingKey).id != EncodingID.NULL)
                    size++;
            }

            final ByteBuffer mapBuffer = ByteBuffer.allocate(1024 * 100);
            ITF8.writeUnsignedITF8(size, mapBuffer);
            for (final SecramEncodingKey encodingKey : encodingMap.keySet()) {
                if (encodingMap.get(encodingKey).id == EncodingID.NULL)
                    continue;

                mapBuffer.put((byte) encodingKey.name().charAt(0));
                mapBuffer.put((byte) encodingKey.name().charAt(1));

                final EncodingParams params = encodingMap.get(encodingKey);
                mapBuffer.put((byte) (0xFF & params.id.ordinal()));
                ITF8.writeUnsignedITF8(params.params.length, mapBuffer);
                mapBuffer.put(params.params);
            }
            mapBuffer.flip();
            final byte[] mapBytes = new byte[mapBuffer.limit()];
            mapBuffer.get(mapBytes);

            ITF8.writeUnsignedITF8(mapBytes.length, outputStream);
            outputStream.write(mapBytes);
        }
    }
    
    public void read(final byte[] data) {
        try {
            read(new ByteArrayInputStream(data));
        } catch (final IOException e) {
            throw new RuntimeException("This should have never happened.");
        }
    }
    
    void read(final InputStream inputStream) throws IOException{
    	{ // encoding map
    		int byteSize = ITF8.readUnsignedITF8(inputStream);
    		byte[] bytes = new byte[byteSize];
    		InputStreamUtils.readFully(inputStream, bytes, 0, bytes.length);
    		ByteBuffer buffer = ByteBuffer.wrap(bytes);
    		
    		int mapSize = ITF8.readUnsignedITF8(buffer);
    		encodingMap = new TreeMap<SecramEncodingKey, EncodingParams>();
    		for(SecramEncodingKey key : SecramEncodingKey.values())
    			encodingMap.put(key, NullEncoding.toParam());
    		
    		for(int i = 0; i < mapSize; i++){
    			String shortKey = new String(new byte[]{buffer.get(), buffer.get()});
    			SecramEncodingKey encodingKey = SecramEncodingKey.byFirstTwoChars(shortKey);
    			if(null == encodingKey){
    				log.debug("Unknown encoding key: " + shortKey);
                    continue;
    			}
    			EncodingID id = EncodingID.values()[buffer.get()];
    			int paramLen = ITF8.readUnsignedITF8(buffer);
    			byte[] paramBytes = new byte[paramLen];
    			buffer.get(paramBytes);
    			
    			encodingMap.put(encodingKey, new EncodingParams(id, paramBytes));
    			
    			log.debug(String.format("FOUND ENCODING: %s, %s, %s.",
                        encodingKey.name(), id.name(),
                        Arrays.toString(Arrays.copyOf(paramBytes, 20))));
    		}
    	}
    }
}
