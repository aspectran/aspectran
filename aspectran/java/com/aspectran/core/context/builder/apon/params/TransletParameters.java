package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class TransletParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine name;
	public static final ParameterDefine request;
	public static final ParameterDefine contents1;
	public static final ParameterDefine contents2;
	public static final ParameterDefine responses;
	public static final ParameterDefine exception;
	public static final ParameterDefine actions;
	public static final ParameterDefine transforms;
	public static final ParameterDefine dispatchs;
	public static final ParameterDefine redirects;
	public static final ParameterDefine forwards;

	private static final ParameterDefine[] parameterDefines;
	
	static {
		name = new ParameterDefine("name", ParameterValueType.STRING);
		request = new ParameterDefine("request", RequestParameters.class);
		contents1 = new ParameterDefine("contents", ContentParameters.class);
		contents2 = new ParameterDefine("content", ContentParameters.class, true);
		responses = new ParameterDefine("response", ResponseParameters.class, true);
		exception = new ParameterDefine("exception", ExceptionParameters.class, true);
		actions = new ParameterDefine("action", ActionParameters.class, true);
		dispatchs = new ParameterDefine("dispatch", DispatchParameters.class, true);
		transforms = new ParameterDefine("transform", TransformParameters.class, true);
		redirects = new ParameterDefine("redirect", RedirectParameters.class, true);
		forwards = new ParameterDefine("forward", ForwardParameters.class, true);
		
		parameterDefines = new ParameterDefine[] {
				name,
				request,
				contents1,
				contents2,
				responses,
				exception,
				actions,
				dispatchs,
				transforms,
				redirects,
				forwards
		};
	}
	
	public TransletParameters() {
		super(TransletParameters.class.getName(), parameterDefines);
	}
	
	public TransletParameters(String plaintext) {
		super(TransletParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
