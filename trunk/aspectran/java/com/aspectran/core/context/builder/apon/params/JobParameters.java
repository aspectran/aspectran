package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class JobParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine translet;
	
	public static final ParameterDefine disabled;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		disabled = new ParameterDefine("disabled", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				translet,
				disabled
		};
	}
	
	public JobParameters() {
		super(JobParameters.class.getName(), parameterDefines);
	}
	
	public JobParameters(String plaintext) {
		super(JobParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
