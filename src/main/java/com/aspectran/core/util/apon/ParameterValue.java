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

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.util.ToStringBuilder;

public class ParameterValue implements Parameter {

	private Parameters container;
	
	private final String name;
	
	private ParameterValueType parameterValueType;
	
	private Class<? extends AbstractParameters> parametersClass;
	
	private boolean array;
	
	private boolean bracketed;
	
	private final boolean predefined;
	
	private Object value;
	
	private List<Object> list;
	
	private boolean assigned;
	
	public ParameterValue(String name, ParameterValueType parameterValueType) {
		this(name, parameterValueType, false);
	}

	public ParameterValue(String name, ParameterValueType parameterValueType, boolean array) {
		this(name, parameterValueType, array, false);
	}
	
	public ParameterValue(String name, ParameterValueType parameterValueType, boolean array, boolean noBracket) {
		this(name, parameterValueType, array, noBracket, false);
	}
	
	protected ParameterValue(String name, ParameterValueType parameterValueType, boolean array, boolean noBracket, boolean predefined) {
		this.name = name;
		this.parameterValueType = parameterValueType;
		this.array = array;
		this.predefined = (predefined && parameterValueType != ParameterValueType.VARIABLE);
		if (this.array && !noBracket) {
			this.bracketed = true;
		}
	}
	
	public ParameterValue(String name, Class<? extends AbstractParameters> parametersClass) {
		this(name, parametersClass, false);
	}
	
	public ParameterValue(String name, Class<? extends AbstractParameters> parametersClass, boolean array) {
		this(name, parametersClass, array, false);
	}
	
	public ParameterValue(String name, Class<? extends AbstractParameters> parametersClass, boolean array, boolean noBracket) {
		this(name, parametersClass, array, noBracket, false);
	}
	
	protected ParameterValue(String name, Class<? extends AbstractParameters> parametersClass, boolean array, boolean noBracket, boolean predefined) {
		this.name = name;
		this.parameterValueType = ParameterValueType.PARAMETERS;
		this.parametersClass = parametersClass;
		this.array = array;
		this.predefined = predefined;
		if (this.array && !noBracket) {
			this.bracketed = true;
		}
	}
	
	@Override
	public Parameters getContainer() {
		return container;
	}

