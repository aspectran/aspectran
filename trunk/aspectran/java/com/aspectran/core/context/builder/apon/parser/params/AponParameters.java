package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AponParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine useFor;
	
	public static final ParameterDefine text;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		useFor = new ParameterDefine("for", ParameterValueType.STRING);
		text = new ParameterDefine("text", ParameterValueType.STRING, true);
		
		parameterDefines = new ParameterDefine[] {
				useFor,
				text
		};
	}
	
	public AponParameters() {
		super(AponParameters.class.getName(), parameterDefines);
	}
	
	public AponParameters(String plaintext) {
		super(AponParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
