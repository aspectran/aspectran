package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class ExceptionRaizedParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine actions;
	public static final ParameterDefine responseByContentType;
	public static final ParameterDefine defaultResponse;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		actions = new ParameterDefine("action", new ActionParameters(), true);
		responseByContentType = new ParameterDefine("responseByContentType", new ResponseByContentTypeParameters());
		defaultResponse = new ParameterDefine("defaultResponse", new DefaultResponseParameters());
		
		parameterDefines = new ParameterDefine[] {
				actions,
				responseByContentType,
				defaultResponse
		};
	}
	
	public ExceptionRaizedParameters() {
		super(ExceptionRaizedParameters.class.getName(), parameterDefines);
	}
	
	public ExceptionRaizedParameters(String plaintext) {
		super(ExceptionRaizedParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
