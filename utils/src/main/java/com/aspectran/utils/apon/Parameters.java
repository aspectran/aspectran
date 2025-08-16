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

import java.io.File;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

/**
 * Central contract representing a mutable collection of named parameters in APON.
 * <p>
 * A {@code Parameters} instance acts as a typed map where each entry is a
 * {@link Parameter} that can store scalar values, arrays, or nested
 * {@link Parameters} (for hierarchical structures). Implementations may expose
 * either a fixed structure (predefined keys) or allow dynamic addition of
 * parameters at runtime. Numerous convenience getters are provided to retrieve
 * values in the desired Java type.
 * </p>
 */
public interface Parameters {

    /**
     * Returns whether the parameter can be added after the parameters instance
     * is created.
     * @return {@code true} if the parameter can be added after the parameters
     *      instance is created, otherwise {@code false}
     */
    boolean isStructureFixed();

    /**
     * Returns the owner of this {@code Parameters}.
     * @return the owner of this {@code Parameters}
     */
    Parameter getProprietor();

    /**
     * Specifies the owner of this {@code Parameters}.
     * @param proprietor the owner of this {@code Parameters}
     */
    void setProprietor(Parameter proprietor);

    /**
     * Returns the parent of the proprietor of this {@code Parameters}.
     * <pre>
     * proprietor --&gt; container --&gt; proprietor == parent
     * </pre>
     * @return a {@code Parameter}
     */
    Parameter getParent();

    /**
     * Returns its real name.
     * If no name is given, it returns the name given by the owner.
     * @return the actual name of this Parameters
     */
    String getActualName();

    /**
     * Specifies the actual name of this {@code Parameters}.
     * @param actualName the actual name of this {@code Parameters}
     */
    void setActualName(String actualName);

    /**
     * Returns the qualified name.
     * @return the qualified name
     */
    String getQualifiedName();

    /**
         * Returns the qualified name built from this container's actual name and the given local name.
         * @param name the local parameter name
         * @return a qualified name for display/logging
         */
        String getQualifiedName(String name);

    /**
         * Returns the qualified name for the given predefined key in this container.
         * @param key the parameter definition
         * @return a qualified name for display/logging
         */
        String getQualifiedName(ParameterKey key);

    /**
         * Return the internal {@link ParameterValue} holder by name, or {@code null} if absent.
         * @param name the parameter name
         * @return the holder or {@code null}
         */
        ParameterValue getParameterValue(String name);

    /**
         * Return the internal {@link ParameterValue} holder by predefined key, or {@code null}.
         * @param key the parameter definition
         * @return the holder or {@code null}
         */
        ParameterValue getParameterValue(ParameterKey key);

    /**
         * Return a read-only view of all {@link ParameterValue} holders in declaration order.
         * @return a collection of holders
         */
        Collection<ParameterValue> getParameterValues();

    /**
     * Returns all parameter names associated with this {@code Parameters}.
     * @return an array of all parameter names associated with this {@code Parameters}
     */
    String[] getParameterNames();

    /**
     * Returns whether this parameter exists.
     * @param name the name of the parameter to check
     * @return {@code true} if the specified parameter exists; {@code false} otherwise
     */
    boolean hasParameter(String name);

    /**
     * Returns whether the specified parameter exists.
     * @param key the parameter definition
     * @return {@code true} if the specified parameter exists; {@code false} otherwise
     */
    boolean hasParameter(ParameterKey key);

    /**
     * Returns whether a value is assigned to the specified parameter.
     * Even if a null is assigned, it is valid.
     * @param name the name of the parameter to check
     * @return {@code true} if a parameter is assigned a value; {@code false} otherwise
     */
    boolean isAssigned(String name);

    /**
     * Returns whether a value is assigned to the specified parameter.
     * Even if a null is assigned, it is valid.
     * @param key the parameter definition
     * @return {@code true} if a parameter is assigned a value; {@code false} otherwise
     */
    boolean isAssigned(ParameterKey key);

    /**
     * Returns whether a non-null value is assigned to the specified parameter.
     * @param name the name of the parameter to check
     * @return {@code true} if a non-null value is assigned a value; {@code false} otherwise
     */
    boolean hasValue(String name);

    /**
     * Returns whether a non-null value is assigned to the specified parameter.
     * @param key the parameter definition
     * @return {@code true} if a non-null value is assigned a value; {@code false} otherwise
     */
    boolean hasValue(ParameterKey key);

    /**
     * Returns the Parameter with the specified name.
     * @param name the parameter name
     * @return the Parameter with the specified name, or {@code null} if it does not exist
     */
    Parameter getParameter(String name);

