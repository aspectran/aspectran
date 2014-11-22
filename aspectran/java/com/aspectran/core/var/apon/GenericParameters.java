package com.aspectran.core.var.apon;


public class GenericParameters extends AbstractParameters {

	public GenericParameters(String title, String plaintext) {
		super(title, null, plaintext);
	}

	public GenericParameters(String title, ParameterValue[] parameterValues) {
		super(title, parameterValues);
	}

}
