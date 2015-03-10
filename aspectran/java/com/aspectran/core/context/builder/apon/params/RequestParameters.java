package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class RequestParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine method;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine attributes;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		method = new ParameterDefine("method", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		attributes = new ParameterDefine("attribute", ItemParameters.class, true);
		
		parameterDefines = new ParameterDefine[] {
				method,
				characterEncoding,
				attributes
		};
	}
	
	public RequestParameters() {
		super(parameterDefines);
	}
	
	public RequestParameters(String text) {
		super(parameterDefines, text);
	}
	
}
