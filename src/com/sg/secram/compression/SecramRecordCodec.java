package com.sg.secram.compression;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import com.sg.secram.impl.records.PosCigarFeature;
import com.sg.secram.impl.records.PosCigarFeatureCode;
import com.sg.secram.impl.records.ReadHeader;
import com.sg.secram.impl.records.SecramRecord;

import htsjdk.samtools.cram.encoding.DataSeriesType;
import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;

/**
 * Codec for serializing / deserializig a SECRAM record.
 * <p>
 * See also:
 * <ul>
 * <li>{@link com.sg.secram.structure.SecramCompressionHeaderFactory} for defining the encoding strategy of each field.</li>
 * <li>{@link SecramRecordCodecFactory} for creating codecs of all fields.</li>
 * </ul>
 * @author zhihuang
 *
 */
public class SecramRecordCodec {
	private Charset charset = Charset.forName("UTF8");
	/**
	 * Previous record, used for relative integer encoding. For some fields, the value of a record 
	 * is close to the value of its previous record, hence we could only encode the difference 
	 * (a small integer) for the current record.
	 */
	private SecramRecord prevRecord;
	/**
	 * Whether we use lossy quality scores.
	 */
	private boolean lossyQual;

	/**
	 * External codec.
	 */
	@SecramDataSeries(key = SecramEncodingKey.AP_AbsolutePosition, type = DataSeriesType.LONG)
	public SecramFieldCodec<Long> absolutePositionCodec;

