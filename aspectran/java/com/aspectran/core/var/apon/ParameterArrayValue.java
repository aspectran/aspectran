package com.aspectran.core.var.apon;

public class ParameterArrayValue extends ParameterValue {

	private ParameterValueType parameterValueType;
	
	public ParameterArrayValue(String name, ParameterValueType parameterType) {
		super(name, parameterType, true, null);
	}
	
	public ParameterArrayValue(String name, Parameters parameters) {
		super(name, ParameterValueType.PARAMETERS, true, parameters);
	}

	public ParameterValueType getParameterValueType() {
		if(parameterValueType == null)
			return super.getParameterValueType();
		
		return parameterValueType;
	}

	public void setParameterValueType(ParameterValueType parameterValueType) {
		this.parameterValueType = parameterValueType;
	}
	
}
