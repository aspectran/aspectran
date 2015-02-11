package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class TransletParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine request;
	public static final ParameterDefine contents;
	public static final ParameterDefine responses;
	public static final ParameterDefine exception;
	public static final ParameterDefine actions;
	public static final ParameterDefine transforms;
	public static final ParameterDefine dispatchs;
	public static final ParameterDefine redirects;
	public static final ParameterDefine forwards;

	private static final ParameterDefine[] parameterDefines;
	
	static {
		request = new ParameterDefine("request", new RequestParameters());
		contents = new ParameterDefine("content", new ContentParameters(), true);
		responses = new ParameterDefine("response", new ResponseParameters(), true);
		exception = new ParameterDefine("exception", new ExceptionParameters(), true);
		actions = new ParameterDefine("action", new ActionParameters(), true);
		dispatchs = new ParameterDefine("dispatch", new DispatchParameters(), true);
		transforms = new ParameterDefine("transform", new TransformParameters(), true);
		redirects = new ParameterDefine("redirect", new RedirectParameters(), true);
		forwards = new ParameterDefine("forward", new ForwardParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				request,
				contents,
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
