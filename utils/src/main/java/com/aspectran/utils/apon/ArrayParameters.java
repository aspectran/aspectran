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
package com.aspectran.utils.apon;

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * {@link Parameters} implementation designed to represent an ordered collection (array or list)
 * of values, which can include primitive types, strings, or nested {@link Parameters} blocks.
 * <p>This class extends {@link DefaultParameters} and provides functionality to manage
 * elements in an array-like structure. Internally, it uses the synthetic name
 * {@code <noname>} to store the individual elements of the array.</p>
 * <p>Unlike typical {@code Parameters} implementations that retrieve values by a specific name,
 * {@code ArrayParameters} focuses on providing access to its elements as an ordered list.
 * Consequently, most {@code get*} methods that attempt to retrieve a single named parameter
 * (e.g., {@code getString(String name)}) are not supported and will throw an
 * {@link UnsupportedOperationException}. Access to elements should primarily be done
 * through iteration or dedicated array-access methods like {@link #getValueList()},
 * {@link #getStringList()}, or {@link #getParametersList()}.</p>
 * <p>This class is {@link Serializable}, allowing its instances to be persisted or
 * transferred across processes.</p>
 *
 * @since 6.2.0
 * @see DefaultParameters
 * @see VariableParameters
 */
public class ArrayParameters extends DefaultParameters implements Iterable<Object>, Serializable {

    @Serial
    private static final long serialVersionUID = 2058392199376865356L;

    private static final String UNSUPPORTED_OPERATION_MESSAGE =
            "This method is not supported in ArrayParameters as it is designed for array-like access " +
            "rather than retrieving single named parameters.";

    /**
     * The synthetic name for the array parameter.
     */
    public static final String NONAME = "";

    /**
     * Creates a new {@code ArrayParameters} instance with a dynamic schema,
     * capable of holding an ordered collection of any {@link ValueType}.
     * Elements are stored internally under a synthetic name.
     */
    public ArrayParameters() {
        super(null);
        attachParameterValue(NONAME, ValueType.VARIABLE, true);
    }

    /**
     * Parses APON text into a new {@code ArrayParameters} instance.
     * The elements within the array can be of any {@link ValueType}.
     * @param apon APON text representing an array
     * @throws AponParseException if parsing fails
     */
    public ArrayParameters(String apon) throws AponParseException {
        this();
        readFrom(StringUtils.trimWhitespace(apon));
    }

    /**
     * Creates a new {@code ArrayParameters} instance with a fixed schema,
     * where each element in the array is expected to be an instance of the specified
     * {@link DefaultParameters}-derived class.
     * @param elementClass the {@link DefaultParameters} implementation for array elements
     */
    public ArrayParameters(Class<? extends DefaultParameters> elementClass) {
        // Create the single schema key used by this array container.
        super(new ParameterKey[] { new ParameterKey(NONAME, elementClass, true) });
    }

    /**
     * Parses APON text into a new {@code ArrayParameters} instance with a fixed schema,
     * where each element in the array is expected to be an instance of the specified
     * {@link DefaultParameters}-derived class.
     * @param elementClass the {@link DefaultParameters} implementation for array elements
     * @param apon APON text representing an array
     * @throws AponParseException if parsing fails
     */
    public ArrayParameters(Class<? extends DefaultParameters> elementClass, String apon) throws AponParseException {
        this(elementClass);
        readFrom(StringUtils.trimWhitespace(apon));
    }

    /**
     * Appends a new value to this array.
     * The type of the value will be determined automatically.
     * @param value the value to add
     */
    public void addValue(Object value) {
        putValue(NONAME, value);
    }

    /**
     * Returns the contents of this array as a {@link List} of {@link Object}s.
     * Each element in the list corresponds to an entry in the array.
     * @return a {@link List} containing all elements of this array,
     *      or {@code null} if the array is empty or not assigned
     */
    public List<?> getValueList() {
        return getValueList(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link String} array.
     * Each element in the array is converted to its string representation.
     * @return a {@link String} array containing all elements of this array,
     *      or {@code null} if the array is empty or not assigned
     */
    public String[] getStringArray() {
        return getStringArray(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link List} of {@link String}s.
     * Each element in the list is converted to its string representation.
     * @return a {@link List} containing all elements of this array as strings,
     *      or {@code null} if the array is empty or not assigned
     */
    public List<String> getStringList() {
        return getStringList(NONAME);
    }

    /**
     * Returns the contents of this array as an {@link Integer} array.
     * Each element in the array is converted to an integer.
     * @return an {@link Integer} array containing all elements of this array,
     *      or {@code null} if the array is empty or not assigned
     */
    public Integer[] getIntArray() {
        return getIntArray(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link List} of {@link Integer}s.
     * Each element in the list is converted to an integer.
     * @return a {@link List} containing all elements of this array as integers,
     *      or {@code null} if the array is empty or not assigned
     */
    public List<Integer> getIntList() {
        return getIntList(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link Long} array.
     * Each element in the array is converted to a long.
     * @return a {@link Long} array containing all elements of this array,
     *      or {@code null} if the array is empty or not assigned
     */
    public Long[] getLongArray() {
        return getLongArray(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link List} of {@link Long}s.
     * Each element in the list is converted to a long.
     * @return a {@link List} containing all elements of this array as longs,
     *      or {@code null} if the array is empty or not assigned
     */
    public List<Long> getLongList() {
        return getLongList(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link Float} array.
     * Each element in the array is converted to a float.
     * @return a {@link Float} array containing all elements of this array,
     *      or {@code null} if the array is empty or not assigned
     */
    public Float[] getFloatArray() {
        return getFloatArray(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link List} of {@link Float}s.
     * Each element in the list is converted to a float.
     * @return a {@link List} containing all elements of this array as floats,
     *      or {@code null} if the array is empty or not assigned
     */
    public List<Float> getFloatList() {
        return getFloatList(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link Double} array.
     * Each element in the array is converted to a double.
     * @return a {@link Double} array containing all elements of this array,
     *      or {@code null} if the array is empty or not assigned
     */
    public Double[] getDoubleArray() {
        return getDoubleArray(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link List} of {@link Double}s.
     * Each element in the list is converted to a double.
     * @return a {@link List} containing all elements of this array as doubles,
     *      or {@code null} if the array is empty or not assigned
     */
    public List<Double> getDoubleList() {
        return getDoubleList(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link Boolean} array.
     * Each element in the array is converted to a boolean.
     * @return a {@link Boolean} array containing all elements of this array,
     *      or {@code null} if the array is empty or not assigned
     */
    public Boolean[] getBooleanArray() {
        return getBooleanArray(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link List} of {@link Boolean}s.
     * Each element in the list is converted to a boolean.
     * @return a {@link List} containing all elements of this array as booleans,
     *      or {@code null} if the array is empty or not assigned
     */
    public List<Boolean> getBooleanList() {
        return getBooleanList(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link Parameters} array.
     * Each element in the array is expected to be a {@link Parameters} instance.
     * @return a {@link Parameters} array containing all elements of this array,
     *      or {@code null} if the array is empty or not assigned
     */
    public Parameters[] getParametersArray() {
        return getParametersArray(NONAME);
    }

    /**
     * Returns the contents of this array as a {@link List} of {@link Parameters} blocks.
     * Each element in the list is expected to be a {@link Parameters} instance.
     * @param <T> the type of {@link Parameters}
     * @return a {@link List} containing all elements of this array as {@link Parameters},
     *      or {@code null} if the array is empty or not assigned
     */
    public <T extends Parameters> List<T> getParametersList() {
        return getParametersList(NONAME);
    }

    /**
     * Returns an iterator over the elements contained in this array.
     * The elements can be of any {@link ValueType}.
     * @return an iterator, possibly empty
     */
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Iterator<Object> iterator() {
        List<Object> list = (List<Object>)getValueList(NONAME);
        if (list != null) {
            return list.iterator();
        } else {
            return Collections.emptyIterator();
        }
    }

    @Override
    public String getString(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public String getString(String name, String defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public String getString(ParameterKey key) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public String getString(ParameterKey key, String defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Integer getInt(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public int getInt(String name, int defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Integer getInt(ParameterKey key) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public int getInt(ParameterKey key, int defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Long getLong(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public long getLong(String name, long defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Long getLong(ParameterKey key) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public long getLong(ParameterKey key, long defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Float getFloat(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public float getFloat(String name, float defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Float getFloat(ParameterKey key) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public float getFloat(ParameterKey key, float defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Double getDouble(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public double getDouble(String name, double defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Double getDouble(ParameterKey key) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public double getDouble(ParameterKey key, double defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Boolean getBoolean(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Boolean getBoolean(ParameterKey key) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public boolean getBoolean(ParameterKey key, boolean defaultValue) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <T extends Parameters> T getParameters(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <T extends Parameters> T getParameters(ParameterKey key) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

}
