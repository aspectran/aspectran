package com.aspectran.core.util.apon;

import java.util.List;
import java.util.Set;


public interface Parameters {
	
	public ParameterDefine[] getParameterDefines();

	public void setParent(ParameterDefine parent);

	public ParameterDefine getParent();
	
	public Object getTitle();

	public String getQualifiedName();
	
	public String[] getParameterNames();
	
	public Set<String> getParameterNameSet();
	
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
	
	public List<String> getStringList(String name);

	public List<String> getStringList(ParameterDefine parameter);
	
	public String getText(String name);	
	
	public String getText(ParameterDefine parameter);	
	
	public Integer getInt(String name);
	
	public int getInt(String name, int defaultValue);
	
	public Integer[] getIntArray(String name);
	
	public Integer getInt(ParameterDefine parameter);
	
	public int getInt(ParameterDefine parameter, int defaultValue);
	
	public Integer[] getIntArray(ParameterDefine parameter);
	
	public List<Integer> getIntList(String name);
	
	public List<Integer> getIntList(ParameterDefine parameter);
	
	public Long getLong(String name);
	
	public long getLong(String name, long defaultValue);
	
	public Long[] getLongArray(String name);
	
	public Long getLong(ParameterDefine parameter);
	
	public long getLong(ParameterDefine parameter, long defaultValue);
	
	public Long[] getLongArray(ParameterDefine parameter);
	
	public List<Long> getLongList(String name);
	
	public List<Long> getLongList(ParameterDefine parameter);

	public Float getFloat(String name);
	
	public float getFloat(String name, float defaultValue);
	
	public Float[] getFloatArray(String name);
	
	public Float getFloat(ParameterDefine parameter);
	
	public float getFloat(ParameterDefine parameter, float defaultValue);
	
	public Float[] getFloatArray(ParameterDefine parameter);
	
	public List<Float> getFloatList(String name);
	
	public List<Float> getFloatList(ParameterDefine parameter);

	public Double getDouble(String name);
	
	public double getDouble(String name, double defaultValue);
	
	public Double[] getDoubleArray(String name);

	public Double getDouble(ParameterDefine parameter);
	
	public double getDouble(ParameterDefine parameter, double defaultValue);
	
	public Double[] getDoubleArray(ParameterDefine parameter);
	
	public List<Double> getDoubleList(String name);
	
	public List<Double> getDoubleList(ParameterDefine parameter);

	public Boolean getBoolean(String name);
	
	public boolean getBoolean(String name, boolean defaultValue);

	public Boolean[] getBooleanArray(String name);

	public Boolean getBoolean(ParameterDefine parameter);
	
	public boolean getBoolean(ParameterDefine parameter, boolean defaultValue);
	
	public Boolean[] getBooleanArray(ParameterDefine parameter);
	
	public List<Boolean> getBooleanList(String name);
	
	public List<Boolean> getBooleanList(ParameterDefine parameter);

	public Parameters getParameters(String name);

	public Parameters[] getParametersArray(String name);
	
	public Parameters getParameters(ParameterDefine parameter);
	
	public Parameters[] getParametersArray(ParameterDefine parameter);
	
	public List<Parameters> getParametersList(String name);
	
	public List<Parameters> getParametersList(ParameterDefine parameter);
	
}
