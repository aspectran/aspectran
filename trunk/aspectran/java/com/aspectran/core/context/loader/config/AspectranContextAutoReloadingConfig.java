package com.aspectran.core.context.loader.config;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AspectranContextAutoReloadingConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine reloadMethod = new ParameterDefine("reloadMethod", ParameterValueType.STRING);
	
	public static final ParameterDefine observationInterval = new ParameterDefine("observationInterval", ParameterValueType.INTEGER);
	
	public static final ParameterDefine startup = new ParameterDefine("startup", ParameterValueType.BOOLEAN);
	
	private final static ParameterDefine[] parameterValues;
	
	static {
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
