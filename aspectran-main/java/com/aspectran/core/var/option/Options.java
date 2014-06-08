package com.aspectran.core.var.option;

import java.util.Map;

public interface Options {

	public Map<String, Option> getOptionMap();
	
	public Object getValue(String name);
	
	public String getString(String name);

	public String getString(String name, String defaultValue);
	
	public int getInt(String name, int defaultValue);
	
	public boolean getBoolean(String name, boolean defaultValue);
	
	public String[] getStringArray(String name);
	
	public Object getValue(Option option);
	
	public String getString(Option option);

	public String getString(Option option, String defaultValue);
	
	public int getInt(Option option, int defaultValue);
	
	public boolean getBoolean(Option option, boolean defaultValue);

	public String[] getStringArray(Option option);
	
}
