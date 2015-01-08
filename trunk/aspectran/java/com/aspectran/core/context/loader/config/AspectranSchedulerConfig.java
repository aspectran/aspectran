package com.aspectran.core.context.loader.config;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AspectranSchedulerConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine startDelaySeconds = new ParameterDefine("startDelaySeconds", ParameterValueType.INTEGER);
	
	public static final ParameterDefine waitOnShutdown = new ParameterDefine("waitOnShutdown", ParameterValueType.BOOLEAN);
	
	public static final ParameterDefine startup = new ParameterDefine("startup", ParameterValueType.BOOLEAN);
	
	private final static ParameterDefine[] parameterDefines;
	
	static {
		parameterDefines = new ParameterDefine[] {
			startDelaySeconds,
			waitOnShutdown,
			startup
		};
	}
	
	public AspectranSchedulerConfig() {
		super(AspectranSchedulerConfig.class.getName(), parameterDefines);
	}
	
	public AspectranSchedulerConfig(String plaintext) throws InvalidParameterException {
		super(AspectranSchedulerConfig.class.getName(), parameterDefines, plaintext);
	}
	
}
