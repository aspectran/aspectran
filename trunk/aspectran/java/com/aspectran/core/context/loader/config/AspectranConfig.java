package com.aspectran.core.context.loader.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class AspectranConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine context;
	public static final ParameterDefine scheduler;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		context = new ParameterDefine("context", AspectranContextConfig.class);
		scheduler = new ParameterDefine("scheduler", AspectranSchedulerConfig.class);

		parameterDefines = new ParameterDefine[] {
				context,
				scheduler
		};
	}
	
	public AspectranConfig() {
		super(parameterDefines);
	}
	
	public AspectranConfig(String text) {
		super(parameterDefines, text);
	}
	
}
