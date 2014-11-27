package com.aspectran.core.var.apon;


public interface Parameters {

	public void setParent(ParameterValue parent);

	public ParameterValue getParent();
	
	public Object getTitle();
	
	public String getString(String name);

	public String getString(String name, String defaultValue);

	public String[] getStringArray(String name);

	public int getInt(String name);
	
	public int getInt(String name, int defaultValue);
	
	public int[] getIntArray(String name);
	
	public long getLong(String name);
	
	public long getLong(String name, long defaultValue);
	
	public long[] getLongArray(String name);
	
	public float getFloat(String name);
	
	public float getFloat(String name, float defaultValue);
	
	public float[] getFloatArray(String name);
	
	public double getDouble(String name);
	
	public double getDouble(String name, double defaultValue);
	
	public double[] getDoubleArray(String name);

	public boolean getBoolean(String name);
	
	public boolean getBoolean(String name, boolean defaultValue);

	public boolean[] getBooleanArray(String name);

	public Parameters getParameters(String name);

	public Parameters[] getParametersArray(String name);
	
}
