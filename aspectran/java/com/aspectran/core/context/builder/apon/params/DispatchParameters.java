package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class DispatchParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine contentType;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine template;
	public static final ParameterDefine actions;
	public static final ParameterDefine defaultResponse;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		template = new ParameterDefine("template", TemplateParameters.class);
		actions = new ParameterDefine("action", ActionParameters.class, true);
		defaultResponse = new ParameterDefine("defaultResponse", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				contentType,
				characterEncoding,
				template,
				actions,
				defaultResponse
		};
	}
	
	public DispatchParameters() {
		super(DispatchParameters.class.getName(), parameterDefines);
	}
	
	public DispatchParameters(String plaintext) {
		super(DispatchParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
