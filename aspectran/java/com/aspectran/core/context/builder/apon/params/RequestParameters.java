package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class RequestParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine method;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine attributes;
	public static final ParameterDefine multipart;
	
	private static final ParameterDefine[] parameterDefines;

	
	static {
		method = new ParameterDefine("method", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		attributes = new ParameterDefine("attribute", new ItemParameters(), true);
		multipart = new ParameterDefine("multipart", new MultipartParameters());
		
		parameterDefines = new ParameterDefine[] {
				method,
				characterEncoding,
				attributes,
				multipart
		};
	}
	
	public RequestParameters() {
		super(RequestParameters.class.getName(), parameterDefines);
	}
	
	public RequestParameters(String plaintext) {
		super(RequestParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
