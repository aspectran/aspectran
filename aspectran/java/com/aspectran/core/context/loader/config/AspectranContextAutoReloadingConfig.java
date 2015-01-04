package com.aspectran.core.context.loader.config;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterValue;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AspectranContextAutoReloadingConfig extends AbstractParameters implements Parameters {

	public static final ParameterValue reloadMethod = new ParameterValue("reloadMethod", ParameterValueType.STRING);
	
	public static final ParameterValue observationInterval = new ParameterValue("observationInterval", ParameterValueType.INTEGER);
	
	public static final ParameterValue startup = new ParameterValue("startup", ParameterValueType.BOOLEAN);
	
	private final static ParameterValue[] parameterValues;
	
	static {
		parameterValues = new ParameterValue[] {
				reloadMethod,
				observationInterval,
				startup
		};
	}
	
	public AspectranContextAutoReloadingConfig() {
		super(AspectranContextAutoReloadingConfig.class.getName(), parameterValues);
	}
	
	public AspectranContextAutoReloadingConfig(String plaintext) throws InvalidParameterException {
		super(AspectranContextAutoReloadingConfig.class.getName(), parameterValues, plaintext);
	}
	
}
