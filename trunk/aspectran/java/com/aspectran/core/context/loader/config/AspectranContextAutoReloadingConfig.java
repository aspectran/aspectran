package com.aspectran.core.context.loader.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.InvalidParameterException;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class AspectranContextAutoReloadingConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine reloadMethod;
	public static final ParameterDefine observationInterval;
	public static final ParameterDefine startup;
	
	private final static ParameterDefine[] parameterValues;
	
	static {
		reloadMethod = new ParameterDefine("reloadMethod", ParameterValueType.STRING);
		observationInterval = new ParameterDefine("observationInterval", ParameterValueType.INT);
		startup = new ParameterDefine("startup", ParameterValueType.BOOLEAN);

		parameterValues = new ParameterDefine[] {
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
