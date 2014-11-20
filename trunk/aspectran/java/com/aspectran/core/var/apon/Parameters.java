package com.aspectran.core.var.apon;


public interface Parameters {

	public Object getValue(String name);
	
	public String getString(String name);

	public String getString(String name, String defaultValue);
	
	public int getInt(String name, int defaultValue);
	
	public boolean getBoolean(String name, boolean defaultValue);
	
	public String[] getStringArray(String name);
	
	public Parameters getParameters(String name);
	
	public Object getValue(ParameterValue Parameter);
	
	public String getString(ParameterValue Parameter);

	public String getString(ParameterValue Parameter, String defaultValue);
	
	public int getInt(ParameterValue Parameter, int defaultValue);
	
	public boolean getBoolean(ParameterValue Parameter, boolean defaultValue);

	public String[] getStringArray(ParameterValue Parameter);
	
	public Parameters getParameters(ParameterValue Parameter);
	
}
