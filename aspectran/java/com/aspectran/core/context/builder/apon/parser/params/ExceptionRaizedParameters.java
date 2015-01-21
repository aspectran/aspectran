package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ExceptionRaizedParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine actions;
	public static final ParameterDefine responseByContentType;
	
	private static final ParameterDefine[] parameterDefines;

	
	static {
		actions = new ParameterDefine("action", new ActionParameters(), true);
		responseByContentType = new ParameterDefine("responseByContentType", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				actions,
				responseByContentType
		};
	}
	
	public ExceptionRaizedParameters() {
		super(ExceptionRaizedParameters.class.getName(), parameterDefines);
	}
	
	public ExceptionRaizedParameters(String plaintext) {
		super(ExceptionRaizedParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
