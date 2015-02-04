package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class ExceptionParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine responseByContentType;
	public static final ParameterDefine defaultResponse;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		responseByContentType = new ParameterDefine("responseByContentType", new ResponseByContentTypeParameters());
		defaultResponse = new ParameterDefine("defaultResponse", new DefaultResponseParameters());
	
		parameterDefines = new ParameterDefine[] {
				responseByContentType,
				defaultResponse
		};
	}
	
	public ExceptionParameters() {
		super(ExceptionParameters.class.getName(), parameterDefines);
	}
	
	public ExceptionParameters(String plaintext) {
		super(ExceptionParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
