/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util.apon;

import java.util.List;

public interface Parameter {

    /**
     * Returns the {@code Parameters} that contains the {@code Parameter}.
     *
     * @return the {@code Parameters}
     */
    Parameters getContainer();

    /**
     * Returns the parameter name.
     *
     * @return the parameter name
     */
    String getName();

    /**
     * Returns the fully qualified parameter name.
     *
     * @return the qualified name
     */
    String getQualifiedName();

    /**
     * Returns the parameter value type.
     *
     * @return the parameter value type
     */
    ParameterValueType getParameterValueType();

    /**
     * Sets the parameter value type.
     *
     * @param parameterValueType the parameter value type
     */
    void setParameterValueType(ParameterValueType parameterValueType);

    /**
     * Returns whether the value type is hinted.
     *
     * @return true if the value type is hinted; false otherwise
     */
    boolean isValueTypeHinted();

    /**
     * Sets whether the value type is hinted.
     *
     * @param valueTypeHinted true if the value type is hinted; false otherwise
     */
    void setValueTypeHinted(boolean valueTypeHinted);

    /**
     * Returns whether the parameter value is an array.
     *
     * @return {@code true} if the parameter value is an array, otherwise {@code false}
     */
    boolean isArray();

    /**
     * Returns whether the value of the array is represented using square brackets.
     *
     * @return {@code true} if the value of the array is represented using square brackets,
     *      otherwise {@code false}
     */
    boolean isBracketed();

    /**
     * Returns whether this is a predefined parameter.
     *
     * @return {@code true} if this is a predefined parameter, otherwise {@code false}
     */
    boolean isPredefined();

    /**
     * Returns whether a parameter has been assigned a value.
     *
     * @return {@code true} if a parameter has been assigned a value, otherwise {@code false}
     */
    boolean isAssigned();

    /**
     * Returns the size of the array if the value is an array.
     *
     * @return the size of the array
     */
    int getArraySize();

    /**
     * Returns a value as an {@code Object}.
     *
     * @return an {@code Object}
     */
    Object getValue();

    /**
     * Change parameter type to array type.
     */
    void arraylize();

    /**
     * Puts the parameter value.
     *
     * @param value the parameter value
     */
    void putValue(Object value);

    /**
     * Returns a value as an {@code Object} array.
     *
     * @return an array of {@code Object}
     */
    Object[] getValues();

    /**
     * Returns a value as a {@code List}.
     *
     * @return a {@code List}
     */
    List<?> getValueList();

    /**
     * Returns a value as a {@code String}.
     *
     * @return a {@code String}
     */
    String getValueAsString();

    /**
     * Returns a value as a {@code String} array.
     *
     * @return a {@code String} array
     */
    String[] getValueAsStringArray();

    /**
     * Returns a value as a {@code List<String>}.
     *
     * @return a {@code List<String>}
     */
    List<String> getValueAsStringList();

    /**
     * Returns a value as an {@code Integer}.
     *
     * @return an {@code Integer}
     */
    Integer getValueAsInt();

    /**
     * Returns a value as an {@code Integer} array.
     *
     * @return an {@code Integer} array
     */
    Integer[] getValueAsIntArray();

    /**
     * Returns a value as a {@code List<Integer>}.
     *
     * @return a {@code List<Integer>}
     */
    List<Integer> getValueAsIntList();

    /**
     * Returns a value as a {@code Long}.
     *
     * @return a {@code Long}
     */
    Long getValueAsLong();

    /**
     * Returns a value as a {@code Long} array.
     *
     * @return a {@code Long} array
     */
    Long[] getValueAsLongArray();

    /**
     * Returns a value as a {@code List<Long>}.
     *
     * @return a {@code List<Long>}
     */
    List<Long> getValueAsLongList();

    /**
     * Returns a value as a {@code Float}.
     *
     * @return a {@code Float}
     */
    Float getValueAsFloat();

    /**
     * Returns a value as a {@code Float} array.
     *
     * @return a {@code Float} array
     */
    Float[] getValueAsFloatArray();

    /**
     * Returns a value as a {@code List<Float>}.
     *
     * @return a {@code List<Float>}
     */
    List<Float> getValueAsFloatList();

    /**
     * Returns a value as a {@code Double}.
     *
     * @return a {@code Double}
     */
    Double getValueAsDouble();

    /**
     * Returns a value as a {@code Double} array.
     *
     * @return a {@code Double} array
     */
    Double[] getValueAsDoubleArray();

    /**
     * Returns a value as a {@code List<Double>}.
     *
     * @return a {@code List<Double>}
     */
    List<Double> getValueAsDoubleList();

    /**
     * Returns a value as a {@code Boolean}.
     *
     * @return a {@code Boolean}
     */
    Boolean getValueAsBoolean();

    /**
     * Returns a value as a {@code Boolean} array.
     *
     * @return a {@code Boolean} array
     */
    Boolean[] getValueAsBooleanArray();

    /**
     * Returns a value as a {@code List<Boolean>}.
     *
     * @return a {@code List<Boolean>}
     */
    List<Boolean> getValueAsBooleanList();

    /**
     * Returns a value as a {@code Parameters}.
     *
     * @return a {@code Parameters}
     */
    Parameters getValueAsParameters();

    /**
     * Returns a value as a {@code Parameters} array.
     *
     * @return a {@code Parameters} array
     */
    Parameters[] getValueAsParametersArray();

    /**
     * Returns a value as a {@code List<Parameters>}.
     *
     * @return a {@code List<Parameters>}
     */
    List<Parameters> getValueAsParametersList();

    /**
     * Creates a new instance of {@code Parameters} with the specified
     * identifier {@code Parameter}.
     *
     * @param identifier the specified identifier {@code Parameter}
     * @return a {@code Parameters}
     */
    Parameters newParameters(Parameter identifier);

    /**
     * Clears the parameter value.
     */
    void clearValue();

}
