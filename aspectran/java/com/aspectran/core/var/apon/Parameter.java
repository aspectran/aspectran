package com.aspectran.core.var.apon;

import java.util.List;

public interface Parameter {

	public String getName();

	public String getQualifiedName();

	public ParameterValueType getParameterValueType();

	public boolean isArray();

	public int getArraySize();
	
	public Object getValue();
	
	public void putValue(Object value);
	
	public Object[] getValues();

	public List<?> getValueList();

	public String getValueAsString();
	
	public String[] getValueAsStringArray();

	public String getValueAsText();
	
	public int getValueAsInt();

	public int[] getValueAsIntArray();

	public List<Integer> getValueAsIntList();

	public long getValueAsLong();
	
	public long[] getValueAsLongArray();

	public List<Long> getValueAsLongList();

	public float getValueAsFloat();
	
	public float[] getValueAsFloatArray();

	public List<Float> getValueAsFloatList();

	public double getValueAsDouble();

	public double[] getValueAsDoubleArray();
	
	public List<Double> getValueAsDoubleList();
	
	public boolean getValueAsBoolean();

	public boolean[] getValueAsBooleanArray();
	
	public List<Boolean> getValueAsBooleanList();
	
	public Parameters getValueAsParameters();

	public Parameters[] getValueAsParametersArray();

	public List<Parameters> getValueAsParametersList();

}
