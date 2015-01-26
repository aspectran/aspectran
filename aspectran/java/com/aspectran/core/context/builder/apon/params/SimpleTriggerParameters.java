package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class SimpleTriggerParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine startDelay;
	public static final ParameterDefine repleatInterval;
	public static final ParameterDefine repeatCount;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		startDelay = new ParameterDefine("startDelay", ParameterValueType.INTEGER);
		repleatInterval = new ParameterDefine("repleatInterval", ParameterValueType.INTEGER);
		repeatCount = new ParameterDefine("repeatCount", ParameterValueType.INTEGER);
		
		parameterDefines = new ParameterDefine[] {
				startDelay,
				repleatInterval,
				repeatCount
		};
	}
	
	public SimpleTriggerParameters() {
		super(SimpleTriggerParameters.class.getName(), parameterDefines);
	}
	
	public SimpleTriggerParameters(String plaintext) {
		super(SimpleTriggerParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
