package com.aspectran.core.var.apon;


public interface Parameters {

	public Object getValue(String name);
	
	public String getString(String name);

	public String getString(String name, String defaultValue);
	
	public int getInt(String name, int defaultValue);
	
	public boolean getBoolean(String name, boolean defaultValue);
	
	public String[] getStringArray(String name);
	
	public Parameters getParameters(String name);
	
	public Object getValue(ParameterValue parameterValue);
	
	public String getString(ParameterValue parameterValue);

	public String getString(ParameterValue parameterValue, String defaultValue);
	
	public int getInt(ParameterValue parameterValue, int defaultValue);
	
	public boolean getBoolean(ParameterValue parameterValue, boolean defaultValue);

	public String[] getStringArray(ParameterValue parameterValue);
	
	public Parameters getParameters(ParameterValue parameterValue);
	
}
