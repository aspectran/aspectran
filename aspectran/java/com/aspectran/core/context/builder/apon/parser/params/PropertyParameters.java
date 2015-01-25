package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class PropertyParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine items;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		items = new ParameterDefine("items", new ItemParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				items
		};
	}
	
	public PropertyParameters() {
		super(PropertyParameters.class.getName(), parameterDefines);
	}
	
	public PropertyParameters(String plaintext) {
		super(PropertyParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
