package com.aspectran.core.context.loader.config;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AspectranContextConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine root = new ParameterDefine("root", ParameterValueType.STRING);
	
	public static final ParameterDefine encoding = new ParameterDefine("encoding", ParameterValueType.STRING);
	
	public static final ParameterDefine resources = new ParameterDefine("resources", ParameterValueType.STRING, true);
	
	public static final ParameterDefine autoReloading = new ParameterDefine("autoReloading", new AspectranContextAutoReloadingConfig());
	
	private final static ParameterDefine[] parameterDefines;
	
	static {
		parameterDefines = new ParameterDefine[] {
				root,
				encoding,
				resources,
				autoReloading
		};
	}
	
	public AspectranContextConfig() {
		super(AspectranContextConfig.class.getName(), parameterDefines);
	}
	
	public AspectranContextConfig(String plaintext) throws InvalidParameterException {
		super(AspectranContextConfig.class.getName(), parameterDefines, plaintext);
	}
	
}
