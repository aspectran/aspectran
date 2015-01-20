package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class PointcutParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine targets;

	// for scheduler
	public static final ParameterDefine simpleTrigger;
	public static final ParameterDefine cronTrigger;
	

	private static final ParameterDefine[] parameterDefines;
	
	static {
		targets = new ParameterDefine("target", new TargetParameters());
		simpleTrigger = new ParameterDefine("simpleTrigger", new SimpleTriggerParameters());
		cronTrigger = new ParameterDefine("cronTrigger", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				targets,
				simpleTrigger,
				cronTrigger
		};
	}
	
	public PointcutParameters() {
		super(PointcutParameters.class.getName(), parameterDefines);
	}
	
	public PointcutParameters(String plaintext) {
		super(PointcutParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
