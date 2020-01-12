/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import java.io.IOException;
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
     *
     * @return {@code true} if the parameter can be added after the parameters
     *      instance is created, otherwise {@code false}
     */
    boolean isStructureFixed();

    /**
     * Returns the owner of this Parameters.
     *
     * @return the owner of this Parameters
     */
    Parameter getProprietor();

    /**
     * Specifies the owner of this Parameters.
     *
     * @param proprietor the owner of this Parameters
     */
    void setProprietor(Parameter proprietor);

    /**
     * Returns the parent of the proprietor of this Parameters.
     * <pre>
     * proprietor --&gt; container --&gt; proprietor == parent
     * </pre>
     *
     * @return a {@code Parameter}
     */
    Parameter getParent();

    /**
     * Returns its real name.
     * If no name is given, it returns the name given by the owner.
     *
     * @return the actual name of this Parameters
     */
    String getActualName();

    /**
     * Specifies the actual name of this Parameters.
     *
     * @param actualName the actual name of this Parameters
     */
    void setActualName(String actualName);

    /**
     * Returns the qualified name.
     *
     * @return the qualified name
     */
    String getQualifiedName();

    ParameterValue getParameterValue(String name);

    /**
     * Returns a map of the {@code ParameterValue}s.
     *
     * @return a map of the {@code ParameterValue}s
     */
    Map<String, ParameterValue> getParameterValueMap();

    /**
     * Returns all the parameter names associated with this Parameters.
     *
     * @return an array of all parameter names associated with this Parameters
     */
    String[] getParameterNames();

    /**
     * Returns all the parameter names associated with this Parameters.
     *
     * @return the Set with all parameter names associated with this Parameters
     */
    Set<String> getParameterNameSet();

    /**
     * Returns whether this parameter exists.
     *
     * @param name the name of the parameter to check
     * @return {@code true} if the specified parameter exists; {@code false} otherwise
     */
    boolean hasParameter(String name);

    /**
     * Returns whether the specified parameter exists.
     *
     * @param parameterKey the parameter definition
     * @return {@code true} if the specified parameter exists; {@code false} otherwise
     */
    boolean hasParameter(ParameterKey parameterKey);

    /**
     * Returns whether a value is assigned to the specified parameter.
     * Even if a null is assigned, it is valid.
     *
     * @param name the name of the parameter to check
     * @return {@code true} if a parameter is assigned a value; {@code false} otherwise
     */
    boolean isAssigned(String name);

    /**
     * Returns whether a value is assigned to the specified parameter.
     * Even if a null is assigned, it is valid.
     *
     * @param parameterKey the parameter definition
     * @return {@code true} if a parameter is assigned a value; {@code false} otherwise
     */
    boolean isAssigned(ParameterKey parameterKey);

    /**
     * Returns whether a non-null value is assigned to the specified parameter.
     *
     * @param name the name of the parameter to check
     * @return {@code true} if a non-null value is assigned a value; {@code false} otherwise
     */
    boolean hasValue(String name);

    /**
     * Returns whether a non-null value is assigned to the specified parameter.
     *
     * @param parameterKey the parameter definition
     * @return {@code true} if a non-null value is assigned a value; {@code false} otherwise
     */
    boolean hasValue(ParameterKey parameterKey);

    /**
     * Returns the Parameter with the specified name.
     *
     * @param name the parameter name
     * @return the Parameter with the specified name, or {@code null} if it does not exist
     */
    Parameter getParameter(String name);

    /**
     * Returns the Parameter corresponding to the specified parameter definition.
     *
     * @param parameterKey the parameter definition
     * @return the Parameter corresponding to the specified parameter definition,
     *      or {@code null} if it does not exist
     */
    Parameter getParameter(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Object getValue(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Object getValue(ParameterKey parameterKey);

    void putAll(Parameters parameters);

    /**
     * Put a value into the Parameter with the specified name.
     *
     * @param name the parameter name
     * @param value the value of parameter
     */
    void putValue(String name, Object value);

    /**
     * Put a value into the Parameter with the specified parameter definition.
     *
     * @param parameterKey the parameter definition
     * @param value the value of parameter
     */
    void putValue(ParameterKey parameterKey, Object value);

    /**
     * Put a value of the parameter corresponding to the given name.
     * If the value is null, ignore it.
     *
     * @param name the parameter name
     * @param value the value of parameter
     */
    void putValueNonNull(String name, Object value);

    /**
     * Put a value of the parameter corresponding to the given parameter definition.
     * If the value is null, ignore it.
     *
     * @param parameterKey the parameter definition
     * @param value the value of parameter
     */
    void putValueNonNull(ParameterKey parameterKey, Object value);

    void clearValue(String name);

    void clearValue(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    String getString(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    String getString(String name, String defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    String[] getStringArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    String getString(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    String getString(ParameterKey parameterKey, String defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    String[] getStringArray(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<String> getStringList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<String> getStringList(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Integer getInt(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    int getInt(String name, int defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Integer[] getIntArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Integer getInt(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    int getInt(ParameterKey parameterKey, int defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Integer[] getIntArray(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Integer> getIntList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Integer> getIntList(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Long getLong(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    long getLong(String name, long defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Long[] getLongArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Long getLong(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    long getLong(ParameterKey parameterKey, long defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Long[] getLongArray(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Long> getLongList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Long> getLongList(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Float getFloat(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    float getFloat(String name, float defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Float[] getFloatArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Float getFloat(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    float getFloat(ParameterKey parameterKey, float defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Float[] getFloatArray(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Float> getFloatList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Float> getFloatList(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Double getDouble(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    double getDouble(String name, double defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Double[] getDoubleArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Double getDouble(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    double getDouble(ParameterKey parameterKey, double defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Double[] getDoubleArray(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Double> getDoubleList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Double> getDoubleList(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Boolean getBoolean(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param name the parameter name
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    Boolean[] getBooleanArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Boolean getBoolean(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @param defaultValue the default value to return if no value is found
     * @return the value for the specified parameter, or {@code defaultValue}
     */
    boolean getBoolean(ParameterKey parameterKey, boolean defaultValue);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    Boolean[] getBooleanArray(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    List<Boolean> getBooleanList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    List<Boolean> getBooleanList(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param <T> the type parameter
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> T getParameters(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param <T> the type parameter
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> T[] getParametersArray(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param <T> the type parameter
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> T getParameters(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param <T> the type parameter
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> T[] getParametersArray(ParameterKey parameterKey);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param <T> the type parameter
     * @param name the parameter name
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> List<T> getParametersList(String name);

    /**
     * Return the value for the specified parameter,
     * or {@code null} if the parameter does not exist.
     *
     * @param <T> the type parameter
     * @param parameterKey the parameter definition
     * @return the value for the specified parameter, or {@code null}
     */
    <T extends Parameters> List<T> getParametersList(ParameterKey parameterKey);

    ParameterValue newParameterValue(String name, ValueType valueType);

    ParameterValue newParameterValue(String name, ValueType valueType, boolean array);

    <T extends Parameters> T newParameters(String name);

    <T extends Parameters> T newParameters(ParameterKey parameterKey);

    <T extends Parameters> T touchParameters(String name);

    <T extends Parameters> T touchParameters(ParameterKey parameterKey);

    /**
     * Updates the holder of the subparameters belonging to this parameter.
     */
    void updateContainer(Parameters container);

    void readFrom(String text) throws IOException;

    String describe();

    String describe(boolean details);

}
