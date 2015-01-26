package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AdviceParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine bean;
	public static final ParameterDefine beforeActions;
	public static final ParameterDefine afterActions;
	public static final ParameterDefine aroundActions;
	public static final ParameterDefine finallyActions;
	public static final ParameterDefine exceptionRaized;
	public static final ParameterDefine jobs;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		beforeActions = new ParameterDefine("before", new ActionParameters(), true);
		afterActions = new ParameterDefine("after", new ActionParameters(), true);
		aroundActions = new ParameterDefine("around", new ActionParameters(), true);
		finallyActions = new ParameterDefine("finally", new ActionParameters(), true);
		exceptionRaized = new ParameterDefine("exceptionRaized", new ExceptionRaizedParameters());
		jobs = new ParameterDefine("job", new JobParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				bean,
				beforeActions,
				afterActions,
				aroundActions,
				finallyActions,
				exceptionRaized,
				jobs
		};
	}
	
	public AdviceParameters() {
		super(AdviceParameters.class.getName(), parameterDefines);
	}
	
	public AdviceParameters(String plaintext) {
		super(AdviceParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
