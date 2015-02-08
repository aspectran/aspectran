package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ResponseByContentTypeParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine exceptionType;
	public static final ParameterDefine transforms;
	public static final ParameterDefine dispatchs;
	public static final ParameterDefine redirects;
	public static final ParameterDefine forwards;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		exceptionType = new ParameterDefine("exceptionType", ParameterValueType.STRING);
		dispatchs = new ParameterDefine("dispatch", new DispatchParameters(), true);
		transforms = new ParameterDefine("transform", new TransformParameters(), true);
		redirects = new ParameterDefine("redirect", new RedirectParameters(), true);
		forwards = new ParameterDefine("forward", new ForwardParameters(), true);

		parameterDefines = new ParameterDefine[] {
				exceptionType,
				dispatchs,
				transforms,
				redirects,
				forwards
		};
	}
	
	public ResponseByContentTypeParameters() {
		super(ResponseByContentTypeParameters.class.getName(), parameterDefines);
	}
	
	public ResponseByContentTypeParameters(String plaintext) {
		super(ResponseByContentTypeParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
