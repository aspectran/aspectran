/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

		//if(this.array && !noBracket && (parameterValueType == ParameterValueType.PARAMETERS || parameterValueType == ParameterValueType.VARIABLE))
		if(this.array && !noBracket)
			this.bracketed = true;
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

		if(this.array && !noBracket)
			this.bracketed = true;
	}
	
	public Parameters getContainer() {
		return container;
	}

	public void setContainer(Parameters container) {
		this.container = container;
	}

	public String getName() {
		return name;
	}

	public String getQualifiedName() {
		if(container == null)
			return name;
		
		Parameter prototype = container.getPrototype();
		
		if(prototype != null)
			return prototype.getQualifiedName() + "." + name;
		
		return name;
	}

	public ParameterValueType getParameterValueType() {
		return parameterValueType;
	}

	public void setParameterValueType(ParameterValueType parameterValueType) {
		this.parameterValueType = parameterValueType;
	}

	public boolean isArray() {
		return array;
	}

	public boolean isBracketed() {
		return bracketed;
	}

	public void setBracketed(boolean bracketed) {
		this.bracketed = bracketed;
	}

	public boolean isPredefined() {
		return predefined;
	}

	public boolean isAssigned() {
		return assigned;
	}

	public int getArraySize() {
		if(list == null)
			return 0;
		
		return list.size();
	}
	
	public void putValue(Object value) {
		if(!predefined) {
			if(parameterValueType == ParameterValueType.STRING) {
				if(value.toString().indexOf(AponFormat.NEXT_LINE_CHAR) != -1)
					parameterValueType = ParameterValueType.TEXT;
			} else if(parameterValueType == ParameterValueType.VARIABLE && value instanceof String) {
				if(value.toString().indexOf(AponFormat.NEXT_LINE_CHAR) != -1) {
					parameterValueType = ParameterValueType.TEXT;
				} else {
					parameterValueType = ParameterValueType.STRING;
				}
			}
		}

		if(!predefined && !array && this.value != null) {
			addValue(this.value);
			addValue(value);
			this.value = null;
			array = true;
			bracketed = true;
		} else {
			if(array) {
				addValue(value);
			} else {
				this.value = value;
				assigned = true;
			}
		}
	}
	
	public void clearValue() {
		value = null;
		list = null;
		assigned = false;
	}
	
	private synchronized void addValue(Object value) {
		if(list == null) {
			list = new ArrayList<Object>();
			assigned = true;
		}
		
		list.add(value);
	}

	public Object getValue() {
		if(array)
			return list;
		else
			return value;
	}

	public List<?> getValueList() {
		if(!predefined && value != null && list == null && parameterValueType == ParameterValueType.VARIABLE) {
			List<Object> list = new ArrayList<Object>();
			list.add(value);
			return list;
		}
		
		return list;
	}
	
	public Object[] getValues() {
		if(list == null)
			return null;

		return list.toArray(new Object[list.size()]);
	}

	public String getValueAsString() {
		if(value == null)
			return null;

		return value.toString();
	}
	
	public String[] getValueAsStringArray() {
		if(array) {
			if(list == null)
				return null;

			String[] s = new String[list.size()];
			
			for(int i = 0; i < s.length; i++) {
				s[i] = list.get(i).toString();
			}
			
			return s;
		} else {
			if(value == null)
				return null;

			return new String[] { value.toString() };
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getValueAsStringList() {
		if(list == null)
			return null;

		if(parameterValueType == ParameterValueType.STRING) {
			return (List<String>)getValueList();
		}
		
		List<String> list2 = new ArrayList<String>();
		
		for(Object o : list) {
			list2.add(o.toString());
		}
		
		return list2;
	}

	public Integer getValueAsInt() {
		checkParameterValueType(ParameterValueType.INT);

		if(value == null)
			return null;
		
		return (Integer)value;
	}
	
	public Integer[] getValueAsIntArray() {
		List<Integer> intList = getValueAsIntList();
		
		if(intList == null)
			return null;
		
		return (Integer[])intList.toArray(new Integer[intList.size()]);
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getValueAsIntList() {
		checkParameterValueType(ParameterValueType.INT);

		return (List<Integer>)getValueList();
	}
	
	public Long getValueAsLong() {
		checkParameterValueType(ParameterValueType.LONG);
		
		if(value == null)
			return null;
		
		return (Long)value;
	}
	
	public Long[] getValueAsLongArray() {
		List<Long> longList = getValueAsLongList();

		if(longList == null)
			return null;
		
		return (Long[])longList.toArray(new Long[longList.size()]);
	}

	@SuppressWarnings("unchecked")
	public List<Long> getValueAsLongList() {
		checkParameterValueType(ParameterValueType.LONG);

		return (List<Long>)getValueList();
	}
	
	public Float getValueAsFloat() {
		checkParameterValueType(ParameterValueType.FLOAT);

		if(value == null)
			return null;
		
		return (Float)value;
	}
	
	public Float[] getValueAsFloatArray() {
		List<Float> floatList = getValueAsFloatList();

		if(floatList == null)
			return null;
		
		return (Float[])floatList.toArray(new Float[floatList.size()]);
	}

	@SuppressWarnings("unchecked")
	public List<Float> getValueAsFloatList() {
		checkParameterValueType(ParameterValueType.FLOAT);

		return (List<Float>)getValueList();
	}
	
	public Double getValueAsDouble() {
		checkParameterValueType(ParameterValueType.DOUBLE);
		
		if(value == null)
			return null;
		
		return (Double)value;
	}
	
	public Double[] getValueAsDoubleArray() {
		List<Double> doubleList = getValueAsDoubleList();
		
		if(doubleList == null)
			return null;
		
		return (Double[])doubleList.toArray(new Double[doubleList.size()]);
	}
	
	@SuppressWarnings("unchecked")
	public List<Double> getValueAsDoubleList() {
		checkParameterValueType(ParameterValueType.DOUBLE);
		
		return (List<Double>)getValueList();
	}

	public Boolean getValueAsBoolean() {
		checkParameterValueType(ParameterValueType.BOOLEAN);
		return (Boolean)value;
	}

	public Boolean[] getValueAsBooleanArray() {
		List<Boolean> booleanList = getValueAsBooleanList();
		
		if(booleanList == null)
			return null;
		
		return (Boolean[])booleanList.toArray(new Boolean[booleanList.size()]);
	}
	
	@SuppressWarnings("unchecked")
	public List<Boolean> getValueAsBooleanList() {
		checkParameterValueType(ParameterValueType.BOOLEAN);

		return (List<Boolean>)getValueList();
	}
	
	public Parameters getValueAsParameters() {
		checkParameterValueType(ParameterValueType.PARAMETERS);
		
		return (Parameters)value;
	}

	public Parameters[] getValueAsParametersArray() {
		List<Parameters> parametersList = getValueAsParametersList();
		
		if(parametersList == null)
			return null;
		
		return parametersList.toArray(new Parameters[parametersList.size()]);
	}

	@SuppressWarnings("unchecked")
	public List<Parameters> getValueAsParametersList() {
		if(parameterValueType != ParameterValueType.PARAMETERS)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);
		
		return (List<Parameters>)getValueList();
	}

	public Parameters newParameters(Parameter prototype) {
		if(parameterValueType == ParameterValueType.VARIABLE) {
			parameterValueType = ParameterValueType.PARAMETERS;
			parametersClass = GenericParameters.class;
		} else {
			checkParameterValueType(ParameterValueType.PARAMETERS);
			if(parametersClass == null)
				parametersClass = GenericParameters.class;
		}

		try {
			Parameters p = (Parameters)parametersClass.newInstance();
			p.setPrototype(prototype);
			putValue(p);
			return p;
		} catch(Exception e) {
			throw new InvalidParameterException("Could not instantiate parameters class " + parametersClass, e);
		}
	}

	private void checkParameterValueType(ParameterValueType parameterValueType) {
		if(this.parameterValueType != ParameterValueType.VARIABLE && this.parameterValueType != parameterValueType)
			throw new IncompatibleParameterValueTypeException(this, parameterValueType);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name=").append(name);
		sb.append(", parameterValueType=").append(parameterValueType);
		if(parameterValueType == ParameterValueType.PARAMETERS)
			sb.append(", parametersClass=").append(parametersClass.getName());
		sb.append(", array=").append(array);
		sb.append(", bracketed=").append(bracketed);
		if(array)
			sb.append(", arraySize=").append(getArraySize());
		sb.append(", qualifiedName=").append(getQualifiedName());
		sb.append("}");
		
		return sb.toString();
	}

}
