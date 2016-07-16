/**
 * Copyright 2008-2016 Juho Jeong
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
	 * Gets the parameter name.
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
	 * Gets the parameter value type.
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
	 * Returns whether the parameter value is array.
	 *
	 * @return {@code true} if the parameter value is array, otherwise {@code false}
	 */
	boolean isArray();
	
	boolean isBracketed();

	/**
	 * Returns whether a value was assigned to the parameter value.
	 *
	 * @return {@code true} if a value was assigned to the parameter value, otherwise {@code false}
	 */
	boolean isAssigned();

	/**
	 * Gets the size of the array if the value is an array.
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
	 * Puts the parameter value.
	 *
	 * @param value the parameter value
	 */
	void putValue(Object value);

	/**
	 * Clears the parameter value.
	 */
	void clearValue();

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

	List<String> getValueAsStringList();
	
	Integer getValueAsInt();

	Integer[] getValueAsIntArray();

	List<Integer> getValueAsIntList();

	Long getValueAsLong();
	
	Long[] getValueAsLongArray();

	List<Long> getValueAsLongList();

	Float getValueAsFloat();
	
	Float[] getValueAsFloatArray();

	List<Float> getValueAsFloatList();

	Double getValueAsDouble();

	Double[] getValueAsDoubleArray();
	
	List<Double> getValueAsDoubleList();
	
	Boolean getValueAsBoolean();

	Boolean[] getValueAsBooleanArray();
	
	List<Boolean> getValueAsBooleanList();
	
	Parameters getValueAsParameters();

	Parameters[] getValueAsParametersArray();

	List<Parameters> getValueAsParametersList();
	
	Parameters newParameters(Parameter prototype);

}
