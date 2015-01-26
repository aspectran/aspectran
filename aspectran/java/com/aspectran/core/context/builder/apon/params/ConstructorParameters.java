package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class ConstructorParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine arguments;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		arguments = new ParameterDefine("argument", new ItemParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				arguments
		};
	}
	
	public ConstructorParameters() {
		super(ConstructorParameters.class.getName(), parameterDefines);
	}
	
	public ConstructorParameters(String plaintext) {
		super(ConstructorParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
