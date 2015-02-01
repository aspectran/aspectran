package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class CronTriggerParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine expression;

	private final static ParameterDefine[] parameterDefines;
	
	static {
		expression = new ParameterDefine("withIntervalInMilliseconds", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				expression
		};
	}
	
	public CronTriggerParameters() {
		super(CronTriggerParameters.class.getName(), parameterDefines);
	}
	
	public CronTriggerParameters(String plaintext) {
		super(CronTriggerParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
