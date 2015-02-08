package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class DefaultSettingsParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine transletNamePattern;
	public static final ParameterDefine transletNamePatternPrefix;
	public static final ParameterDefine transletNamePatternSuffix;
	public static final ParameterDefine transletInterfaceClass;
	public static final ParameterDefine transletImplementClass;
	public static final ParameterDefine useNamespaces;
	public static final ParameterDefine nullableContentId;
	public static final ParameterDefine nullableActionId;
	public static final ParameterDefine beanProxyMode;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		transletNamePattern = new ParameterDefine("transletNamePattern", ParameterValueType.STRING);
		transletNamePatternPrefix = new ParameterDefine("transletNamePatternPrefix", ParameterValueType.STRING);
		transletNamePatternSuffix = new ParameterDefine("transletNamePatternSuffix", ParameterValueType.STRING);
		transletInterfaceClass = new ParameterDefine("transletInterfaceClass", ParameterValueType.STRING);
		transletImplementClass = new ParameterDefine("transletImplementClass", ParameterValueType.STRING);
		useNamespaces = new ParameterDefine("useNamespaces", ParameterValueType.BOOLEAN);
		nullableContentId = new ParameterDefine("nullableContentId", ParameterValueType.BOOLEAN);
		nullableActionId = new ParameterDefine("nullableActionId", ParameterValueType.BOOLEAN);
		beanProxyMode = new ParameterDefine("beanProxyMode", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				transletNamePattern,
				transletInterfaceClass,
				transletImplementClass,
				useNamespaces,
				nullableContentId,
				nullableActionId,
				beanProxyMode
		};
	}
	
	public DefaultSettingsParameters() {
		super(DefaultSettingsParameters.class.getName(), parameterDefines);
	}
	
	public DefaultSettingsParameters(String plaintext) {
		super(DefaultSettingsParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
