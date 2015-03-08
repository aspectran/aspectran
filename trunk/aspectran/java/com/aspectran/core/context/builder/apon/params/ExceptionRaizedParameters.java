package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class ExceptionRaizedParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine action;
	public static final ParameterDefine responseByContentTypes;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		action = new ParameterDefine("action", ActionParameters.class);
		responseByContentTypes = new ParameterDefine("responseByContentType", ResponseByContentTypeParameters.class, true, true);
		
		parameterDefines = new ParameterDefine[] {
				action,
				responseByContentTypes
		};
	}
	
	public ExceptionRaizedParameters() {
		super(ExceptionRaizedParameters.class.getName(), parameterDefines);
	}
	
	public ExceptionRaizedParameters(String plaintext) {
		super(ExceptionRaizedParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
