package com.aspectran.core.var.apon;


public interface Parameters {

	public void setParent(ParameterValue parent);

	public ParameterValue getParent();
	
	public Object getTitle();

	public String getQualifiedName();
	
	public ParameterValue getParameter(String name);
	
	public Object getValue(String name);

	public Object getValue(ParameterValue parameter);
	
	public String getString(String name);

	public String getString(String name, String defaultValue);

	public String[] getStringArray(String name);

	public String getString(ParameterValue parameter);
	
	public String getString(ParameterValue parameter, String defaultValue);
	
	public String[] getStringArray(ParameterValue parameter);
	
	public int getInt(String name);
	
	public int getInt(String name, int defaultValue);
	
	public int[] getIntArray(String name);
	
	public int getInt(ParameterValue parameter);
	
	public int getInt(ParameterValue parameter, int defaultValue);
	
	public int[] getIntArray(ParameterValue parameter);
	
	public long getLong(String name);
	
	public long getLong(String name, long defaultValue);
	
	public long[] getLongArray(String name);
	
	public long getLong(ParameterValue parameter);
	
	public long getLong(ParameterValue parameter, long defaultValue);
	
	public long[] getLongArray(ParameterValue parameter);
	
	public float getFloat(String name);
	
	public float getFloat(String name, float defaultValue);
	
	public float[] getFloatArray(String name);
	
	public float getFloat(ParameterValue parameter);
	
	public float getFloat(ParameterValue parameter, float defaultValue);
	
	public float[] getFloatArray(ParameterValue parameter);
	
	public double getDouble(String name);
	
	public double getDouble(String name, double defaultValue);
	
	public double[] getDoubleArray(String name);

	public double getDouble(ParameterValue parameter);
	
	public double getDouble(ParameterValue parameter, double defaultValue);
	
	public double[] getDoubleArray(ParameterValue parameter);
	
	public boolean getBoolean(String name);
	
	public boolean getBoolean(String name, boolean defaultValue);

	public boolean[] getBooleanArray(String name);

	public boolean getBoolean(ParameterValue parameter);
	
	public boolean getBoolean(ParameterValue parameter, boolean defaultValue);
	
	public boolean[] getBooleanArray(ParameterValue parameter);
	
	public Parameters getParameters(String name);

	public Parameters[] getParametersArray(String name);
	
	public Parameters getParameters(ParameterValue parameter);
	
	public Parameters[] getParametersArray(ParameterValue parameter);
	
}
