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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

public abstract class AbstractParameters implements Parameters {

	private Map<String, ParameterValue> parameterValueMap;
	
	private Parameter prototype;
	
	private final boolean addable;
	
	protected AbstractParameters(ParameterDefine[] parameterDefines) {
		this.parameterValueMap = new LinkedHashMap<>();
		
		if(parameterDefines != null) {
			for(ParameterDefine pd : parameterDefines) {
				ParameterValue pv = pd.newParameterValue();
				pv.setContainer(this);
				parameterValueMap.put(pd.getName(), pv);
			}
			addable = false;
		} else {
			addable = true;
		}
	}

	protected AbstractParameters(ParameterDefine[] parameterDefines, String text) {
		this(parameterDefines);
		
		if(text != null) {
			try {
				AponReader aponReader = new AponReader(text);
				aponReader.read(this);
				aponReader.close();
			} catch(IOException e) {
				throw new AponReadFailedException(e);
			}
		}
	}

	@Override
	public Parameter getPrototype() {
		return prototype;
	}

	@Override
	public void setPrototype(Parameter prototype) {
		this.prototype = prototype;
	}

	@Override
	public String getQualifiedName() {
		if(prototype != null)
			return prototype.getQualifiedName();
		
		return this.getClass().getName();
	}

	@Override
	public Parameter getParent() {
		if(prototype != null)
			if(prototype.getContainer() != null)
				if(prototype.getContainer().getPrototype() != null)
					return prototype.getContainer().getPrototype();
		return null;
	}

	@Override
	public Map<String, ParameterValue> getParameterValueMap() {
		return parameterValueMap;
	}
	
	@Override
	public String[] getParameterNames() {
		return parameterValueMap.keySet().toArray(new String[parameterValueMap.size()]);
	}

	@Override
	public Set<String> getParameterNameSet() {
		return parameterValueMap.keySet();
	}

	@Override
	public boolean hasParameter(String name) {
		return (parameterValueMap.get(name) != null);
	}

	@Override
	public boolean hasParameter(ParameterDefine parameterDefine) {
		return hasParameter(parameterDefine.getName());
	}

	@Override
	public Parameter getParameter(String name) {
		Parameter p = parameterValueMap.get(name);
		if(!addable && p == null) {
			throw new UnknownParameterException(name, this);
		}
		return p;
	}

	@Override
	public Parameter getParameter(ParameterDefine parameterDefine) {
		return getParameter(parameterDefine.getName());
	}

