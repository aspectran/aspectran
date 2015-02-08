package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ResponseParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine name;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine transforms;
	public static final ParameterDefine dispatchs;
	public static final ParameterDefine redirects;
	public static final ParameterDefine forwards;
	
	private static final ParameterDefine[] parameterDefines;

	
	static {
		name = new ParameterDefine("name", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		dispatchs = new ParameterDefine("dispatch", new DispatchParameters(), true);
		transforms = new ParameterDefine("transform", new TransformParameters(), true);
		redirects = new ParameterDefine("redirect", new RedirectParameters(), true);
		forwards = new ParameterDefine("forward", new ForwardParameters(), true);
	
		parameterDefines = new ParameterDefine[] {
				name,
				characterEncoding,
				dispatchs,
				transforms,
				redirects,
				forwards
		};
	}
	
	public ResponseParameters() {
		super(ResponseParameters.class.getName(), parameterDefines);
	}
	
	public ResponseParameters(String plaintext) {
		super(ResponseParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
