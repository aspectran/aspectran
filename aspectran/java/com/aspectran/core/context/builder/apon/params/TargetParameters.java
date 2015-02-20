package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class TargetParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine pluses;
	public static final ParameterDefine minuses;
	
	public static final ParameterDefine translet;
	public static final ParameterDefine bean;
	public static final ParameterDefine method;
	public static final ParameterDefine excludeTargets;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		pluses = new ParameterDefine("+", ParameterValueType.STRING, true);
		minuses = new ParameterDefine("-", ParameterValueType.STRING, true);
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		method = new ParameterDefine("method", ParameterValueType.STRING);
		excludeTargets = new ParameterDefine("exclude", ExcludeTargetParameters.class, true);
		
		parameterDefines = new ParameterDefine[] {
				pluses,
				minuses,
				translet,
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
