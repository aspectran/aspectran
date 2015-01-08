package com.aspectran.core.var.apon;


public class GenericParameters extends AbstractParameters {

	public GenericParameters(String plaintext) {
		super(null, null, plaintext);
	}

	public GenericParameters(String title, String plaintext) {
		super(title, null, plaintext);
	}

	public GenericParameters(String title, ParameterDefine[] parameterValues) {
		super(title, parameterValues);
	}
	
	public static Parameters toParameters(String plaintext) {
		Parameters parameters = new GenericParameters(plaintext);
		return parameters;
	}

}
