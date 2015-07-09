/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.sg.secram.avro;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class SecramRecordAvro extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SecramRecordAvro\",\"namespace\":\"com.sg.secram.avro\",\"fields\":[{\"name\":\"POS\",\"type\":\"long\"},{\"name\":\"readHeaders\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"ReadHeaderAvro\",\"fields\":[{\"name\":\"Order\",\"type\":\"int\"},{\"name\":\"Ref_len\",\"type\":\"int\"},{\"name\":\"Next_POS\",\"type\":\"long\"},{\"name\":\"Bin_mq_nl\",\"type\":\"int\"},{\"name\":\"Read_Name\",\"type\":\"string\"},{\"name\":\"Flag\",\"type\":\"int\"},{\"name\":\"TLen\",\"type\":\"int\"},{\"name\":\"Tags\",\"type\":\"bytes\"}]}}},{\"name\":\"PosCigar\",\"type\":\"bytes\"},{\"name\":\"Qual\",\"type\":\"bytes\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public long POS;
  @Deprecated public java.util.List<com.sg.secram.avro.ReadHeaderAvro> readHeaders;
  @Deprecated public java.nio.ByteBuffer PosCigar;
  @Deprecated public java.nio.ByteBuffer Qual;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public SecramRecordAvro() {}

  /**
   * All-args constructor.
   */
  public SecramRecordAvro(java.lang.Long POS, java.util.List<com.sg.secram.avro.ReadHeaderAvro> readHeaders, java.nio.ByteBuffer PosCigar, java.nio.ByteBuffer Qual) {
    this.POS = POS;
    this.readHeaders = readHeaders;
    this.PosCigar = PosCigar;
    this.Qual = Qual;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return POS;
    case 1: return readHeaders;
    case 2: return PosCigar;
    case 3: return Qual;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: POS = (java.lang.Long)value$; break;
    case 1: readHeaders = (java.util.List<com.sg.secram.avro.ReadHeaderAvro>)value$; break;
    case 2: PosCigar = (java.nio.ByteBuffer)value$; break;
    case 3: Qual = (java.nio.ByteBuffer)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'POS' field.
   */
  public java.lang.Long getPOS() {
    return POS;
  }

  /**
   * Sets the value of the 'POS' field.
   * @param value the value to set.
   */
  public void setPOS(java.lang.Long value) {
    this.POS = value;
  }

  /**
   * Gets the value of the 'readHeaders' field.
   */
  public java.util.List<com.sg.secram.avro.ReadHeaderAvro> getReadHeaders() {
    return readHeaders;
  }

  /**
   * Sets the value of the 'readHeaders' field.
   * @param value the value to set.
   */
  public void setReadHeaders(java.util.List<com.sg.secram.avro.ReadHeaderAvro> value) {
    this.readHeaders = value;
  }

  /**
   * Gets the value of the 'PosCigar' field.
   */
  public java.nio.ByteBuffer getPosCigar() {
    return PosCigar;
  }

  /**
   * Sets the value of the 'PosCigar' field.
   * @param value the value to set.
   */
  public void setPosCigar(java.nio.ByteBuffer value) {
    this.PosCigar = value;
  }

  /**
   * Gets the value of the 'Qual' field.
   */
  public java.nio.ByteBuffer getQual() {
    return Qual;
  }

  /**
   * Sets the value of the 'Qual' field.
   * @param value the value to set.
   */
  public void setQual(java.nio.ByteBuffer value) {
    this.Qual = value;
  }

  /** Creates a new SecramRecordAvro RecordBuilder */
  public static com.sg.secram.avro.SecramRecordAvro.Builder newBuilder() {
    return new com.sg.secram.avro.SecramRecordAvro.Builder();
  }
  
  /** Creates a new SecramRecordAvro RecordBuilder by copying an existing Builder */
  public static com.sg.secram.avro.SecramRecordAvro.Builder newBuilder(com.sg.secram.avro.SecramRecordAvro.Builder other) {
    return new com.sg.secram.avro.SecramRecordAvro.Builder(other);
  }
  
  /** Creates a new SecramRecordAvro RecordBuilder by copying an existing SecramRecordAvro instance */
  public static com.sg.secram.avro.SecramRecordAvro.Builder newBuilder(com.sg.secram.avro.SecramRecordAvro other) {
    return new com.sg.secram.avro.SecramRecordAvro.Builder(other);
  }
  
  /**
   * RecordBuilder for SecramRecordAvro instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<SecramRecordAvro>
    implements org.apache.avro.data.RecordBuilder<SecramRecordAvro> {

    private long POS;
    private java.util.List<com.sg.secram.avro.ReadHeaderAvro> readHeaders;
    private java.nio.ByteBuffer PosCigar;
    private java.nio.ByteBuffer Qual;

    /** Creates a new Builder */
    private Builder() {
      super(com.sg.secram.avro.SecramRecordAvro.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.sg.secram.avro.SecramRecordAvro.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.POS)) {
        this.POS = data().deepCopy(fields()[0].schema(), other.POS);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.readHeaders)) {
        this.readHeaders = data().deepCopy(fields()[1].schema(), other.readHeaders);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.PosCigar)) {
        this.PosCigar = data().deepCopy(fields()[2].schema(), other.PosCigar);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.Qual)) {
        this.Qual = data().deepCopy(fields()[3].schema(), other.Qual);
        fieldSetFlags()[3] = true;
      }
    }
    
    /** Creates a Builder by copying an existing SecramRecordAvro instance */
    private Builder(com.sg.secram.avro.SecramRecordAvro other) {
            super(com.sg.secram.avro.SecramRecordAvro.SCHEMA$);
      if (isValidValue(fields()[0], other.POS)) {
        this.POS = data().deepCopy(fields()[0].schema(), other.POS);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.readHeaders)) {
        this.readHeaders = data().deepCopy(fields()[1].schema(), other.readHeaders);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.PosCigar)) {
        this.PosCigar = data().deepCopy(fields()[2].schema(), other.PosCigar);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.Qual)) {
        this.Qual = data().deepCopy(fields()[3].schema(), other.Qual);
        fieldSetFlags()[3] = true;
      }
    }

    /** Gets the value of the 'POS' field */
    public java.lang.Long getPOS() {
      return POS;
    }
    
    /** Sets the value of the 'POS' field */
    public com.sg.secram.avro.SecramRecordAvro.Builder setPOS(long value) {
      validate(fields()[0], value);
      this.POS = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'POS' field has been set */
    public boolean hasPOS() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'POS' field */
    public com.sg.secram.avro.SecramRecordAvro.Builder clearPOS() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'readHeaders' field */
    public java.util.List<com.sg.secram.avro.ReadHeaderAvro> getReadHeaders() {
      return readHeaders;
    }
    
    /** Sets the value of the 'readHeaders' field */
    public com.sg.secram.avro.SecramRecordAvro.Builder setReadHeaders(java.util.List<com.sg.secram.avro.ReadHeaderAvro> value) {
      validate(fields()[1], value);
      this.readHeaders = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'readHeaders' field has been set */
    public boolean hasReadHeaders() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'readHeaders' field */
    public com.sg.secram.avro.SecramRecordAvro.Builder clearReadHeaders() {
      readHeaders = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'PosCigar' field */
    public java.nio.ByteBuffer getPosCigar() {
      return PosCigar;
    }
    
    /** Sets the value of the 'PosCigar' field */
    public com.sg.secram.avro.SecramRecordAvro.Builder setPosCigar(java.nio.ByteBuffer value) {
      validate(fields()[2], value);
      this.PosCigar = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'PosCigar' field has been set */
    public boolean hasPosCigar() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'PosCigar' field */
    public com.sg.secram.avro.SecramRecordAvro.Builder clearPosCigar() {
      PosCigar = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'Qual' field */
    public java.nio.ByteBuffer getQual() {
      return Qual;
    }
    
    /** Sets the value of the 'Qual' field */
    public com.sg.secram.avro.SecramRecordAvro.Builder setQual(java.nio.ByteBuffer value) {
      validate(fields()[3], value);
      this.Qual = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'Qual' field has been set */
    public boolean hasQual() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'Qual' field */
    public com.sg.secram.avro.SecramRecordAvro.Builder clearQual() {
      Qual = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public SecramRecordAvro build() {
      try {
        SecramRecordAvro record = new SecramRecordAvro();
        record.POS = fieldSetFlags()[0] ? this.POS : (java.lang.Long) defaultValue(fields()[0]);
        record.readHeaders = fieldSetFlags()[1] ? this.readHeaders : (java.util.List<com.sg.secram.avro.ReadHeaderAvro>) defaultValue(fields()[1]);
        record.PosCigar = fieldSetFlags()[2] ? this.PosCigar : (java.nio.ByteBuffer) defaultValue(fields()[2]);
        record.Qual = fieldSetFlags()[3] ? this.Qual : (java.nio.ByteBuffer) defaultValue(fields()[3]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
