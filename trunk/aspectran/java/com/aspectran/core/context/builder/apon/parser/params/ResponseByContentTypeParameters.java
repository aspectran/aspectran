package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ResponseByContentTypeParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine exceptionType;
	public static final ParameterDefine transforms;
	public static final ParameterDefine dispatchs;
	
	private static final ParameterDefine[] parameterDefines;

	
	static {
		exceptionType = new ParameterDefine("exceptionType", ParameterValueType.STRING);
		dispatchs = new ParameterDefine("dispatch", new DispatchParameters(), true);
		transforms = new ParameterDefine("transform", new TransformParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				exceptionType,
				dispatchs,
				transforms
		};
	}
	
	public ResponseByContentTypeParameters() {
		super(ResponseByContentTypeParameters.class.getName(), parameterDefines);
	}
	
	public ResponseByContentTypeParameters(String plaintext) {
		super(ResponseByContentTypeParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
