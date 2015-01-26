package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class AttributesParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine items;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		items = new ParameterDefine("item", new ItemParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				items
		};
	}
	
	public AttributesParameters() {
		super(AttributesParameters.class.getName(), parameterDefines);
	}
	
	public AttributesParameters(String plaintext) {
		super(AttributesParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
