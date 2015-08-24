package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class JoinpointParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine scope;
	public static final ParameterDefine pointcut;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		scope = new ParameterDefine("scope", ParameterValueType.STRING);
		pointcut = new ParameterDefine("pointcut", PointcutParameters.class);
		
		parameterDefines = new ParameterDefine[] {
				scope,
				pointcut
		};
	}
	
	public JoinpointParameters() {
		super(parameterDefines);
	}
	
	public JoinpointParameters(String text) {
		super(parameterDefines, text);
	}
	
}
