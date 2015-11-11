/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.util.apon;

import java.util.List;

public interface Parameter {

	public Parameters getContainer();
	
	public String getName();

	public String getQualifiedName();

	public ParameterValueType getParameterValueType();
	
	public void setParameterValueType(ParameterValueType parameterValueType);

	public boolean isArray();
	
	public boolean isBracketed();
	
	public boolean isAssigned();

	public int getArraySize();
	
	public Object getValue();
	
	public void putValue(Object value);
	
	public void clearValue();
	
	public Object[] getValues();

	public List<?> getValueList();

	public String getValueAsString();
	
	public String[] getValueAsStringArray();

	public List<String> getValueAsStringList();
	
	public Integer getValueAsInt();

	public Integer[] getValueAsIntArray();

	public List<Integer> getValueAsIntList();

	public Long getValueAsLong();
	
	public Long[] getValueAsLongArray();

	public List<Long> getValueAsLongList();

	public Float getValueAsFloat();
	
	public Float[] getValueAsFloatArray();

	public List<Float> getValueAsFloatList();

	public Double getValueAsDouble();

	public Double[] getValueAsDoubleArray();
	
	public List<Double> getValueAsDoubleList();
	
	public Boolean getValueAsBoolean();

	public Boolean[] getValueAsBooleanArray();
	
	public List<Boolean> getValueAsBooleanList();
	
	public Parameters getValueAsParameters();

	public Parameters[] getValueAsParametersArray();

	public List<Parameters> getValueAsParametersList();
	
	public Parameters newParameters(Parameter prototype);

}
