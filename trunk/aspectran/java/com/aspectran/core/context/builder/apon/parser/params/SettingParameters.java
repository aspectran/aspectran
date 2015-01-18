package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class SettingParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine name;
	
	public static final ParameterDefine value;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		name = new ParameterDefine("name", ParameterValueType.STRING);
		value = new ParameterDefine("value", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				name,
				value
		};
	}
	
	public SettingParameters() {
		super(SettingParameters.class.getName(), parameterDefines);
	}
	
	public SettingParameters(String plaintext) {
		super(SettingParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
