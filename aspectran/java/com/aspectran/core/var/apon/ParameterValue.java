package com.aspectran.core.var.apon;

import java.util.ArrayList;
import java.util.List;

public class ParameterValue {

	private final String name;
	
	private Object value;
	
	private final ParameterValueType parameterValueType;
	
	private final boolean array;
	
	private Parameters parameters;
	
	private List<Parameters> parametersList;
	
	private Object lock = new Object();
	
	public ParameterValue(String name, ParameterValueType parameterType) {
		this(name, parameterType, false, null);
	}
	
	public ParameterValue(String name, Parameters parameters) {
		this(name, ParameterValueType.PARAMETERS, false, parameters);
	}
	
	protected ParameterValue(String name, ParameterValueType parameterValueType, boolean array, Parameters parameters) {
		this.name = name;
		this.parameterValueType = parameterValueType;
		this.array = array;
		
		if(parameterValueType == ParameterValueType.PARAMETERS) {
			this.parameters = parameters;
		}
	}

	public String getName() {
		return name;
	}

	public ParameterValueType getParameterValueType() {
		return parameterValueType;
	}

	public boolean isArray() {
		return array;
	}

	public Object getValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if(array) {
			synchronized(lock) {
				if(this.value == null) {
					this.value = new ArrayList<Object>();
				}
			}
			((List<Object>)this.value).add(value);
		} else {
			this.value = value;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Object[] getValues() {
		if(array) {
			return ((List<Object>)this.value).toArray(new Object[((List<Object>)this.value).size()]);
		} else {
			return new Object[] { this.value };
		}
	}

	@SuppressWarnings("unchecked")
	public String getString() {
		if(value == null)
			return null;

		if(array) {
			StringBuilder sb = new StringBuilder();
			
			for(int i = 0; i < ((List<Object>)value).size(); i++) {
				sb.append(((List<Object>)value).get(i).toString()).append("\n");
			}
			
			return sb.toString();
		} else {
			return value.toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	public String[] getStringArray() {
		if(value == null)
			return null;
		
		if(array) {
			String[] s = new String[((List<Object>)value).size()];
			
			for(int i = 0; i < s.length; i++) {
				s[i] = ((List<Object>)value).get(i).toString();
			}
			
			return s;
		} else {
			return new String[] { value.toString()};
		}
	}

	public int getInt() {
		if(value == null)
			return 0;
		
		if(parameterValueType != ParameterValueType.INTEGER)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.INTEGER);
		
		return ((Integer)value).intValue();
	}

	public long getLong() {
		if(value == null)
			return 0L;
		
		if(parameterValueType != ParameterValueType.LONG)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.LONG);
		
		return ((Long)value).longValue();
	}

	public float getFloat() {
		if(value == null)
			return 0.0F;
		
		if(parameterValueType != ParameterValueType.FLOAT)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.FLOAT);
		
		return ((Float)value).floatValue();
	}

	public double getDouble() {
		if(value == null)
			return 0.0D;
		
		if(parameterValueType != ParameterValueType.DOUBLE)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.DOUBLE);
		
		return ((Double)value).doubleValue();
	}
	
	public boolean getBoolean() {
		if(value == null)
			return false;
		
		if(parameterValueType != ParameterValueType.BOOLEAN)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.BOOLEAN);
		
		return ((Boolean)value).booleanValue();
	}
	
	public Parameters getParameters() {
		return parameters;
	}

	public void setParameters(Parameters parameters) {
		if(array) {
			addParameters(parameters);
		} else {
			this.parameters = parameters;
		}
	}

	public List<Parameters> getParametersList() {
		return parametersList;
	}

	public Parameters getParameters(int index) {
		if(parametersList == null)
			return null;
		
		return parametersList.get(index);
	}
	
	public void setParametersList(List<Parameters> parametersList) {
		this.parametersList = parametersList;
	}

	public void addParameters(Parameters parameters) {
		if(parametersList == null)
			parametersList = new ArrayList<Parameters>();
		
		parametersList.add(parameters);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{name=").append(name);
		sb.append(", parameterValueType=").append(parameterValueType);
		sb.append(", array=").append(array);
		sb.append("}");
		
		return sb.toString();
	}

}
