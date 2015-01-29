package com.aspectran.core.var.apon;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractParameters implements Parameters {

	protected Map<String, ParameterDefine> parameterDefineMap;
	
	private final String title;
	
	private String text;
	
	private ParameterDefine parent;
	
	protected AbstractParameters(String title, ParameterDefine[] parameterDefines) {
		this(title, parameterDefines, null);
	}

	protected AbstractParameters(String title, ParameterDefine[] parameterDefines, String text) {
		this.title = title;
		this.text = text;
		
		if(text != null) {
			AponReader reader = new AponReader(this);
			this.parameterDefineMap = reader.read(text, parameterDefines);
		} else {
			this.parameterDefineMap = new LinkedHashMap<String, ParameterDefine>();
			
			if(parameterDefines != null) {
				for(ParameterDefine pd : parameterDefines) {
					pd.setHolder(this);
					parameterDefineMap.put(pd.getName(), pd);
				}
			}
		}
	}
	
	protected Map<String, ParameterDefine> getParameterDefineMap() {
		return parameterDefineMap;
	}

	public ParameterDefine[] getParameterDefines() {
		Collection<ParameterDefine> values = parameterDefineMap.values();
		return values.toArray(new ParameterDefine[values.size()]);
	}
	
	public void addParameterDefine(ParameterDefine parameterDefine) {
		parameterDefineMap.put(parameterDefine.getName(), parameterDefine);
	}
	
	public ParameterDefine getParent() {
		return parent;
	}

	public void setParent(ParameterDefine parent) {
		this.parent = parent;
	}

	public String getTitle() {
		return title;
	}

	public String getQualifiedName() {
		if(parent != null)
			return parent.getQualifiedName();
		
		return title;
	}

	public String[] getParameterNames() {
		String[] names = new String[parameterDefineMap.size()];
		
		Iterator<String> iter = parameterDefineMap.keySet().iterator();
		int i = 0;
		
		while(iter.hasNext()) {
			names[i++] = iter.next();
		}
		
		return names;
	}

	public Set<String> getParameterNameSet() {
		return parameterDefineMap.keySet();
	}
	
	public Parameter getParameter(String name) {
		Parameter p = parameterDefineMap.get(name);
		
		if(p == null)
			throw new UnknownParameterException(name, this);
		
		return p;
	}
	
	public Parameter getParameter(ParameterDefine parameterDefine) {
		return getParameter(parameterDefine.getName());
	}
	
	public Object getValue(String name) {
		Parameter p = getParameter(name);
		return p.getValue();
	}
	
	public Object getValue(ParameterDefine parameter) {
		return getValue(parameter.getName());
	}
	
	public String getString(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsString();
	}

	public String getString(String name, String defaultValue) {
		String s = getString(name);
		
		if(s == null)
			return defaultValue;
		
		return s;
	}

	public String[] getStringArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsStringArray();
	}

	public String getString(ParameterDefine parameter) {
		return getString(parameter.getName());
	}
	
	public String getString(ParameterDefine parameter, String defaultValue) {
		return getString(parameter.getName(), defaultValue);
	}

	public String[] getStringArray(ParameterDefine parameter) {
		return getStringArray(parameter.getName());
	}
	
	public String getText(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsText();
	}
	
	public int getInt(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsInt();
	}
	
	public int getInt(String name, int defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsInt();
	}

	public int[] getIntArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsIntArray();
	}

	public int getInt(ParameterDefine parameter) {
		return getInt(parameter.getName());
	}

	public int getInt(ParameterDefine parameter, int defaultValue) {
		return getInt(parameter.getName(), defaultValue);
	}

	public int[] getIntArray(ParameterDefine parameter) {
		return getIntArray(parameter.getName());
	}

	public List<Integer> getIntList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsIntList();
	}

	public List<Integer> getIntList(ParameterDefine parameter) {
		return getIntList(parameter.getName());
	}

	public long getLong(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsLong();
	}
	
	public long getLong(String name, long defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsLong();
	}
	
	public long[] getLongArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsLongArray();
	}
	
	public long getLong(ParameterDefine parameter) {
		return getLong(parameter.getName());
	}
	
	public long getLong(ParameterDefine parameter, long defaultValue) {
		return getLong(parameter.getName());
	}
	
	public long[] getLongArray(ParameterDefine parameter) {
		return getLongArray(parameter.getName());
	}
	
	public List<Long> getLongList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsLongList();
	}

	public List<Long> getLongList(ParameterDefine parameter) {
		return getLongList(parameter.getName());
	}
	
	public float getFloat(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsFloat();
	}
	
	public float getFloat(String name, float defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsFloat();
	}

	public float[] getFloatArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsFloatArray();
	}
	
	public float getFloat(ParameterDefine parameter) {
		return getFloat(parameter.getName());
	}
	
	public float getFloat(ParameterDefine parameter, float defaultValue) {
		return getFloat(parameter.getName(), defaultValue);
	}
	
	public float[] getFloatArray(ParameterDefine parameter) {
		return getFloatArray(parameter.getName());
	}

	public List<Float> getFloatList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsFloatList();
	}

	public List<Float> getFloatList(ParameterDefine parameter) {
		return getFloatList(parameter.getName());
	}

	public double getDouble(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsDouble();
	}
	
	public double getDouble(String name, double defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsDouble();
	}

	public double[] getDoubleArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsDoubleArray();
	}
	
	public double getDouble(ParameterDefine parameter) {
		return getDouble(parameter.getName());
	}
	
	public double getDouble(ParameterDefine parameter, double defaultValue) {
		return getDouble(parameter.getName(), defaultValue);
	}
	
	public double[] getDoubleArray(ParameterDefine parameter) {
		return getDoubleArray(parameter.getName());
	}
	
	public List<Double> getDoubleList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsDoubleList();
	}

	public List<Double> getDoubleList(ParameterDefine parameter) {
		return getDoubleList(parameter.getName());
	}
	
	public boolean getBoolean(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsBoolean();
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsBoolean();
	}
	
	public boolean[] getBooleanArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsBooleanArray();
	}
	
	public boolean getBoolean(ParameterDefine parameter) {
		return getBoolean(parameter.getName());
	}
	
	public boolean getBoolean(ParameterDefine parameter, boolean defaultValue) {
		return getBoolean(parameter.getName(), defaultValue);
	}
	
	public boolean[] getBooleanArray(ParameterDefine parameter) {
		return getBooleanArray(parameter.getName());
	}
	
	public List<Boolean> getBooleanList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsBooleanList();
	}

	public List<Boolean> getBooleanList(ParameterDefine parameter) {
		return getBooleanList(parameter.getName());
	}

	public Parameters getParameters(String name) {
		Parameter p = getParameter(name);
		return (Parameters)p.getValue();
	}

	public Parameters getParameters(ParameterDefine parameter) {
		return getParameters(parameter.getName());
	}
	
	public Parameters[] getParametersArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsParametersArray();
	}
	
	public Parameters[] getParametersArray(ParameterDefine parameter) {
		return getParametersArray(parameter.getName());
	}
	
	public List<Parameters> getParametersList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsParametersList();
	}
	
	public List<Parameters> getParametersList(ParameterDefine parameter) {
		return getParametersList(parameter.getName());
	}
	
	public String toText() {
		return text;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{title=").append(title);
		sb.append(", qualifiedName=").append(getQualifiedName());
		sb.append("}");
		
		return sb.toString();
	}
	
}
