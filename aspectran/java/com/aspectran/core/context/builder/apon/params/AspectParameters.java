package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

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
		jointpoint = new ParameterDefine("joinpoint", JoinpointParameters.class);
		setting = new ParameterDefine("setting", GenericParameters.class);
		advice = new ParameterDefine("advice", AdviceParameters.class);
		
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
