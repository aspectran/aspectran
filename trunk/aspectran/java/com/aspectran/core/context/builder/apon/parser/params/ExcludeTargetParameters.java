package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ExcludeTargetParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine translet;
	public static final ParameterDefine bean;
	public static final ParameterDefine method;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		method = new ParameterDefine("method", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				translet,
				bean,
				method
		};
	}
	
	public ExcludeTargetParameters() {
		super(ExcludeTargetParameters.class.getName(), parameterDefines);
	}
	
	public ExcludeTargetParameters(String plaintext) {
		super(ExcludeTargetParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
