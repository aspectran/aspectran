package com.aspectran.core.util.apon;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.util.BooleanUtils;

public abstract class AbstractParameters implements Parameters {

	protected Map<String, ParameterValue> parameterValueMap;
	
	private final String title;
	
	private String text;
	
	private ParameterValue parent;
	
	protected AbstractParameters(String title, ParameterDefine[] parameterDefines) {
		this(title, parameterDefines, null);
	}

	protected AbstractParameters(String title, ParameterDefine[] parameterDefines, String text) {
		this.title = title;
		this.text = text;
		this.parameterValueMap = new LinkedHashMap<String, ParameterValue>();
		
		if(parameterDefines != null) {
			for(ParameterDefine pd : parameterDefines) {
				ParameterValue pv = pd.newParameterValue();
				pv.setHolder(this);
				parameterValueMap.put(pd.getName(), pv);
				//System.out.println("&& ParameterValue=" + pv);
			}
		}
		
		if(text != null) {
			AponReader reader = new AponReader();
			reader.read(text, parameterValueMap);
		}
	}
	
	public Map<String, ParameterValue> getParameterValueMap() {
		return parameterValueMap;
	}
	
	public void addParameterValue(ParameterValue parameterValue) {
		parameterValueMap.put(parameterValue.getName(), parameterValue);
	}
	
	public ParameterValue getParent() {
		return parent;
	}

	public void setParent(ParameterValue parent) {
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
		String[] names = new String[parameterValueMap.size()];
		
		Iterator<String> iter = parameterValueMap.keySet().iterator();
		int i = 0;
		
		while(iter.hasNext()) {
			names[i++] = iter.next();
		}
		
		return names;
	}

	public Set<String> getParameterNameSet() {
		return parameterValueMap.keySet();
	}
	
	public Parameter getParameter(String name) {
		Parameter p = parameterValueMap.get(name);
		
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
	
	public Object getValue(ParameterDefine parameterDefine) {
		return getValue(parameterDefine.getName());
	}
	
	public void setValue(String name, Object value) {
		Parameter p = getParameter(name);
		p.setValue(value);
	}
	
	public void setValue(ParameterDefine parameterDefine, Object value) {
		setValue(parameterDefine.getName(), value);
	}
	
	public void putValue(String name, Object value) {
		Parameter p = getParameter(name);
		p.putValue(value);
	}
	
	public void putValue(ParameterDefine parameterDefine, Object value) {
		putValue(parameterDefine.getName(), value);
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

	public String getString(ParameterDefine parameterDefine) {
		return getString(parameterDefine.getName());
	}
	
	public String getString(ParameterDefine parameterDefine, String defaultValue) {
		return getString(parameterDefine.getName(), defaultValue);
	}

	public String[] getStringArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsStringArray();
	}

	public String[] getStringArray(ParameterDefine parameterDefine) {
		return getStringArray(parameterDefine.getName());
	}
	
	public List<String> getStringList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsStringList();
	}

	public List<String> getStringList(ParameterDefine parameterDefine) {
		return getStringList(parameterDefine.getName());
	}
	
	public String getText(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsText();
	}
	
	public String getText(ParameterDefine parameterDefine) {
		return getText(parameterDefine.getName());
	}
	
	public Integer getInt(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsInt();
	}
	
	public int getInt(String name, int defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsInt();
	}

