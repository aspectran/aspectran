package com.aspectran.core.context.builder.apon.structure;

import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.loader.config.AspectranSchedulerConfig;
import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class AspectranStructure extends AbstractParameters implements Parameters {

	public static final ParameterDefine context = new ParameterDefine("context", new AspectranContextConfig());
	
	public static final ParameterDefine scheduler = new ParameterDefine("scheduler", new AspectranSchedulerConfig());
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		parameterDefines = new ParameterDefine[] {
			context,
			scheduler
		};
	}
	
	public AspectranStructure() {
		super(AspectranStructure.class.getName(), parameterDefines);
	}
	
	public AspectranStructure(String plaintext) {
		super(AspectranStructure.class.getName(), parameterDefines, plaintext);
	}
	
}
