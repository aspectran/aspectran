package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class PropertiesParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine items;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		items = new ParameterDefine("items", new ItemParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				items
		};
	}
	
	public PropertiesParameters() {
		super(PropertiesParameters.class.getName(), parameterDefines);
	}
	
	public PropertiesParameters(String plaintext) {
		super(PropertiesParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
