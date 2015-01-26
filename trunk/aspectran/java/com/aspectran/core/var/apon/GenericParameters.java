package com.aspectran.core.var.apon;


public class GenericParameters extends AbstractParameters {

	public GenericParameters() {
		super(null, null, null);
	}
	
	public GenericParameters(String text) {
		super(null, null, text);
	}

	public GenericParameters(String title, String text) {
		super(title, null, text);
	}

	public GenericParameters(String title, ParameterDefine[] parameterDefines) {
		super(title, parameterDefines);
	}
	
	public static Parameters toParameters(String text) {
		Parameters parameters = new GenericParameters(text);
		return parameters;
	}

}
