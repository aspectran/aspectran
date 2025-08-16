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

import java.util.List;

/**
 * Represents a single named parameter within an APON {@link Parameters} container.
 * A parameter holds a name, a declared {@link ValueType} (which may be hinted or
 * fixed), and an associated value which can be a scalar, an array, or a nested
 * {@link Parameters} structure when the value type is {@code PARAMETERS}.
 * Implementations also expose convenient typed accessors for retrieving values.
 */
public interface Parameter {

    /**
     * Return the container that holds this parameter.
     * @return the owning Parameters container (may be null while unattached)
     */
    Parameters getContainer();

    /**
     * Return the local (unqualified) name of this parameter.
     */
    String getName();

    /**
     * Return the fully qualified name (including owner ancestry where applicable).
     */
    String getQualifiedName();

    /**
     * Return the declared value type for this parameter.
     */
    ValueType getValueType();

    /**
     * Change the declared value type for this parameter.
     * @param valueType the new type
     */
    void setValueType(ValueType valueType);

    /**
     * Whether the value type is fixed (not adjusted at runtime).
     */
    boolean isValueTypeFixed();

    /**
     * Whether the value type originated from a name hint, e.g. name(int).
     */
    boolean isValueTypeHinted();

    /**
     * Mark this parameter as having a hinted value type.
     * @param valueTypeHinted true if hinted
     */
    void setValueTypeHinted(boolean valueTypeHinted);

    /**
     * Whether the parameter accepts multiple values (array semantics).
     */
    boolean isArray();

    /**
     * Whether array values are represented with explicit square brackets in APON.
     */
    boolean isBracketed();

    /**
     * Whether any value (including null) has been assigned.
     */
    boolean isAssigned();

    /**
     * Whether a non-null value is present.
     */
    boolean hasValue();

    /**
     * Return the number of elements if this parameter is an array; 0 for none.
     */
    int getArraySize();

    /**
     * Return the raw value (scalar, array, or nested Parameters) or null.
     */
    Object getValue();

    /**
     * Convert the parameter to array form if not already; preserves existing value as first element.
     */
    void arraylize();

    /**
     * Put/append a value to this parameter (converts to array if needed).
     * @param value the value to assign or append
     */
    void putValue(Object value);

    /**
     * Remove the current value (clears assigned state).
     */
    void removeValue();

    /**
     * Return the values as an object array (may contain boxed primitives or Parameters).
     */
    Object[] getValues();

    /**
     * Return the values as an untyped list view.
     */
    List<?> getValueList();

    /**
     * Retrieve the value as a String, performing conversion as needed.
     */
    String getValueAsString();

    /**
     * Retrieve the value as an array of Strings.
     */
    String[] getValueAsStringArray();

    /**
     * Retrieve the value as a List of Strings.
     */
    List<String> getValueAsStringList();

    /**
     * Retrieve the value as an Integer.
     */
    Integer getValueAsInt();

    /**
     * Retrieve the value as an array of Integers.
     */
    Integer[] getValueAsIntArray();

    /**
     * Retrieve the value as a List of Integers.
     */
    List<Integer> getValueAsIntList();

    /**
     * Retrieve the value as a Long.
     */
    Long getValueAsLong();

    /**
     * Retrieve the value as an array of Longs.
     */
    Long[] getValueAsLongArray();

    /**
     * Retrieve the value as a List of Longs.
     */
    List<Long> getValueAsLongList();

    /**
     * Retrieve the value as a Float.
     */
    Float getValueAsFloat();

    /**
     * Retrieve the value as an array of Floats.
     */
    Float[] getValueAsFloatArray();

    /**
     * Retrieve the value as a List of Floats.
     */
    List<Float> getValueAsFloatList();

    /**
     * Retrieve the value as a Double.
     */
    Double getValueAsDouble();

    /**
     * Retrieve the value as an array of Doubles.
     */
    Double[] getValueAsDoubleArray();

    /**
     * Retrieve the value as a List of Doubles.
     */
    List<Double> getValueAsDoubleList();

    /**
     * Retrieve the value as a Boolean.
     */
    Boolean getValueAsBoolean();

    /**
     * Retrieve the value as an array of Booleans.
     */
    Boolean[] getValueAsBooleanArray();

    /**
     * Retrieve the value as a List of Booleans.
     */
    List<Boolean> getValueAsBooleanList();

    /**
     * Retrieve the value as a nested Parameters block.
     */
    Parameters getValueAsParameters();

    /**
     * Retrieve the value as an array of nested Parameters blocks.
     */
    Parameters[] getValueAsParametersArray();

    /**
     * Retrieve the value as a List of nested Parameters blocks.
     */
    List<Parameters> getValueAsParametersList();

    /**
     * Create and attach a new nested Parameters instance under this parameter (must be PARAMETERS type).
     * @param identifier the parameter metadata or instance used to infer characteristics
     * @param <T> the nested container type
     * @return the created nested container
     */
    <T extends Parameters> T newParameters(Parameter identifier);

}
