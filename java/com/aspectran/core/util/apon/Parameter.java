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
