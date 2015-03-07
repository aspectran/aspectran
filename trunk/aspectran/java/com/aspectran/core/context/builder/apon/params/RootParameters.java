package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class RootParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine aspectran;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		aspectran = new ParameterDefine("aspectran", AspectranParameters.class);
		
		parameterDefines = new ParameterDefine[] {
			aspectran
		};
	}
	
	public RootParameters() {
		super(RootParameters.class.getName(), parameterDefines);
	}
	
	public RootParameters(String plaintext) {
		super(RootParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