	public void setContainer(Parameters container) {
		this.container = container;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getQualifiedName() {
		if (container == null) {
			return name;
		}
		Parameter prototype = container.getPrototype();
		if (prototype != null) {
			return prototype.getQualifiedName() + "." + name;
		}
		return name;
	}

	@Override
	public ParameterValueType getParameterValueType() {
		return parameterValueType;
	}

	@Override
	public void setParameterValueType(ParameterValueType parameterValueType) {
		this.parameterValueType = parameterValueType;
	}

	@Override
	public boolean isArray() {
		return array;
	}

	@Override
	public boolean isBracketed() {
		return bracketed;
	}

	public void setBracketed(boolean bracketed) {
		this.bracketed = bracketed;
	}

	public boolean isPredefined() {
		return predefined;
	}

	@Override
	public boolean isAssigned() {
		return assigned;
	}

	@Override
	public int getArraySize() {
		return (list != null ? list.size() : 0);
	}
	
	@Override
	public void putValue(Object value) {
		if (!predefined && value != null) {
			if (parameterValueType == ParameterValueType.STRING) {
				if (value.toString().indexOf(AponFormat.NEXT_LINE_CHAR) != -1) {
					parameterValueType = ParameterValueType.TEXT;
				}
			} else if (parameterValueType == ParameterValueType.VARIABLE && value instanceof String) {
				if (value.toString().indexOf(AponFormat.NEXT_LINE_CHAR) != -1) {
					parameterValueType = ParameterValueType.TEXT;
				} else {
					parameterValueType = ParameterValueType.STRING;
				}
			}
		}
		if (!predefined && !array && this.value != null) {
			addValue(this.value);
			addValue(value);
			this.value = null;
			array = true;
			bracketed = true;
		} else {
			if (array) {
				addValue(value);
			} else {
				this.value = value;
				assigned = true;
			}
		}
	}
	
	@Override
	public void clearValue() {
		value = null;
		list = null;
		assigned = false;
	}
	
	private synchronized void addValue(Object value) {
		if (list == null) {
			list = new ArrayList<Object>();
			assigned = true;
		}
		list.add(value);
	}

	@Override
	public Object getValue() {
		return (array ? list : value);
	}

	@Override
	public List<?> getValueList() {
		if (!predefined && value != null && list == null && parameterValueType == ParameterValueType.VARIABLE) {
			List<Object> list = new ArrayList<Object>();
			list.add(value);
			return list;
		}
		return list;
	}
	
	@Override
	public Object[] getValues() {
		return (list != null ? list.toArray(new Object[list.size()]) : null);
	}

	@Override
	public String getValueAsString() {
		return (value != null ? value.toString() : null);
	}
	
	@Override
	public String[] getValueAsStringArray() {
		if (array) {
			if (list == null) {
				return null;
			}
			String[] s = new String[list.size()];
			for (int i = 0; i < s.length; i++) {
				s[i] = list.get(i).toString();
			}
			return s;
		} else {
			if (value == null) {
				return null;
			}
			return new String[] { value.toString() };
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<String> getValueAsStringList() {
		if (list == null) {
			return null;
		}
		if (parameterValueType == ParameterValueType.STRING) {
			return (List<String>)getValueList();
		}
		List<String> list2 = new ArrayList<String>();
		for (Object o : list) {
			list2.add(o.toString());
		}
		return list2;
	}

	@Override
	public Integer getValueAsInt() {
		checkParameterValueType(ParameterValueType.INT);
		return (Integer)value;
	}
	
	@Override
	public Integer[] getValueAsIntArray() {
		List<Integer> intList = getValueAsIntList();
		return (intList != null ? intList.toArray(new Integer[intList.size()]): null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getValueAsIntList() {
		checkParameterValueType(ParameterValueType.INT);
		return (List<Integer>)getValueList();
	}
	
	@Override
	public Long getValueAsLong() {
		checkParameterValueType(ParameterValueType.LONG);
		return (Long)value;
	}
	
	@Override
	public Long[] getValueAsLongArray() {
		List<Long> longList = getValueAsLongList();
		return (longList != null ? longList.toArray(new Long[longList.size()]) : null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Long> getValueAsLongList() {
		checkParameterValueType(ParameterValueType.LONG);
		return (List<Long>)getValueList();
	}
	
	@Override
	public Float getValueAsFloat() {
		checkParameterValueType(ParameterValueType.FLOAT);
		return (Float)value;
	}
	
	@Override
	public Float[] getValueAsFloatArray() {
		List<Float> floatList = getValueAsFloatList();
		return (floatList != null ? floatList.toArray(new Float[floatList.size()]) : null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Float> getValueAsFloatList() {
		checkParameterValueType(ParameterValueType.FLOAT);
		return (List<Float>)getValueList();
	}
	
	@Override
	public Double getValueAsDouble() {
		checkParameterValueType(ParameterValueType.DOUBLE);
		return (Double)value;
	}
	
	@Override
	public Double[] getValueAsDoubleArray() {
		List<Double> doubleList = getValueAsDoubleList();
		return (doubleList != null ? doubleList.toArray(new Double[doubleList.size()]) : null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Double> getValueAsDoubleList() {
		checkParameterValueType(ParameterValueType.DOUBLE);
		return (List<Double>)getValueList();
	}

	@Override
	public Boolean getValueAsBoolean() {
		checkParameterValueType(ParameterValueType.BOOLEAN);
		return (Boolean)value;
	}

	@Override
	public Boolean[] getValueAsBooleanArray() {
		List<Boolean> booleanList = getValueAsBooleanList();
		return (booleanList != null ? booleanList.toArray(new Boolean[booleanList.size()]) : null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Boolean> getValueAsBooleanList() {
		checkParameterValueType(ParameterValueType.BOOLEAN);
		return (List<Boolean>)getValueList();
	}
	
	@Override
	public Parameters getValueAsParameters() {
		checkParameterValueType(ParameterValueType.PARAMETERS);
		return (Parameters)value;
	}

	@Override
	public Parameters[] getValueAsParametersArray() {
		List<Parameters> parametersList = getValueAsParametersList();
		return (parametersList != null ? parametersList.toArray(new Parameters[parametersList.size()]) : null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Parameters> getValueAsParametersList() {
		if (parameterValueType != ParameterValueType.PARAMETERS)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);
		
		return (List<Parameters>)getValueList();
	}

	@Override
	public Parameters newParameters(Parameter prototype) {
		if (parameterValueType == ParameterValueType.VARIABLE) {
			parameterValueType = ParameterValueType.PARAMETERS;
			parametersClass = VariableParameters.class;
		} else {
			checkParameterValueType(ParameterValueType.PARAMETERS);
			if (parametersClass == null)
				parametersClass = VariableParameters.class;
		}

		try {
			Parameters p = parametersClass.newInstance();
			p.setPrototype(prototype);
			putValue(p);
			return p;
		} catch (Exception e) {
			throw new InvalidParameterException("Could not instantiate parameters class " + parametersClass, e);
		}
	}

	private void checkParameterValueType(ParameterValueType parameterValueType) {
		if (this.parameterValueType != ParameterValueType.VARIABLE && this.parameterValueType != parameterValueType)
			throw new IncompatibleParameterValueTypeException(this, parameterValueType);
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("name", name);
		tsb.append("parameterValueType", parameterValueType);
		if (parameterValueType == ParameterValueType.PARAMETERS) {
			tsb.append("parametersClass", parametersClass);
		}
		tsb.append("array", array);
		tsb.append("bracketed", bracketed);
		if (array) {
			tsb.append("arraySize", getArraySize());
		}
		tsb.append("qualifiedName", getQualifiedName());
		return tsb.toString();
	}

}
