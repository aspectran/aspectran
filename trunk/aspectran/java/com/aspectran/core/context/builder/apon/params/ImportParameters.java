package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ImportParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine file;
	public static final ParameterDefine resource;
	public static final ParameterDefine url;
	public static final ParameterDefine fileType;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		file = new ParameterDefine("file", ParameterValueType.STRING);
		resource = new ParameterDefine("resource", ParameterValueType.STRING);
		url = new ParameterDefine("url", ParameterValueType.STRING);
		fileType = new ParameterDefine("fileType", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				file,
				resource,
				url,
				fileType
		};
	}
	
	public ImportParameters() {
		super(ImportParameters.class.getName(), parameterDefines);
	}
	
	public ImportParameters(String plaintext) {
		super(ImportParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
