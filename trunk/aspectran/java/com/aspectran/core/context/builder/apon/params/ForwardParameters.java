package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ForwardParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine contentType;
	public static final ParameterDefine translet;
	public static final ParameterDefine parameters;
	public static final ParameterDefine actions;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		parameters = new ParameterDefine("parameter", new ItemParameters(), true);
		actions = new ParameterDefine("action", new ActionParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				contentType,
				translet,
				parameters,
				actions
		};
	}
	
	public ForwardParameters() {
		super(ForwardParameters.class.getName(), parameterDefines);
	}
	
	public ForwardParameters(String plaintext) {
		super(ForwardParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
