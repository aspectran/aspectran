/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.embed.sample.anno;

import com.aspectran.core.component.bean.annotation.Format;
import com.aspectran.core.component.bean.annotation.Qualifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * <p>Created: 2019-02-16</p>
 */
public class TestModel {

    private String string;
    private String[] strings;

    private Character character;
    private Character[] characters;
    private char pcharacter;
    private char[] pcharacters;

    private Byte abyte;
    private Byte[] bytes;
    private byte pbyte;
    private byte[] pbytes;

    private Short ashort;
    private Short[] shorts;
    private short pshort;
    private short[] pshorts;

    private Integer integer;
    private Integer[] integers;
    private int pinteger;
    private int[] pintegers;

    private Long along;
    private Long[] longs;
    private long plong;
    private long[] plongs;

    private Float afloat;
    private Float[] floats;
    private float pfloat;
    private float[] pfloats;

    private Double adouble;
    private Double[] doubles;
    private double pdouble;
    private double[] pdoubles;

    private BigInteger bigInteger;
    private BigInteger[] bigIntegers;

    private BigDecimal bigDecimal;
    private BigDecimal[] bigDecimals;

    private String property;

    private Date date;

    public String getProperty() {
        return property;
    }

    public void setProperty(@Qualifier("string") String property) {
        this.property = property;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(@Format("yyyy-MM-dd") Date date) {
        this.date = date;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String[] getStrings() {
        return strings;
    }

    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public Character[] getCharacters() {
        return characters;
    }

    public void setCharacters(Character[] characters) {
        this.characters = characters;
    }

    public char getPcharacter() {
        return pcharacter;
    }

    public void setPcharacter(char pcharacter) {
        this.pcharacter = pcharacter;
    }

    public char[] getPcharacters() {
        return pcharacters;
    }

    public void setPcharacters(char[] pcharacters) {
        this.pcharacters = pcharacters;
    }

    public Byte getAbyte() {
        return abyte;
    }

    public void setAbyte(Byte abyte) {
        this.abyte = abyte;
    }

    public Byte[] getBytes() {
        return bytes;
    }

    public void setBytes(Byte[] bytes) {
        this.bytes = bytes;
    }

    public byte getPbyte() {
        return pbyte;
    }

    public void setPbyte(byte pbyte) {
        this.pbyte = pbyte;
    }

    public byte[] getPbytes() {
        return pbytes;
    }

    public void setPbytes(byte[] pbytes) {
        this.pbytes = pbytes;
    }

    public Short getAshort() {
        return ashort;
    }

    public void setAshort(Short ashort) {
        this.ashort = ashort;
    }

    public Short[] getShorts() {
        return shorts;
    }

    public void setShorts(Short[] shorts) {
        this.shorts = shorts;
    }

    public short getPshort() {
        return pshort;
    }

    public void setPshort(short pshort) {
        this.pshort = pshort;
    }

    public short[] getPshorts() {
        return pshorts;
    }

    public void setPshorts(short[] pshorts) {
        this.pshorts = pshorts;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public Integer[] getIntegers() {
        return integers;
    }

    public void setIntegers(Integer[] integers) {
        this.integers = integers;
    }

    public int getPinteger() {
        return pinteger;
    }

    public void setPinteger(int pinteger) {
        this.pinteger = pinteger;
    }

    public int[] getPintegers() {
        return pintegers;
    }

    public void setPintegers(int[] pintegers) {
        this.pintegers = pintegers;
    }

    public Long getAlong() {
        return along;
    }

    public void setAlong(Long along) {
        this.along = along;
    }

    public Long[] getLongs() {
        return longs;
    }

    public void setLongs(Long[] longs) {
        this.longs = longs;
    }

    public long getPlong() {
        return plong;
    }

    public void setPlong(long plong) {
        this.plong = plong;
    }

    public long[] getPlongs() {
        return plongs;
    }

    public void setPlongs(long[] plongs) {
        this.plongs = plongs;
    }

    public Float getAfloat() {
        return afloat;
    }

    public void setAfloat(Float afloat) {
        this.afloat = afloat;
    }

    public Float[] getFloats() {
        return floats;
    }

    public void setFloats(Float[] floats) {
        this.floats = floats;
    }

    public float getPfloat() {
        return pfloat;
    }

    public void setPfloat(float pfloat) {
        this.pfloat = pfloat;
    }

    public float[] getPfloats() {
        return pfloats;
    }

    public void setPfloats(float[] pfloats) {
        this.pfloats = pfloats;
    }

    public Double getAdouble() {
        return adouble;
    }

    public void setAdouble(Double adouble) {
        this.adouble = adouble;
    }

    public Double[] getDoubles() {
        return doubles;
    }

    public void setDoubles(Double[] doubles) {
        this.doubles = doubles;
    }

    public double getPdouble() {
        return pdouble;
    }

    public void setPdouble(double pdouble) {
        this.pdouble = pdouble;
    }

    public double[] getPdoubles() {
        return pdoubles;
    }

    public void setPdoubles(double[] pdoubles) {
        this.pdoubles = pdoubles;
    }

    public BigInteger getBigInteger() {
        return bigInteger;
    }

    public void setBigInteger(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
    }

    public BigInteger[] getBigIntegers() {
        return bigIntegers;
    }

    public void setBigIntegers(BigInteger[] bigIntegers) {
        this.bigIntegers = bigIntegers;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public BigDecimal[] getBigDecimals() {
        return bigDecimals;
    }

    public void setBigDecimals(BigDecimal[] bigDecimals) {
        this.bigDecimals = bigDecimals;
    }

}
