package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class TransformParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine type;
	public static final ParameterDefine contentType;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine template;
	public static final ParameterDefine actions;
	public static final ParameterDefine defaultResponse;
	public static final ParameterDefine pretty;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		type = new ParameterDefine("type", ParameterValueType.STRING);
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		template = new ParameterDefine("template", TemplateParameters.class);
		actions = new ParameterDefine("action", ActionParameters.class, true, true);
		defaultResponse = new ParameterDefine("defaultResponse", ParameterValueType.BOOLEAN);
		pretty = new ParameterDefine("pretty", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				type,
				contentType,
				characterEncoding,
				template,
				actions,
				defaultResponse,
				pretty
		};
	}
	
	public TransformParameters() {
		super(TransformParameters.class.getName(), parameterDefines);
	}
	
	public TransformParameters(String plaintext) {
		super(TransformParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
