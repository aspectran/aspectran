package com.aspectran.core.var.apon;

import java.util.ArrayList;
import java.util.List;

public class ParameterValue {

	private final String name;
	
	private Object value;
	
	private final ParameterValueType parameterValueType;
	
	private final boolean array;
	
	private final Parameters parameters;
	
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
		
		if(parameterValueType == ParameterValueType.PARAMETERS)
			this.parameters = parameters;
		else
			this.parameters = null;
	}

	public Object getValue() {
		return value;
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
	public void setValue(Object value) {
		if(array) {
			if(this.value == null) {
				this.value = new ArrayList<Object>();
			}
			((List<Object>)this.value).add(value);
		} else {
			this.value = value;
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

	public Parameters getParameters() {
		return parameters;
	}
	
}
