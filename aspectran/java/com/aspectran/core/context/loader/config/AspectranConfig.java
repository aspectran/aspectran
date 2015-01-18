package com.aspectran.core.context.loader.config;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class AspectranConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine context = new ParameterDefine("context", new AspectranContextConfig());
	
	public static final ParameterDefine scheduler = new ParameterDefine("scheduler", new AspectranSchedulerConfig());
	
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
