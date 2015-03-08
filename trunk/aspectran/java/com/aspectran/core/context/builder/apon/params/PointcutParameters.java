package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class PointcutParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine type;
	public static final ParameterDefine targets;

	// for scheduler
	public static final ParameterDefine simpleTrigger;
	public static final ParameterDefine cronTrigger;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		type = new ParameterDefine("type", ParameterValueType.STRING);
		targets = new ParameterDefine("target", TargetParameters.class, true, true);
		simpleTrigger = new ParameterDefine("simpleTrigger", SimpleTriggerParameters.class);
		cronTrigger = new ParameterDefine("cronTrigger", CronTriggerParameters.class);
		
		parameterDefines = new ParameterDefine[] {
				type,
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
