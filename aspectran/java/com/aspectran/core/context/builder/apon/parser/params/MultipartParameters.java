package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class MultipartParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine attribute;
	public static final ParameterDefine fileItems;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		attribute = new ParameterDefine("attribute", ParameterValueType.BOOLEAN);
		fileItems = new ParameterDefine("fileItem", new ItemParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				fileItems
		};
	}
	
	public MultipartParameters() {
		super(MultipartParameters.class.getName(), parameterDefines);
	}
	
	public MultipartParameters(String plaintext) {
		super(MultipartParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
