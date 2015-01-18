package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class ArgumentsParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine items;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		items = new ParameterDefine("item", new ItemParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				items
		};
	}
	
	public ArgumentsParameters() {
		super(ArgumentsParameters.class.getName(), parameterDefines);
	}
	
	public ArgumentsParameters(String plaintext) {
		super(ArgumentsParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
