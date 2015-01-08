package com.aspectran.core.var.apon;


public interface Parameters {

	public void setParent(ParameterDefine parent);

	public ParameterDefine getParent();
	
	public Object getTitle();

	public String getQualifiedName();
	
	public Parameter getParameter(String name);
	
	public Parameter getParameter(ParameterDefine parameterDefine);
	
	public Object getValue(String name);

	public Object getValue(ParameterDefine parameter);
	
	public String getString(String name);

	public String getString(String name, String defaultValue);

	public String[] getStringArray(String name);

	public String getString(ParameterDefine parameter);
	
	public String getString(ParameterDefine parameter, String defaultValue);
	
	public String[] getStringArray(ParameterDefine parameter);
	
	public int getInt(String name);
	
	public int getInt(String name, int defaultValue);
	
	public int[] getIntArray(String name);
	
	public int getInt(ParameterDefine parameter);
	
	public int getInt(ParameterDefine parameter, int defaultValue);
	
	public int[] getIntArray(ParameterDefine parameter);
	
	public long getLong(String name);
	
	public long getLong(String name, long defaultValue);
	
	public long[] getLongArray(String name);
	
	public long getLong(ParameterDefine parameter);
	
	public long getLong(ParameterDefine parameter, long defaultValue);
	
	public long[] getLongArray(ParameterDefine parameter);
	
	public float getFloat(String name);
	
	public float getFloat(String name, float defaultValue);
	
	public float[] getFloatArray(String name);
	
	public float getFloat(ParameterDefine parameter);
	
	public float getFloat(ParameterDefine parameter, float defaultValue);
	
	public float[] getFloatArray(ParameterDefine parameter);
	
	public double getDouble(String name);
	
	public double getDouble(String name, double defaultValue);
	
	public double[] getDoubleArray(String name);

	public double getDouble(ParameterDefine parameter);
	
	public double getDouble(ParameterDefine parameter, double defaultValue);
	
	public double[] getDoubleArray(ParameterDefine parameter);
	
	public boolean getBoolean(String name);
	
	public boolean getBoolean(String name, boolean defaultValue);

	public boolean[] getBooleanArray(String name);

	public boolean getBoolean(ParameterDefine parameter);
	
	public boolean getBoolean(ParameterDefine parameter, boolean defaultValue);
	
	public boolean[] getBooleanArray(ParameterDefine parameter);
	
	public Parameters getParameters(String name);

	public Parameters[] getParametersArray(String name);
	
	public Parameters getParameters(ParameterDefine parameter);
	
	public Parameters[] getParametersArray(ParameterDefine parameter);
	
}
