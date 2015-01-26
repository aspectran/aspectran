package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class TargetParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine plus;
	public static final ParameterDefine minus;
	
	public static final ParameterDefine translet;
	public static final ParameterDefine bean;
	public static final ParameterDefine method;
	public static final ParameterDefine excludeTargets;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		plus = new ParameterDefine("+", ParameterValueType.STRING);
		minus = new ParameterDefine("-", ParameterValueType.STRING);
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		method = new ParameterDefine("method", ParameterValueType.STRING);
		excludeTargets = new ParameterDefine("exclude", new ExcludeTargetParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				plus,
				minus,
				bean,
				method,
				excludeTargets
		};
	}
	
	public TargetParameters() {
		super(TargetParameters.class.getName(), parameterDefines);
	}
	
	public TargetParameters(String plaintext) {
		super(TargetParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
