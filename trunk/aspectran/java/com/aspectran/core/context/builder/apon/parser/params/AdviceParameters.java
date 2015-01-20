package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AdviceParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine bean;
	public static final ParameterDefine jobs;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		jobs = new ParameterDefine("job", new JobParameters());
		
		parameterDefines = new ParameterDefine[] {
				bean,
				jobs
				
		};
	}
	
	public AdviceParameters() {
		super(AdviceParameters.class.getName(), parameterDefines);
	}
	
	public AdviceParameters(String plaintext) {
		super(AdviceParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
