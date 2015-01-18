package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ImportParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine resource;
	
	public static final ParameterDefine file;
	
	public static final ParameterDefine url;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		resource = new ParameterDefine("resource", ParameterValueType.STRING);
		file = new ParameterDefine("file", ParameterValueType.STRING);
		url = new ParameterDefine("url", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				resource,
				file,
				url
		};
	}
	
	public ImportParameters() {
		super(ImportParameters.class.getName(), parameterDefines);
	}
	
	public ImportParameters(String plaintext) {
		super(ImportParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
