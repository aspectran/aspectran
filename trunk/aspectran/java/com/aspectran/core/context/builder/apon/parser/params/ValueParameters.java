package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ValueParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine name;
	
	public static final ParameterDefine tokenize;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		name = new ParameterDefine("name", ParameterValueType.STRING);
		tokenize = new ParameterDefine("tokenize", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				name,
				tokenize
		};
	}
	
	public ValueParameters() {
		super(ValueParameters.class.getName(), parameterDefines);
	}
	
	public ValueParameters(String plaintext) {
		super(ValueParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
