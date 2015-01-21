package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class DispatchParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine contentType;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine template;
	public static final ParameterDefine actions;
	
	private static final ParameterDefine[] parameterDefines;

	
	static {
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		template = new ParameterDefine("template", new TemplateParameters());
		actions = new ParameterDefine("action", new ActionParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				contentType,
				characterEncoding,
				template,
				actions
		};
	}
	
	public DispatchParameters() {
		super(DispatchParameters.class.getName(), parameterDefines);
	}
	
	public DispatchParameters(String plaintext) {
		super(DispatchParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
