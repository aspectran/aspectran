package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class TransformParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine transformType;
	public static final ParameterDefine contentType;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine template;
	public static final ParameterDefine actions;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		transformType = new ParameterDefine("transformType", ParameterValueType.STRING);
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		template = new ParameterDefine("template", new TemplateParameters());
		actions = new ParameterDefine("action", new ActionParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				transformType,
				contentType,
				characterEncoding,
				template,
				actions
		};
	}
	
	public TransformParameters() {
		super(TransformParameters.class.getName(), parameterDefines);
	}
	
	public TransformParameters(String plaintext) {
		super(TransformParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