	@Override
	public Object getValue(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValue() : null);
	}

	@Override
	public Object getValue(ParameterDefine parameterDefine) {
		return getValue(parameterDefine.getName());
	}

	@Override
	public void putValue(String name, Object value) {
		Parameter p = getParameter(name);
		if(p == null) {
			p = newParameterValue(name, ParameterValueType.determineType(value));
		}
		p.putValue(value);
	}

	@Override
	public void putValue(ParameterDefine parameterDefine, Object value) {
		putValue(parameterDefine.getName(), value);
	}
	
	@Override
	public void putValueNonNull(String name, Object value) {
		if(value != null) {
			putValue(name, value);
		}
	}

	@Override
	public void putValueNonNull(ParameterDefine parameterDefine, Object value) {
		if(value != null) {
			putValue(parameterDefine.getName(), value);
		}
	}
	
	@Override
	public String getString(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsString() : null);
	}

	@Override
	public String getString(String name, String defaultValue) {
		String s = getString(name);
		return (s != null ? s : defaultValue);
	}

	@Override
	public String getString(ParameterDefine parameterDefine) {
		return getString(parameterDefine.getName());
	}

	@Override
	public String getString(ParameterDefine parameterDefine, String defaultValue) {
		return getString(parameterDefine.getName(), defaultValue);
	}

	@Override
	public String[] getStringArray(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsStringArray() : null);
	}

	@Override
	public String[] getStringArray(ParameterDefine parameterDefine) {
		return getStringArray(parameterDefine.getName());
	}

	@Override
	public List<String> getStringList(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsStringList() : null);
	}

	@Override
	public List<String> getStringList(ParameterDefine parameterDefine) {
		return getStringList(parameterDefine.getName());
	}

	@Override
	public Integer getInt(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsInt() : null);
	}

	@Override
	public int getInt(String name, int defaultValue) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsInt() : defaultValue);
	}

	@Override
	public Integer[] getIntArray(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsIntArray() : null);
	}

	@Override
	public Integer getInt(ParameterDefine parameterDefine) {
		return getInt(parameterDefine.getName());
	}

	@Override
	public int getInt(ParameterDefine parameterDefine, int defaultValue) {
		return getInt(parameterDefine.getName(), defaultValue);
	}

	@Override
	public Integer[] getIntArray(ParameterDefine parameterDefine) {
		return getIntArray(parameterDefine.getName());
	}

	@Override
	public List<Integer> getIntList(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsIntList() : null);
	}

	@Override
	public List<Integer> getIntList(ParameterDefine parameterDefine) {
		return getIntList(parameterDefine.getName());
	}

	@Override
	public Long getLong(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsLong() : null);
	}

	@Override
	public long getLong(String name, long defaultValue) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsLong() : defaultValue);
	}

	@Override
	public Long[] getLongArray(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsLongArray() : null);
	}

	@Override
	public Long getLong(ParameterDefine parameterDefine) {
		return getLong(parameterDefine.getName());
	}

	@Override
	public long getLong(ParameterDefine parameterDefine, long defaultValue) {
		return getLong(parameterDefine.getName());
	}

	@Override
	public Long[] getLongArray(ParameterDefine parameterDefine) {
		return getLongArray(parameterDefine.getName());
	}

	@Override
	public List<Long> getLongList(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsLongList() : null);
	}

	@Override
	public List<Long> getLongList(ParameterDefine parameterDefine) {
		return getLongList(parameterDefine.getName());
	}

	@Override
	public Float getFloat(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsFloat() : null);
	}

	@Override
	public float getFloat(String name, float defaultValue) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsFloat() : defaultValue);
	}

	@Override
	public Float[] getFloatArray(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsFloatArray() : null);
	}

	@Override
	public Float getFloat(ParameterDefine parameterDefine) {
		return getFloat(parameterDefine.getName());
	}

	@Override
	public float getFloat(ParameterDefine parameterDefine, float defaultValue) {
		return getFloat(parameterDefine.getName(), defaultValue);
	}

	@Override
	public Float[] getFloatArray(ParameterDefine parameterDefine) {
		return getFloatArray(parameterDefine.getName());
	}

	@Override
	public List<Float> getFloatList(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsFloatList() : null);
	}

	@Override
	public List<Float> getFloatList(ParameterDefine parameterDefine) {
		return getFloatList(parameterDefine.getName());
	}

	@Override
	public Double getDouble(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsDouble() : null);
	}

	@Override
	public double getDouble(String name, double defaultValue) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsDouble() : defaultValue);
	}

	@Override
	public Double[] getDoubleArray(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsDoubleArray() : null);
	}

	@Override
	public Double getDouble(ParameterDefine parameterDefine) {
		return getDouble(parameterDefine.getName());
	}

	@Override
	public double getDouble(ParameterDefine parameterDefine, double defaultValue) {
		return getDouble(parameterDefine.getName(), defaultValue);
	}

	@Override
	public Double[] getDoubleArray(ParameterDefine parameterDefine) {
		return getDoubleArray(parameterDefine.getName());
	}

	@Override
	public List<Double> getDoubleList(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsDoubleList() : null);
	}

	@Override
	public List<Double> getDoubleList(ParameterDefine parameterDefine) {
		return getDoubleList(parameterDefine.getName());
	}

	@Override
	public Boolean getBoolean(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsBoolean() : null);
	}

	@Override
	public boolean getBoolean(String name, boolean defaultValue) {
		Parameter p = getParameter(name);
		return (p != null ? BooleanUtils.toBoolean(p.getValueAsBoolean(), defaultValue) : defaultValue);
	}

	@Override
	public Boolean[] getBooleanArray(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsBooleanArray() : null);
	}

	@Override
	public Boolean getBoolean(ParameterDefine parameterDefine) {
		return getBoolean(parameterDefine.getName());
	}

	@Override
	public boolean getBoolean(ParameterDefine parameterDefine, boolean defaultValue) {
		return getBoolean(parameterDefine.getName(), defaultValue);
	}

	@Override
	public Boolean[] getBooleanArray(ParameterDefine parameterDefine) {
		return getBooleanArray(parameterDefine.getName());
	}

	@Override
	public List<Boolean> getBooleanList(String name) {
		Parameter p = getParameter(name);
		return (p != null ? p.getValueAsBooleanList() : null);
	}

	@Override
	public List<Boolean> getBooleanList(ParameterDefine parameterDefine) {
		return getBooleanList(parameterDefine.getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Parameters> T getParameters(String name) {
		Parameter p = getParameter(name);
		return (p != null ? (T)p.getValue() : null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Parameters> T getParameters(ParameterDefine parameterDefine) {
		return (T)getParameters(parameterDefine.getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Parameters> T[] getParametersArray(String name) {
		Parameter p = getParameter(name);
		return (p != null ? (T[])p.getValueAsParametersArray() : null);
	}

	@Override
	public <T extends Parameters> T[] getParametersArray(ParameterDefine parameterDefine) {
		return getParametersArray(parameterDefine.getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Parameters> List<T> getParametersList(String name) {
		Parameter p = getParameter(name);
		return (p != null ? (List<T>)p.getValueAsParametersList() : null);
	}

	@Override
	public <T extends Parameters> List<T> getParametersList(ParameterDefine parameterDefine) {
		return getParametersList(parameterDefine.getName());
	}

	@Override
	public ParameterValue newParameterValue(String name, ParameterValueType parameterValueType) {
		return newParameterValue(name, parameterValueType, false);
	}

	@Override
	public ParameterValue newParameterValue(String name, ParameterValueType parameterValueType, boolean array) {
		ParameterValue pv = new ParameterValue(name, parameterValueType, array);
		pv.setContainer(this);
		parameterValueMap.put(name, pv);
		return pv;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Parameters> T newParameters(String name) {
		Parameter p = getParameter(name);
		if(p == null) {
			throw new UnknownParameterException(name, this);
		}
		Parameters parameters = p.newParameters(p);
		return (T)parameters;
}

	@Override
	public <T extends Parameters> T newParameters(ParameterDefine parameterDefine) {
		return newParameters(parameterDefine.getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Parameters> T touchParameters(String name) {
		Parameters parameters = getParameters(name);
		if(parameters == null) {
			parameters = newParameters(name);
		}
		return (T)parameters;
	}

	@Override
	public <T extends Parameters> T touchParameters(ParameterDefine parameterDefine) {
		return touchParameters(parameterDefine.getName());
	}

	@Override
	public boolean isAddable() {
		return addable;
	}

	@Override
	public String describe() {
		return describe(false);
	}

	@Override
	public String describe(boolean details) {
		ToStringBuilder tsb = new ToStringBuilder();
		if(details) {
			tsb.append("qualifiedName", getQualifiedName());
			tsb.append("parameters", parameterValueMap);
			tsb.append("parent", getParent());
		} else {
			tsb.append(parameterValueMap);
		}
		return tsb.toString();
	}

	@Override
	public String toString() {
		return AponWriter.stringify(this);
	}
	
}
