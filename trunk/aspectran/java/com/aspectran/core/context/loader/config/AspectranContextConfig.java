package com.aspectran.core.context.loader.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.InvalidParameterException;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class AspectranContextConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine root;
	public static final ParameterDefine encoding;
	public static final ParameterDefine resources;
	public static final ParameterDefine hybridLoading;
	public static final ParameterDefine autoReloading;
	
	private final static ParameterDefine[] parameterDefines;
	
	static {
		root = new ParameterDefine("root", ParameterValueType.STRING);
		encoding = new ParameterDefine("encoding", ParameterValueType.STRING);
		resources = new ParameterDefine("resources", ParameterValueType.STRING, true);
		hybridLoading = new ParameterDefine("hybridLoading", ParameterValueType.BOOLEAN);
		autoReloading = new ParameterDefine("autoReloading", AspectranContextAutoReloadingConfig.class);
		
		parameterDefines = new ParameterDefine[] {
				root,
				encoding,
				resources,
				hybridLoading,
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
