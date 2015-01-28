package com.aspectran.core.var.apon;

import java.util.List;

public interface Parameter {

	public String getName();

	public String getQualifiedName();

	public ParameterValueType getParameterValueType();

	public boolean isArray();

	public int getArraySize();
	
	public Object getValue();
	
	public void setValue(Object value);
	
	public Object[] getValues();

	public List<?> getValueList();

	public String getValueAsString();
	
	public String[] getValueAsStringArray();

	public int getValueAsInt();

	public int[] getValueAsIntArray();

	public long getValueAsLong();
	
	public long[] getValueAsLongArray();

	public float getValueAsFloat();
	
	public float[] getValueAsFloatArray();

	public double getValueAsDouble();

	public double[] getValueAsDoubleArray();
	
	public boolean getValueAsBoolean();

	public boolean[] getValueAsBooleanArray();
	
	public Parameters getParameters();

	public Parameters[] getParametersArray();

	public List<Parameters> getParametersList();

}
