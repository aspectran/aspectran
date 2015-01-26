package com.aspectran.core.var.apon;

public class ParameterHolder {

	private static final String PARAMETER_NAME = "item";
	
	private final GenericParameters parameters;
	
	public ParameterHolder(String text, Parameters parameters, boolean array) {
		ParameterDefine[] parameterDefines = new ParameterDefine[] { new ParameterDefine(PARAMETER_NAME, parameters, array) };
		this.parameters = new GenericParameters("holder", parameterDefines);
		this.parameters.parseText(text);
	}
	
	public Parameters[] getParametersArray() {
		return parameters.getParametersArray(PARAMETER_NAME);
	}

	public Parameters getParameters() {
		return parameters.getParameters(PARAMETER_NAME);
	}
	
}
