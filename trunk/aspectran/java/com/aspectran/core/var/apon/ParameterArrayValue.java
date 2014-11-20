package com.aspectran.core.var.apon;

public class ParameterArrayValue extends ParameterValue {

	public ParameterArrayValue(String name, ParameterValueType parameterType) {
		super(name, parameterType, true, null);
	}
	
	public ParameterArrayValue(String name, Parameters parameters) {
		super(name, ParameterValueType.PARAMETERS, true, parameters);
	}
	
}
