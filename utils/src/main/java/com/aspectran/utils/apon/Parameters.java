/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Interface Parameters.
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
     * Returns the owner of this Parameters.
     * @return the owner of this Parameters
     */
    Parameter getProprietor();

    /**
     * Specifies the owner of this Parameters.
     * @param proprietor the owner of this Parameters
     */
    void setProprietor(Parameter proprietor);

    /**
     * Returns the parent of the proprietor of this Parameters.
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
     * Specifies the actual name of this Parameters.
     * @param actualName the actual name of this Parameters
     */
    void setActualName(String actualName);

    /**
     * Returns the qualified name.
     * @return the qualified name
     */
    String getQualifiedName();

    ParameterValue getParameterValue(String name);

    /**
     * Returns a map of the {@code ParameterValue}s.
     * @return a map of the {@code ParameterValue}s
     */
    Map<String, ParameterValue> getParameterValueMap();

    /**
     * Returns all the parameter names associated with this Parameters.
     * @return an array of all parameter names associated with this Parameters
     */
    String[] getParameterNames();

    /**
     * Returns all the parameter names associated with this Parameters.
     * @return the Set with all parameter names associated with this Parameters
     */
    Set<String> getParameterNameSet();

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
     * Put a value into the Parameter with the specified name.
     * If there is an existing value, remove it and put it.
     * @param name the parameter name
     * @param value the value of parameter
     */
    void setValue(String name, Object value);

    /**
     * Put a value into the Parameter with the specified parameter definition.
     * If there is an existing value, remove it and put it.
     * @param key the parameter definition
     * @param value the value of parameter
     */
    void setValue(ParameterKey key, Object value);

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
    void putValueNonNull(String name, Object value);

    /**
     * Put a value of the parameter corresponding to the given parameter definition.
     * If the value is null, ignore it.
     * @param key the parameter definition
     * @param value the value of parameter
     */
    void putValueNonNull(ParameterKey key, Object value);

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

    ParameterValue newParameterValue(String name, ValueType valueType);

    ParameterValue newParameterValue(String name, ValueType valueType, boolean array);

    <T extends Parameters> T newParameters(String name);

    <T extends Parameters> T newParameters(ParameterKey key);

    <T extends Parameters> T touchParameters(String name);

    <T extends Parameters> T touchParameters(ParameterKey key);

    /**
     * Updates the holder of the subparameters belonging to this parameter.
     */
    void updateContainer(Parameters container);

    void readFrom(String apon) throws AponParseException;

    void readFrom(VariableParameters parameters) throws AponParseException;

    void readFrom(File file) throws AponParseException;

    void readFrom(File file, String encoding) throws AponParseException;

    void readFrom(Reader reader) throws AponParseException;

    <T extends Parameters> T copy();

    String describe();

    String describe(boolean details);

}
