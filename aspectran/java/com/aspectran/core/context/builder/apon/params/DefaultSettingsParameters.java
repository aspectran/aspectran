package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class DefaultSettingsParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine transletNamePattern;
	public static final ParameterDefine transletNamePatternPrefix;
	public static final ParameterDefine transletNamePatternSuffix;
	public static final ParameterDefine transletInterfaceClassName;
	public static final ParameterDefine transletImplementClassName;
	public static final ParameterDefine activityDefaultHandler;
	public static final ParameterDefine nullableContentId;
	public static final ParameterDefine nullableActionId;
	public static final ParameterDefine beanProxyMode;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		transletNamePattern = new ParameterDefine("transletNamePattern", ParameterValueType.STRING);
		transletNamePatternPrefix = new ParameterDefine("transletNamePatternPrefix", ParameterValueType.STRING);
		transletNamePatternSuffix = new ParameterDefine("transletNamePatternSuffix", ParameterValueType.STRING);
		transletInterfaceClassName = new ParameterDefine("transletInterfaceClassName", ParameterValueType.STRING);
		transletImplementClassName = new ParameterDefine("transletImplementClassName", ParameterValueType.STRING);
		activityDefaultHandler = new ParameterDefine("activityDefaultHandler", ParameterValueType.STRING);
		nullableContentId = new ParameterDefine("nullableContentId", ParameterValueType.BOOLEAN);
		nullableActionId = new ParameterDefine("nullableActionId", ParameterValueType.BOOLEAN);
		beanProxyMode = new ParameterDefine("beanProxyMode", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				transletNamePattern,
				transletNamePatternPrefix,
				transletNamePatternSuffix,
				transletInterfaceClassName,
				transletImplementClassName,
				activityDefaultHandler,
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
