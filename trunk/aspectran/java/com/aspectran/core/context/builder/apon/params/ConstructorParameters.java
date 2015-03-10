package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class ConstructorParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine arguments;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		arguments = new ParameterDefine("argument", ItemParameters.class, true);
		
		parameterDefines = new ParameterDefine[] {
				arguments
		};
	}
	
	public ConstructorParameters() {
		super(parameterDefines);
	}
	
	public ConstructorParameters(String text) {
		super(parameterDefines, text);
	}
	
}