	public Integer[] getIntArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsIntArray();
	}

	public Integer getInt(ParameterDefine parameterDefine) {
		return getInt(parameterDefine.getName());
	}

	public int getInt(ParameterDefine parameterDefine, int defaultValue) {
		return getInt(parameterDefine.getName(), defaultValue);
	}

	public Integer[] getIntArray(ParameterDefine parameterDefine) {
		return getIntArray(parameterDefine.getName());
	}

	public List<Integer> getIntList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsIntList();
	}

	public List<Integer> getIntList(ParameterDefine parameterDefine) {
		return getIntList(parameterDefine.getName());
	}

	public Long getLong(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsLong();
	}
	
	public long getLong(String name, long defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsLong();
	}
	
	public Long[] getLongArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsLongArray();
	}
	
	public Long getLong(ParameterDefine parameterDefine) {
		return getLong(parameterDefine.getName());
	}
	
	public long getLong(ParameterDefine parameterDefine, long defaultValue) {
		return getLong(parameterDefine.getName());
	}
	
	public Long[] getLongArray(ParameterDefine parameterDefine) {
		return getLongArray(parameterDefine.getName());
	}
	
	public List<Long> getLongList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsLongList();
	}

	public List<Long> getLongList(ParameterDefine parameterDefine) {
		return getLongList(parameterDefine.getName());
	}
	
	public Float getFloat(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsFloat();
	}
	
	public float getFloat(String name, float defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsFloat();
	}

	public Float[] getFloatArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsFloatArray();
	}
	
	public Float getFloat(ParameterDefine parameterDefine) {
		return getFloat(parameterDefine.getName());
	}
	
	public float getFloat(ParameterDefine parameterDefine, float defaultValue) {
		return getFloat(parameterDefine.getName(), defaultValue);
	}
	
	public Float[] getFloatArray(ParameterDefine parameterDefine) {
		return getFloatArray(parameterDefine.getName());
	}

	public List<Float> getFloatList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsFloatList();
	}

	public List<Float> getFloatList(ParameterDefine parameterDefine) {
		return getFloatList(parameterDefine.getName());
	}

	public Double getDouble(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsDouble();
	}
	
	public double getDouble(String name, double defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsDouble();
	}

	public Double[] getDoubleArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsDoubleArray();
	}
	
	public Double getDouble(ParameterDefine parameterDefine) {
		return getDouble(parameterDefine.getName());
	}
	
	public double getDouble(ParameterDefine parameterDefine, double defaultValue) {
		return getDouble(parameterDefine.getName(), defaultValue);
	}
	
	public Double[] getDoubleArray(ParameterDefine parameterDefine) {
		return getDoubleArray(parameterDefine.getName());
	}
	
	public List<Double> getDoubleList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsDoubleList();
	}

	public List<Double> getDoubleList(ParameterDefine parameterDefine) {
		return getDoubleList(parameterDefine.getName());
	}
	
	public Boolean getBoolean(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsBoolean();
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return BooleanUtils.toBoolean(p.getValueAsBoolean(), defaultValue);
	}
	
	public Boolean[] getBooleanArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsBooleanArray();
	}
	
	public Boolean getBoolean(ParameterDefine parameterDefine) {
		return getBoolean(parameterDefine.getName());
	}
	
	public boolean getBoolean(ParameterDefine parameterDefine, boolean defaultValue) {
		return getBoolean(parameterDefine.getName(), defaultValue);
	}
	
	public Boolean[] getBooleanArray(ParameterDefine parameterDefine) {
		return getBooleanArray(parameterDefine.getName());
	}
	
	public List<Boolean> getBooleanList(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsBooleanList();
	}

	public List<Boolean> getBooleanList(ParameterDefine parameterDefine) {
		return getBooleanList(parameterDefine.getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Parameters> T getParameters(String name) {
		Parameter p = getParameter(name);
		return (T)p.getValue();
	}

	@SuppressWarnings("unchecked")
	public <T extends Parameters> T getParameters(ParameterDefine parameterDefine) {
		return (T)getParameters(parameterDefine.getName());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Parameters> T[] getParametersArray(String name) {
		Parameter p = getParameter(name);
		return (T[])p.getValueAsParametersArray();
	}
	
	public <T extends Parameters> T[] getParametersArray(ParameterDefine parameterDefine) {
		return getParametersArray(parameterDefine.getName());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Parameters> List<T> getParametersList(String name) {
		Parameter p = getParameter(name);
		return (List<T>)p.getValueAsParametersList();
	}
	
	public <T extends Parameters> List<T> getParametersList(ParameterDefine parameterDefine) {
		return getParametersList(parameterDefine.getName());
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