    /**
     * Returns the Parameter corresponding to the specified parameter definition.
     * @param key the parameter definition
     * @return the Parameter corresponding to the specified parameter definition,
     *      or {@code null} if it does not exist
     */
    Parameter getParameter(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Object getValue(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Object getValue(ParameterKey key);

    /**
         * Copy all values from the given container into this one, overwriting existing values.
         * Structure is not altered; only values are affected.
         * @param parameters the source of values to copy
         */
        void putAll(Parameters parameters);

    /**
     * Put a value into the Parameter with the specified name.
     * @param name the parameter name
     * @param value the value of parameter
     */
    void putValue(String name, Object value);

    /**
     * Put a value into the Parameter with the specified parameter definition.
     * @param key the parameter definition
     * @param value the value of parameter
     */
    void putValue(ParameterKey key, Object value);

    /**
     * Put a value of the parameter corresponding to the given name.
     * If the value is null, ignore it.
     * @param name the parameter name
     * @param value the value of parameter
     */
    void putValueIfNotNull(String name, Object value);

    /**
     * Put a value of the parameter corresponding to the given parameter definition.
     * If the value is null, ignore it.
     * @param key the parameter definition
     * @param value the value of parameter
     */
    void putValueIfNotNull(ParameterKey key, Object value);

    /**
     * Remove the value of this parameter.
     * @param name the parameter name
     */
    void removeValue(String name);

    /**
     * Remove the value of this parameter.
     * @param key the parameter key
     */
    void removeValue(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    String getString(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    String getString(String name, String defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    String[] getStringArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    String getString(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param key the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    String getString(ParameterKey key, String defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    String[] getStringArray(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<String> getStringList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<String> getStringList(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Integer getInt(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    int getInt(String name, int defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Integer[] getIntArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Integer getInt(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param key the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    int getInt(ParameterKey key, int defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Integer[] getIntArray(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Integer> getIntList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Integer> getIntList(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Long getLong(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    long getLong(String name, long defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Long[] getLongArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Long getLong(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param key the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    long getLong(ParameterKey key, long defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Long[] getLongArray(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Long> getLongList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Long> getLongList(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Float getFloat(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    float getFloat(String name, float defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Float[] getFloatArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Float getFloat(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param key the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    float getFloat(ParameterKey key, float defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Float[] getFloatArray(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Float> getFloatList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Float> getFloatList(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Double getDouble(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    double getDouble(String name, double defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Double[] getDoubleArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Double getDouble(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param key the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    double getDouble(ParameterKey key, double defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Double[] getDoubleArray(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Double> getDoubleList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Double> getDoubleList(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Boolean getBoolean(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Boolean[] getBooleanArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Boolean getBoolean(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     * @param key the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    boolean getBoolean(ParameterKey key, boolean defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Boolean[] getBooleanArray(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Boolean> getBooleanList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Boolean> getBooleanList(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param <T> the type parameter
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> T getParameters(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param <T> the type parameter
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> T[] getParametersArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param <T> the type parameter
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> T getParameters(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param <T> the type parameter
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> T[] getParametersArray(ParameterKey key);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param <T> the type parameter
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> List<T> getParametersList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     * @param <T> the type parameter
     * @param key the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> List<T> getParametersList(ParameterKey key);

    /**
     * Create a new {@link ParameterValue} holder with the given type under the given name.
     * @param name the parameter name
     * @param valueType the declared type
     * @return a new holder associated with this container
     */
    ParameterValue newParameterValue(String name, ValueType valueType);

    /**
     * Create a new {@link ParameterValue} holder with the given type and array flag.
     * @param name the parameter name
     * @param valueType the declared type
     * @param array whether the parameter accepts multiple values
     * @return a new holder associated with this container
     */
    ParameterValue newParameterValue(String name, ValueType valueType, boolean array);

    /**
     * Create and attach a new nested {@link Parameters} instance under the given name.
     * @param <T> the concrete subtype
     * @param name the parameter name
     * @return the created nested container
     */
    <T extends Parameters> T newParameters(String name);

    /**
     * Create and attach a new nested {@link Parameters} instance under the given key.
     * @param <T> the concrete subtype
     * @param key the parameter definition
     * @return the created nested container
     */
    <T extends Parameters> T newParameters(ParameterKey key);

    /**
     * Ensure a nested {@link Parameters} exists under the given name and return it.
     * Creates it if missing.
     * @param <T> the concrete subtype
     * @param name the parameter name
     * @return the existing or newly created nested container
     */
    <T extends Parameters> T touchParameters(String name);

    /**
     * Ensure a nested {@link Parameters} exists under the given key and return it.
     * Creates it if missing.
     * @param <T> the concrete subtype
     * @param key the parameter definition
     * @return the existing or newly created nested container
     */
    <T extends Parameters> T touchParameters(ParameterKey key);

    /**
     * Updates the holder of the subparameters belonging to this parameter so that
     * the back-reference of nested parameter holders to point to the given container.
     * Primarily used internally when moving/merging parameters.
     * @param container the new parent container
     */
    void updateContainer(Parameters container);

    /**
     * Populate this container by parsing APON text.
     * @param apon APON-formatted string
     * @throws AponParseException on parse error
     */
    void readFrom(String apon) throws AponParseException;

    /**
     * Merge values from the given variable-structure container into this one.
     * @param parameters the source parameters
     * @throws AponParseException on parse/merge error
     */
    void readFrom(VariableParameters parameters) throws AponParseException;

    /**
     * Populate this container by reading APON content from the given file.
     * @param file the source file
     * @throws AponParseException on I/O or parse error
     */
    void readFrom(File file) throws AponParseException;

    /**
     * Populate this container by reading APON content from the given file using the specified encoding.
     * @param file the source file
     * @param encoding the character encoding to use
     * @throws AponParseException on I/O or parse error
     */
    void readFrom(File file, String encoding) throws AponParseException;

    /**
     * Populate this container by reading APON content from the given reader.
     * @param reader the character stream supplying APON content
     * @throws AponParseException on I/O or parse error
     */
    void readFrom(Reader reader) throws AponParseException;

    /**
     * Make a deep copy of this container including all nested parameters.
     * @param <T> the runtime container type
     * @return an independent copy
     */
    <T extends Parameters> T copy();

    /**
     * Render a brief human-readable description of this container and its parameters.
     * @return a description string
     */
    String describe();

    /**
     * Render a description of this container; include details if requested.
     * @param details whether to include parameter values and structure info
     * @return a description string
     */
    String describe(boolean details);

}
