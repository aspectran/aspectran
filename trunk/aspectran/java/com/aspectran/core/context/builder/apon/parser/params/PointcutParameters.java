package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class PointcutParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine wildcard;
	public static final ParameterDefine regexp;

	// for scheduler
	public static final ParameterDefine simpleTrigger;
	public static final ParameterDefine cronTrigger;
	
	public static final ParameterDefine targets;

	private static final ParameterDefine[] parameterDefines;
	
	static {
		wildcard = new ParameterDefine("wildcard", ParameterValueType.STRING);
		regexp = new ParameterDefine("regexp", ParameterValueType.STRING);
		simpleTrigger = new ParameterDefine("simpleTrigger", new SimpleTriggerParameters());
		cronTrigger = new ParameterDefine("cronTrigger", ParameterValueType.STRING);
		targets = new ParameterDefine("target", new TargetParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				wildcard,
				regexp,
				simpleTrigger,
				cronTrigger,
				targets
		};
	}
	
	public PointcutParameters() {
		super(PointcutParameters.class.getName(), parameterDefines);
	}
	
	public PointcutParameters(String plaintext) {
		super(PointcutParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
