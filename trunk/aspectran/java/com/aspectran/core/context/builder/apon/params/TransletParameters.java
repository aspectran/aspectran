package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class TransletParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine request;
	public static final ParameterDefine contents;
	public static final ParameterDefine responses;
	public static final ParameterDefine exception;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		request = new ParameterDefine("request", new RequestParameters());
		contents = new ParameterDefine("content", new ContentParameters(), true);
		responses = new ParameterDefine("response", new ResponseParameters(), true);
		exception = new ParameterDefine("exception", new ExceptionParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				request,
				contents,
				responses,
				exception
		};
	}
	
	public TransletParameters() {
		super(TransletParameters.class.getName(), parameterDefines);
	}
	
	public TransletParameters(String plaintext) {
		super(TransletParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
