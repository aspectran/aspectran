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

	Parameters getContainer();
	
	String getName();

	String getQualifiedName();

	ParameterValueType getParameterValueType();
	
	void setParameterValueType(ParameterValueType parameterValueType);

	boolean isArray();
	
	boolean isBracketed();
	
	boolean isAssigned();

	int getArraySize();
	
	Object getValue();
	
	void putValue(Object value);
	
	void clearValue();
	
	Object[] getValues();

	List<?> getValueList();

	String getValueAsString();
	
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
