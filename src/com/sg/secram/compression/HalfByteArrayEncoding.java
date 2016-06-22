/**
 * ****************************************************************************
 * Copyright 2013 EMBL-EBI
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package com.sg.secram.compression;

import htsjdk.samtools.cram.encoding.BitCodec;
import htsjdk.samtools.cram.encoding.Encoding;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;
import htsjdk.samtools.cram.io.ITF8;
import htsjdk.samtools.cram.structure.EncodingID;
import htsjdk.samtools.cram.structure.EncodingParams;

import java.io.InputStream;
import java.util.Map;

public class HalfByteArrayEncoding implements Encoding<byte[]> {
	//The encodingId and contentId are not used in this class.
    private static final EncodingID encodingId = EncodingID.EXTERNAL;
    private int contentId = -1;

    public HalfByteArrayEncoding() {
    }

    @Override
    public BitCodec<byte[]> buildCodec(final Map<Integer, InputStream> inputMap,
                                     final Map<Integer, ExposedByteArrayOutputStream> outputMap) {
        return new HalfByteArrayCodec();
    }
    
    public static EncodingParams toParam(final int contentId) {
        final HalfByteArrayEncoding halfByteArrayEncoding = new HalfByteArrayEncoding();
        halfByteArrayEncoding.contentId = contentId;
        return new EncodingParams(encodingId, halfByteArrayEncoding.toByteArray());
    }

    @Override
	public byte[] toByteArray() {
        return ITF8.writeUnsignedITF8(contentId);
    }

    @Override
	public void fromByteArray(final byte[] data) {
        contentId = ITF8.readUnsignedITF8(data);
    }
    
    @Override
    public EncodingID id() {
        return encodingId;
    }

}
