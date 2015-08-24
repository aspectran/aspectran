package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class AdviceParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine bean;
	public static final ParameterDefine beforeAdvice;
	public static final ParameterDefine afterAdvice;
	public static final ParameterDefine aroundAdvice;
	public static final ParameterDefine finallyAdvice;
	public static final ParameterDefine exceptionRaized;
	public static final ParameterDefine jobs;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		beforeAdvice = new ParameterDefine("before", AdviceActionParameters.class);
		afterAdvice = new ParameterDefine("after", AdviceActionParameters.class);
		aroundAdvice = new ParameterDefine("around", AdviceActionParameters.class);
		finallyAdvice = new ParameterDefine("finally", AdviceActionParameters.class);
		exceptionRaized = new ParameterDefine("exceptionRaized", ExceptionRaizedParameters.class);
		jobs = new ParameterDefine("job", JobParameters.class, true, true);
		
		parameterDefines = new ParameterDefine[] {
				bean,
				beforeAdvice,
				afterAdvice,
				aroundAdvice,
				finallyAdvice,
				exceptionRaized,
				jobs
		};
	}
	
	public AdviceParameters() {
		super(parameterDefines);
	}
	
	public AdviceParameters(String text) {
		super(parameterDefines, text);
	}
	
}
