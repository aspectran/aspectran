package com.aspectran.core.context.loader.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class AspectranConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine context = new ParameterDefine("context", AspectranContextConfig.class);
	
	public static final ParameterDefine scheduler = new ParameterDefine("scheduler", AspectranSchedulerConfig.class);
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		parameterDefines = new ParameterDefine[] {
				context,
				scheduler
		};
	}
	
	public AspectranConfig() {
		super(AspectranConfig.class.getName(), parameterDefines);
	}
	
	public AspectranConfig(String plaintext) {
		super(AspectranConfig.class.getName(), parameterDefines, plaintext);
	}
	
}
