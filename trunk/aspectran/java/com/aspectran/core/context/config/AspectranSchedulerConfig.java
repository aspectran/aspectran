package com.aspectran.core.context.config;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterArrayValue;
import com.aspectran.core.var.apon.ParameterValue;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AspectranSchedulerConfig extends AbstractParameters implements Parameters {

	public static final ParameterValue startDelaySeconds = new ParameterValue("startDelaySeconds", ParameterValueType.INTEGER);
	
	public static final ParameterValue waitOnShutdown = new ParameterArrayValue("waitOnShutdown", ParameterValueType.BOOLEAN);
	
	public static final ParameterValue startup = new ParameterArrayValue("startup", ParameterValueType.BOOLEAN);
	
	private final static ParameterValue[] parameterValues;
	
	static {
		parameterValues = new ParameterValue[] {
				startDelaySeconds,
				waitOnShutdown,
				startup
		};
	}
	
	public AspectranSchedulerConfig() {
		super(AspectranSchedulerConfig.class.getName(), parameterValues);
	}
	
	public AspectranSchedulerConfig(String plaintext) throws InvalidParameterException {
		super(AspectranSchedulerConfig.class.getName(), parameterValues, plaintext);
	}
	
}
