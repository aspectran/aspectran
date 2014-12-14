package com.aspectran.core.context.config;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterArrayValue;
import com.aspectran.core.var.apon.ParameterValue;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AspectranContextConfig extends AbstractParameters implements Parameters {

	public static final ParameterValue root = new ParameterValue("root", ParameterValueType.STRING);
	
	public static final ParameterValue resources = new ParameterArrayValue("resources", ParameterValueType.STRING);
	
	public static final ParameterValue autoReloading = new ParameterArrayValue("autoReloading", new AspectranContextAutoReloadingConfig());
	
	private final static ParameterValue[] parameterValues;
	
	static {
		parameterValues = new ParameterValue[] {
				root,
				resources,
				autoReloading
		};
	}
	
	public AspectranContextConfig() {
		super(AspectranContextConfig.class.getName(), parameterValues);
	}
	
	public AspectranContextConfig(String plaintext) throws InvalidParameterException {
		super(AspectranContextConfig.class.getName(), parameterValues, plaintext);
	}
	
}
