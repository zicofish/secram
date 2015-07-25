package com.sg.secram.compression;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import htsjdk.samtools.cram.encoding.DataSeriesType;

/**
 * An annotation to denote a data series field in a java class.
 * Some data can be represented as a set of column (data series) where
 * each column is characterized by it's intention ({@link com.sg.secram.compression.SecramEncodingKey} for SECRAM)
 * and it's data type, like {@link java.lang.Integer}or {@link java.lang.String}.
 * Annotating fields in a class with this annotation allows for automated discovery of such column (data series)
 * and attaching specific codec to serialise/deserialize data.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecramDataSeries {
    /**
     * One of the pre-defined SECRAM data series names
     * @return SECRAM data series name (key)
     */
    SecramEncodingKey key();

    /**
     * Data type of the series.
     * @return data type of the series
     */
    DataSeriesType type();
}
