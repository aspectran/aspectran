package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class ExceptionParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine responseByContentTypes;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		responseByContentTypes = new ParameterDefine("responseByContentType", new ResponseByContentTypeParameters(), true);
	
		parameterDefines = new ParameterDefine[] {
				responseByContentTypes
		};
	}
	
	public ExceptionParameters() {
		super(ExceptionParameters.class.getName(), parameterDefines);
	}
	
	public ExceptionParameters(String plaintext) {
		super(ExceptionParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
