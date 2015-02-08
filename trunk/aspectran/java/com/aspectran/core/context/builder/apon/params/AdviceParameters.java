package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class AdviceParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine bean;
	public static final ParameterDefine beforeAction;
	public static final ParameterDefine afterAction;
	public static final ParameterDefine aroundAction;
	public static final ParameterDefine finallyAction;
	public static final ParameterDefine exceptionRaized;
	public static final ParameterDefine jobs;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		beforeAction = new ParameterDefine("before", new ActionParameters());
		afterAction = new ParameterDefine("after", new ActionParameters());
		aroundAction = new ParameterDefine("around", new ActionParameters());
		finallyAction = new ParameterDefine("finally", new ActionParameters());
		exceptionRaized = new ParameterDefine("exceptionRaized", new ExceptionRaizedParameters());
		jobs = new ParameterDefine("job", new JobParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				bean,
				beforeAction,
				afterAction,
				aroundAction,
				finallyAction,
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
