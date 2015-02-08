package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

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