	/**
	 * External codec.
	 */
	@SecramDataSeries(key = SecramEncodingKey.NH_NumberOfReadHeaders, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> numberOfReadHeadersCodec;

	/**
	 * Non-external codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.RL_ReferenceLength, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> referenceLengthCodec;

	/**
	 * Non-external codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.MQ_MappingQualityScore, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> mappingQualityCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.RN_ReadName, type = DataSeriesType.BYTE_ARRAY)
	public SecramFieldCodec<byte[]> readNameCodec;

	/**
	 * Non-external codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.FG_Flag, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> flagCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.TL_TemplateLength, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> templateLengthCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.NP_NextAbsolutePosition, type = DataSeriesType.LONG)
	public SecramFieldCodec<Long> nextAbsolutePositionCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.TG_tags, type = DataSeriesType.BYTE_ARRAY)
	public SecramFieldCodec<byte[]> tagsCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.QS_QualityScore, type = DataSeriesType.BYTE_ARRAY)
	public SecramFieldCodec<byte[]> qualityScoreCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.QL_QualityScoreLength, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> qualityScoreLengthCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.CV_Coverage, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> coverageCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.NF_NumberOfFeatures, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> numberOfFeaturesCodec;

	/**
	 * External codec
	 */
	@SecramDataSeries(key = SecramEncodingKey.SF_SensitiveField, type = DataSeriesType.BYTE_ARRAY)
	public SecramFieldCodec<byte[]> sensitiveFieldCodec;

	// Here begins the sub fields contained in sensitive field;

	/**
	 * Non-external codec (but after encoding, the content is extracted 
	 * to put into the external codec: sensitiveFieldCodec.)
	 */
	@SecramDataSeries(key = SecramEncodingKey.FO_FeatureOrder, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> featureOrderCodec;

	/**
	 * Non-external codec (but after encoding, the content is extracted 
	 * to put into the external codec: sensitiveFieldCodec.)
	 */
	@SecramDataSeries(key = SecramEncodingKey.FC_FeatureCode, type = DataSeriesType.BYTE)
	public SecramFieldCodec<Byte> featureCodeCodec;

	/**
	 * Non-external codec (but after encoding, the content is extracted 
	 * to put into the external codec: sensitiveFieldCodec.)
	 */
	@SecramDataSeries(key = SecramEncodingKey.FL_FeatureLength, type = DataSeriesType.INT)
	public SecramFieldCodec<Integer> featureLengthCodec;
	

	/**
	 * Construct the record codec, and specify whether we should use lossy quality scores
	 * @param lossyQual Whether we should use lossy quality scores
	 */
	public SecramRecordCodec(boolean lossyQual) {
		this.lossyQual = lossyQual;
	}

	/**
	 * Serialize this record to the output streams of different field codecs.
	 * @param record The record to be seriazlied.
	 * @throws IOException
	 */
	public void write(SecramRecord record) throws IOException {
		absolutePositionCodec.writeField(record.absolutePositionDelta);
		numberOfReadHeadersCodec.writeField(record.mReadHeaders.size());
		for (ReadHeader rh : record.mReadHeaders) {
			referenceLengthCodec.writeField(rh.mReferenceLength);
			mappingQualityCodec.writeField(rh.mMappingQuality);
			readNameCodec.writeField(rh.mReadName.getBytes(charset));
			flagCodec.writeField(rh.mFlags);
			templateLengthCodec.writeField(rh.mTemplateLength);
			long tmp = rh.getNextAbsolutePosition()
					- record.getAbsolutePosition();
			nextAbsolutePositionCodec.writeField(tmp);
			tagsCodec.writeField(rh.mTags);
		}
		qualityScoreLengthCodec.writeField(record.qualityLenDelta);
		if (!lossyQual)
			qualityScoreCodec.writeField(record.mQualityScores);
		else
			qualityScoreCodec.writeField(LossyQualityScore
					.packQS(record.mQualityScores));

		coverageCodec.writeField(record.coverageDelta);
		List<PosCigarFeature> features = record.mPosCigar.getNonMatchFeatures();
		numberOfFeaturesCodec.writeField(features.size());

		{ // encode the sensitive field
			ExposedByteArrayOutputStream bitBAOS = new ExposedByteArrayOutputStream();
			DefaultBitOutputStream bitOutputStream = new DefaultBitOutputStream(
					bitBAOS);

			featureOrderCodec.setBitOutputStream(bitOutputStream);
			featureCodeCodec.setBitOutputStream(bitOutputStream);
			featureLengthCodec.setBitOutputStream(bitOutputStream);

			for (PosCigarFeature f : features) {
				featureOrderCodec.writeField(f.mOrder);
				featureCodeCodec.writeField((byte) f.mOP.getCharacter());
				switch (f.mOP) {
				case F:
				case I:
				case S:
				case R:
					featureLengthCodec.writeField(f.mLength);
					for (byte b : BaseHalfByteMap
							.baseArray2HalfByteArray(f.mBases.getBytes()))
						bitOutputStream.write(b, 4);
					break;
				case G:
				case O:
				case H:
				case P:
					featureLengthCodec.writeField(f.mLength);
					break;
				case X:
					bitOutputStream.write(BaseHalfByteMap.base2HalfByteMap
							.get(f.mBases.getBytes()[0]), 4);
					break;
				case D:
				case N:
				case M:
					break;
				}
			}
			bitOutputStream.close();

			sensitiveFieldCodec.writeField(bitBAOS.toByteArray());

		}
	}

	/**
	 * Deserialize a record from the input streams of different field codecs.
	 * @param record The record where we store the deserialized fields.
	 * @throws IOException
	 */
	public void read(final SecramRecord record) throws IOException {
		record.absolutePositionDelta = absolutePositionCodec.readField();
		long absPos = prevRecord.getAbsolutePosition()
				+ record.absolutePositionDelta;
		record.setAbsolutionPosition(absPos);
		int numberOfReadHeaders = numberOfReadHeadersCodec.readField();
		for (int i = 0; i < numberOfReadHeaders; i++) {
			ReadHeader rh = new ReadHeader();
			rh.mReferenceLength = referenceLengthCodec.readField();
			rh.mMappingQuality = mappingQualityCodec.readField();
			rh.mReadName = new String(readNameCodec.readField(), charset);
			rh.mFlags = flagCodec.readField();
			rh.mTemplateLength = templateLengthCodec.readField();
			long tmp = nextAbsolutePositionCodec.readField();
			rh.setNextAbsolutionPosition(tmp + record.getAbsolutePosition());
			rh.mTags = tagsCodec.readField();

			record.mReadHeaders.add(rh);
		}
		record.qualityLenDelta = qualityScoreLengthCodec.readField();
		int qualLen = record.qualityLenDelta + prevRecord.mQualityScores.length;
		if (!lossyQual)
			record.mQualityScores = qualityScoreCodec.readArrayField(qualLen);
		else
			record.mQualityScores = LossyQualityScore.unpackQS(
					qualityScoreCodec.readArrayField((qualLen + 1) / 2),
					qualLen);

		record.coverageDelta = coverageCodec.readField();
		record.mPosCigar.mCoverage = prevRecord.mPosCigar.mCoverage
				+ record.coverageDelta;

		int numberOfFeatures = numberOfFeaturesCodec.readField();
		byte[] sensitiveField = sensitiveFieldCodec.readField();
		{// decode the sensitive field
			ByteArrayInputStream bai = new ByteArrayInputStream(sensitiveField);
			DefaultBitInputStream bitInputStream = new DefaultBitInputStream(
					bai);
			featureOrderCodec.setBitInputStream(bitInputStream);
			featureCodeCodec.setBitInputStream(bitInputStream);
			featureLengthCodec.setBitInputStream(bitInputStream);

			List<PosCigarFeature> features = new LinkedList<PosCigarFeature>();;
			int currentOrder = -1;
			for (int i = 0; i < numberOfFeatures; i++) {
				int order = featureOrderCodec.readField();
				byte code = featureCodeCodec.readField();
				int length = 0;
				String bases = "";
				PosCigarFeatureCode op = PosCigarFeatureCode
						.getOperator((char) code);
				switch (op) {
				case F:
				case I:
				case S:
				case R:
					length = featureLengthCodec.readField();
					byte[] halfByteArray = new byte[length];
					for (int j = 0; j < length; j++)
						halfByteArray[j] = (byte) bitInputStream.readBits(4);
					bases = new String(
							BaseHalfByteMap
									.halfByteArray2BaseArray(halfByteArray));
					break;
				case G:
				case O:
				case H:
				case P:
					length = featureLengthCodec.readField();
					break;
				case X:
					byte b = BaseHalfByteMap.halfByte2BaseArray[bitInputStream
							.readBits(4)];
					length = 1;
					bases = new String(new byte[] { b });
					break;
				case D:
				case N:
					length = 1;
					break;
				case M:
					throw new IOException(
							"Impossible to read a 'M' operator from a secram file.");
				}

				if (order != currentOrder) {
					if (currentOrder >= 0){
						record.mPosCigar.setNonMatchFeaturesForRead(
								currentOrder, features);
					}
					features = new LinkedList<PosCigarFeature>();
					currentOrder = order;
				}
				PosCigarFeature pcf = new PosCigarFeature(order, (char) code,
						length, bases);
				features.add(pcf);
			}
			if (! features.isEmpty())
				record.mPosCigar.setNonMatchFeaturesForRead(currentOrder,
						features);

		}

		prevRecord = record;
	}

	/**
	 * Set the previous record for relative encoding.
	 * @param prevRecord Previous record.
	 */
	public void setPrevRecord(SecramRecord prevRecord) {
		this.prevRecord = prevRecord;
	}
}
