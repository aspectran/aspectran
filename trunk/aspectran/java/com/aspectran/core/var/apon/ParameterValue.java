package com.aspectran.core.var.apon;

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
	
	protected ParameterValue(String name, ParameterValueType parameterValueType, boolean array, Parameters options) {
		this.name = name;
		this.parameterValueType = parameterValueType;
		this.array = array;
		
		if(parameterValueType == ParameterValueType.PARAMETERS)
			this.parameters = options;
		else
			this.parameters = null;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
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
