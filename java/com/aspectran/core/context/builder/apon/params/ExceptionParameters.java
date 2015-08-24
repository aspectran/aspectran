package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class ExceptionParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine responseByContentTypes;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		responseByContentTypes = new ParameterDefine("responseByContentType", ResponseByContentTypeParameters.class, true, true);
	
		parameterDefines = new ParameterDefine[] {
				responseByContentTypes
		};
	}
	
	public ExceptionParameters() {
		super(parameterDefines);
	}
	
	public ExceptionParameters(String text) {
		super(parameterDefines, text);
	}
	
}
