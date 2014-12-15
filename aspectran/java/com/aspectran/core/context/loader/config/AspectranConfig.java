package com.aspectran.core.context.loader.config;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterArrayValue;
import com.aspectran.core.var.apon.ParameterValue;
import com.aspectran.core.var.apon.Parameters;

public class AspectranConfig extends AbstractParameters implements Parameters {

	public static final ParameterValue context = new ParameterValue("context", new AspectranContextConfig());
	
	public static final ParameterValue scheduler = new ParameterArrayValue("scheduler", new AspectranSchedulerConfig());
	
	private final static ParameterValue[] parameterValues;
	
	static {
		parameterValues = new ParameterValue[] {
				context,
				scheduler
		};
	}
	
	public AspectranConfig() {
		super(AspectranConfig.class.getName(), parameterValues);
	}
	
	public AspectranConfig(String plaintext) throws InvalidParameterException {
		super(AspectranConfig.class.getName(), parameterValues, plaintext);
	}
	
}
