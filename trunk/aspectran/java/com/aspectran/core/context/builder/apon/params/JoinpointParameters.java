package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class JoinpointParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine scope;
	
	public static final ParameterDefine pointcut;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		scope = new ParameterDefine("scope", ParameterValueType.STRING);
		pointcut = new ParameterDefine("pointcut", new PointcutParameters());
		
		parameterDefines = new ParameterDefine[] {
				scope,
				pointcut
		};
	}
	
	public JoinpointParameters() {
		super(JoinpointParameters.class.getName(), parameterDefines);
	}
	
	public JoinpointParameters(String plaintext) {
		super(JoinpointParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
