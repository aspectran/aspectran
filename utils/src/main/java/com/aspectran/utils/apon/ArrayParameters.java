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
 * of parameter groups or values.
 * <p>This class extends {@link AbstractParameters} and provides functionality to manage
 * elements in an array-like structure. Internally, it uses the synthetic name
 * {@code <noname>} to store the individual elements of the array.</p>
 * <p>Unlike typical {@code Parameters} implementations that retrieve values by a specific name,
 * {@code ArrayParameters} focuses on providing access to its elements as an ordered list.
 * Consequently, most {@code get*} methods that attempt to retrieve a single named parameter
 * (e.g., {@code getString(String name)}) are not supported and will throw an
 * {@link UnsupportedOperationException}. Access to elements should primarily be done
 * through iteration or methods like {@link #getParametersArray()} or {@link #getParametersList()}.</p>
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
     * Create an array container whose elements are {@link DefaultParameters} blocks.
     */
    public ArrayParameters() {
        super(null);
        newParameterValue(NONAME, ValueType.VARIABLE, true);
    }

    /**
     * Parse APON text into a new array container whose elements are {@link DefaultParameters}.
     * @param apon APON text representing an array
     * @throws AponParseException if parsing fails
     */
    public ArrayParameters(String apon) throws AponParseException {
        this();
        readFrom(StringUtils.trimWhitespace(apon));
    }

    /**
     * Create an array container with the given element class for each entry.
     * @param elementClass the Parameters implementation for array elements
     */
    public ArrayParameters(Class<? extends AbstractParameters> elementClass) {
        super(createParameterKeys(elementClass));
    }

    /**
     * Parse APON text into a new array container with the given element class.
     * @param elementClass the Parameters implementation for array elements
     * @param apon APON text representing an array
     * @throws AponParseException if parsing fails
     */
    public ArrayParameters(Class<? extends AbstractParameters> elementClass, String apon) throws AponParseException {
        this(elementClass);
        readFrom(StringUtils.trimWhitespace(apon));
    }

    /**
     * Create the single schema key used by this array container.
     * @param elementClass the Parameters class used for elements
     * @return a one-element key array for internal use
     */
    @NonNull
    private static ParameterKey[] createParameterKeys(Class<? extends AbstractParameters> elementClass) {
        ParameterKey pk = new ParameterKey(NONAME, elementClass, true);
        return new ParameterKey[] { pk };
    }

    public void addValue(Object value) {
        putValue(NONAME, value);
    }

    public List<Object> getValueList() {
        return getValueList(NONAME);
    }

    /**
     * Return the contents as a list of parameter blocks.
     * @return the list of elements or {@code null} if none
     */
    public <T extends Parameters> List<T> getParametersList() {
        return getParametersList(NONAME);
    }

    /**
     * Returns an iterator over the parameter blocks contained in this array.
     * @return an iterator, possibly empty
     */
    @Override
    @NonNull
    public Iterator<Object> iterator() {
        List<Object> list = getValueList(NONAME);
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
