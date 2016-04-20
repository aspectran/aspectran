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
import java.util.Map;
import java.util.Set;

public interface Parameters {
	
	Map<String, ParameterValue> getParameterValueMap();

	void setPrototype(Parameter parent);

	Parameter getPrototype();
	
	String getQualifiedName();
	
	Parameter getParent();
	
	String[] getParameterNames();
	
	Set<String> getParameterNameSet();

	boolean hasParameter(String name);

	boolean hasParameter(ParameterDefine parameterDefine);

	Parameter getParameter(String name);
	
	Parameter getParameter(ParameterDefine parameterDefine);
	
	Object getValue(String name);

	Object getValue(ParameterDefine parameterDefine);
	
	void putValue(String name, Object value);
	
	void putValue(ParameterDefine parameterDefine, Object value);
	
	void putValueNonNull(String name, Object value);

	void putValueNonNull(ParameterDefine parameterDefine, Object value);
	
	String getString(String name);

	String getString(String name, String defaultValue);

	String[] getStringArray(String name);

	String getString(ParameterDefine parameterDefine);
	
	String getString(ParameterDefine parameterDefine, String defaultValue);
	
	String[] getStringArray(ParameterDefine parameterDefine);
	
	List<String> getStringList(String name);

	List<String> getStringList(ParameterDefine parameterDefine);
	
	Integer getInt(String name);
	
	int getInt(String name, int defaultValue);
	
	Integer[] getIntArray(String name);
	
	Integer getInt(ParameterDefine parameterDefine);
	
	int getInt(ParameterDefine parameterDefine, int defaultValue);
	
	Integer[] getIntArray(ParameterDefine parameterDefine);
	
	List<Integer> getIntList(String name);
	
	List<Integer> getIntList(ParameterDefine parameterDefine);
	
	Long getLong(String name);
	
	long getLong(String name, long defaultValue);
	
	Long[] getLongArray(String name);
	
	Long getLong(ParameterDefine parameterDefine);
	
	long getLong(ParameterDefine parameterDefine, long defaultValue);
	
	Long[] getLongArray(ParameterDefine parameterDefine);
	
	List<Long> getLongList(String name);
	
	List<Long> getLongList(ParameterDefine parameterDefine);

	Float getFloat(String name);
	
	float getFloat(String name, float defaultValue);
	
	Float[] getFloatArray(String name);
	
	Float getFloat(ParameterDefine parameterDefine);
	
	float getFloat(ParameterDefine parameterDefine, float defaultValue);
	
	Float[] getFloatArray(ParameterDefine parameterDefine);
	
	List<Float> getFloatList(String name);
	
	List<Float> getFloatList(ParameterDefine parameterDefine);

	Double getDouble(String name);
	
	double getDouble(String name, double defaultValue);
	
	Double[] getDoubleArray(String name);

	Double getDouble(ParameterDefine parameterDefine);
	
	double getDouble(ParameterDefine parameterDefine, double defaultValue);
	
	Double[] getDoubleArray(ParameterDefine parameterDefine);
	
	List<Double> getDoubleList(String name);
	
	List<Double> getDoubleList(ParameterDefine parameterDefine);

	Boolean getBoolean(String name);
	
	boolean getBoolean(String name, boolean defaultValue);

	Boolean[] getBooleanArray(String name);

	Boolean getBoolean(ParameterDefine parameterDefine);
	
	boolean getBoolean(ParameterDefine parameterDefine, boolean defaultValue);
	
	Boolean[] getBooleanArray(ParameterDefine parameterDefine);
	
	List<Boolean> getBooleanList(String name);
	
	List<Boolean> getBooleanList(ParameterDefine parameterDefine);

	<T extends Parameters> T getParameters(String name);

	<T extends Parameters> T[] getParametersArray(String name);
	
	<T extends Parameters> T getParameters(ParameterDefine parameterDefine);
	
	<T extends Parameters> T[] getParametersArray(ParameterDefine parameterDefine);
	
	<T extends Parameters> List<T> getParametersList(String name);
	
	<T extends Parameters> List<T> getParametersList(ParameterDefine parameterDefine);
	
	ParameterValue newParameterValue(String name, ParameterValueType parameterValueType);
	
	ParameterValue newParameterValue(String name, ParameterValueType parameterValueType, boolean array);
	
	<T extends Parameters> T newParameters(String name);
	
	<T extends Parameters> T newParameters(ParameterDefine parameterDefine);
	
	<T extends Parameters> T touchParameters(String name);
	
	<T extends Parameters> T touchParameters(ParameterDefine parameterDefine);
	
	boolean isAddable();
	
	String describe();

	String describe(boolean details);
	
}
