package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ContentParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine actions;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		actions = new ParameterDefine("before", new ActionParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				id,
				actions
		};
	}
	
	public ContentParameters() {
		super(ContentParameters.class.getName(), parameterDefines);
	}
	
	public ContentParameters(String plaintext) {
		super(ContentParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
