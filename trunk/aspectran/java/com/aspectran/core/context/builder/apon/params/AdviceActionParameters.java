package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class AdviceActionParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine action;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		action = new ParameterDefine("action", ActionParameters.class);
		
		parameterDefines = new ParameterDefine[] {
				action
		};
	}
	
	public AdviceActionParameters() {
		super(AdviceActionParameters.class.getName(), parameterDefines);
	}
	
	public AdviceActionParameters(String plaintext) {
		super(AdviceActionParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
