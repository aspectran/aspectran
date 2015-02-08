package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ForwardParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine contentType;
	public static final ParameterDefine translet;
	public static final ParameterDefine attributes;
	public static final ParameterDefine actions;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		attributes = new ParameterDefine("attribute", new ItemParameters(), true);
		actions = new ParameterDefine("action", new ActionParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				contentType,
				translet,
				attributes,
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
