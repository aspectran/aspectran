package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ResponseByContentTypeParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine exceptionType;
	public static final ParameterDefine transforms;
	public static final ParameterDefine dispatchs;
	public static final ParameterDefine forwards;
	public static final ParameterDefine redirects;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		exceptionType = new ParameterDefine("exceptionType", ParameterValueType.STRING);
		transforms = new ParameterDefine("transform", TransformParameters.class, true, true);
		dispatchs = new ParameterDefine("dispatch", DispatchParameters.class, true, true);
		forwards = new ParameterDefine("forward", ForwardParameters.class, true, true);
		redirects = new ParameterDefine("redirect", RedirectParameters.class, true, true);

		parameterDefines = new ParameterDefine[] {
				exceptionType,
				transforms,
				dispatchs,
				forwards,
				redirects
		};
	}
	
	public ResponseByContentTypeParameters() {
		super(parameterDefines);
	}
	
	public ResponseByContentTypeParameters(String text) {
		super(parameterDefines, text);
	}
	
}
