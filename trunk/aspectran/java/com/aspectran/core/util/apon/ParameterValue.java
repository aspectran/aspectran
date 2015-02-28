package com.aspectran.core.util.apon;

import java.util.ArrayList;
import java.util.List;

public class ParameterValue implements Parameter {

	private final String name;
	
	private ParameterValueType parameterValueType;
	
	private Class<? extends AbstractParameters> parametersClass;
	
	private boolean array;
	
	private boolean bracketed;
	
	private final boolean predefined;
	
	private Object value;
	
	private List<Object> list;
	
	private Parameters holder;
	
	private boolean assigned;
	
	public ParameterValue(String name, ParameterValueType parameterType) {
		this(name, parameterType, false);
	}
	
	public ParameterValue(String name, ParameterValueType parameterValueType, boolean array) {
		this(name, parameterValueType, array, false);
	}

	protected ParameterValue(String name, ParameterValueType parameterValueType, boolean array, boolean predefined) {
		this.name = name;
		this.parameterValueType = parameterValueType;
		this.array = array;
		this.predefined = predefined;

		if(array)
			this.bracketed = true;
	}
	
	public ParameterValue(String name, Class<? extends AbstractParameters> parametersClass) {
		this(name, parametersClass, false);
	}
	
	public ParameterValue(String name, Class<? extends AbstractParameters> parametersClass, boolean array) {
		this(name, parametersClass, array, false);
	}
	
	protected ParameterValue(String name, Class<? extends AbstractParameters> parametersClass, boolean array, boolean predefined) {
		this.name = name;
		this.parameterValueType = ParameterValueType.PARAMETERS;
		this.parametersClass = parametersClass;
		this.array = array;
		this.predefined = predefined;

		if(array)
			this.bracketed = true;
	}
	
	protected Parameters getHolder() {
		return holder;
	}

	protected void setHolder(Parameters holder) {
		this.holder = holder;
	}

	public String getName() {
		return name;
	}

	public String getQualifiedName() {
		if(holder == null)
			return name;
		
		ParameterValue parent = holder.getParent();
		
		if(parent != null)
			return parent.getQualifiedName() + "." + name;
		
		if(holder.getTitle() == null)
			return name;
		
		return holder.getTitle() + "." + name;
	}

	public ParameterValueType getParameterValueType() {
		return parameterValueType;
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
	
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if(array) {
			list = (List<Object>)value;
			if(this.value != null)
				this.value = null;
		} else {
			this.value = value;
			if(this.list != null)
				this.list = null;
		}
		
		assigned = true;
	}
	
	public void putValue(Object value) {
		if(!predefined && !array && this.value != null) {
			addValue(this.value);
			addValue(value);
			this.value = null;
			array = true;
			bracketed = false;
		} else {
			if(array) {
				addValue(value);
			} else {
				this.value = value;
				assigned = true;
			}
		}
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

		if(value instanceof Parameters)
			return ((Parameters)value).toText();
		
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
	
//	public String getValueAsText() {
//		if(array) {
//			if(list == null)
//				return null;
//			
//			StringBuilder sb = new StringBuilder();
//			
//			for(int i = 0; i < list.size(); i++) {
//				sb.append(list.get(i).toString()).append("\n");
//			}
//			
//			return sb.toString();
//		} else {
//			return getValueAsString();
//		}
//	}

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

	//@SuppressWarnings("unchecked")
	protected Parameters newParameters() {
		if(parameterValueType == ParameterValueType.VARIABLE) {
			parameterValueType = ParameterValueType.PARAMETERS;
			parametersClass = GenericParameters.class;
		} else {
			checkParameterValueType(ParameterValueType.PARAMETERS);
			if(parametersClass == null)
				parametersClass = GenericParameters.class;
		}

		//System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&: this.individual: " + this.individual + ", individual: " + individual);

		//if(list == null)
		//	return (Parameters)value;
		
		//Class<? extends AbstractParameters> type = (Class<? extends AbstractParameters>)value.getClass();
		
		try {
			Parameters p = (Parameters)parametersClass.newInstance();
			p.setParent(this);
			putValue(p);
			return p;
		} catch(Exception e) {
			throw new InvalidParameterException(e);
		}
	}
/*
	protected Parameters touchValueAsParameters() {
		if(value == null && parameterValueType == ParameterValueType.VARIABLE) {
			parameterValueType = ParameterValueType.PARAMETERS;
			parametersClass = GenericParameters.class;
			value = new GenericParameters();
		} else {
			checkParameterValueType(ParameterValueType.PARAMETERS);
		}
		
		
		
		return (Parameters)value;
	}
*/
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
			sb.append(", parametersClass=").append(parametersClass);
		sb.append(", array=").append(array);
		if(array)
			sb.append(", arraySize=").append(getArraySize());
		sb.append(", qualifiedName=").append(getQualifiedName());
		sb.append("}");
		
		return sb.toString();
	}

}
