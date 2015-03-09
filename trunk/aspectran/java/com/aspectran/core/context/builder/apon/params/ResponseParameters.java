package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ResponseParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine name;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine transform;
	public static final ParameterDefine dispatch;
	public static final ParameterDefine forward;
	public static final ParameterDefine redirect;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		name = new ParameterDefine("name", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		transform = new ParameterDefine("transform", TransformParameters.class);
		dispatch = new ParameterDefine("dispatch", DispatchParameters.class);
		forward = new ParameterDefine("forward", ForwardParameters.class);
		redirect = new ParameterDefine("redirect", RedirectParameters.class);
	
		parameterDefines = new ParameterDefine[] {
				name,
				characterEncoding,
				transform,
				dispatch,
				forward,
				redirect
		};
	}
	
	public ResponseParameters() {
		super(ResponseParameters.class.getName(), parameterDefines);
	}
	
	public ResponseParameters(String plaintext) {
		super(ResponseParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
