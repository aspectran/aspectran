package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ReferenceParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine bean;
	public static final ParameterDefine parameter;
	public static final ParameterDefine attribute;
	public static final ParameterDefine property;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		parameter = new ParameterDefine("parameter", ParameterValueType.STRING);
		attribute = new ParameterDefine("attribute", ParameterValueType.STRING);
		property = new ParameterDefine("property", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				bean,
				parameter,
				attribute,
				property
		};
	}
	
	public ReferenceParameters() {
		super(ReferenceParameters.class.getName(), parameterDefines);
	}
	
	public ReferenceParameters(String plaintext) {
		super(ReferenceParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
