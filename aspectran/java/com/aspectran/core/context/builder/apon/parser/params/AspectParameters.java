package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.GenericParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AspectParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine useFor;
	public static final ParameterDefine jointpoint;
	public static final ParameterDefine setting;
	public static final ParameterDefine advice;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		useFor = new ParameterDefine("useFor", ParameterValueType.STRING);
		jointpoint = new ParameterDefine("joinpoint", new JoinpointParameters());
		setting = new ParameterDefine("setting", new GenericParameters());
		advice = new ParameterDefine("advice", new AdviceParameters());
		
		parameterDefines = new ParameterDefine[] {
				id,
				useFor,
				jointpoint,
				setting,
				advice
		};
	}
	
	public AspectParameters() {
		super(AspectParameters.class.getName(), parameterDefines);
	}
	
	public AspectParameters(String plaintext) {
		super(AspectParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
