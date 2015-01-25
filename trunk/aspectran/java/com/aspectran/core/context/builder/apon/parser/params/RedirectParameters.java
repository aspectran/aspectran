package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class RedirectParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine contentType;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine translet;
	public static final ParameterDefine url;
	public static final ParameterDefine parameters;
	public static final ParameterDefine excludeNullParameter;
	public static final ParameterDefine actions;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		url = new ParameterDefine("url", ParameterValueType.STRING);
		parameters = new ParameterDefine("parameter", new ItemParameters(), true);
		excludeNullParameter = new ParameterDefine("excludeNullParameter", new TemplateParameters());
		actions = new ParameterDefine("action", new ActionParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				contentType,
				characterEncoding,
				translet,
				url,
				parameters,
				excludeNullParameter,
				actions
		};
	}
	
	public RedirectParameters() {
		super(RedirectParameters.class.getName(), parameterDefines);
	}
	
	public RedirectParameters(String plaintext) {
		super(RedirectParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
