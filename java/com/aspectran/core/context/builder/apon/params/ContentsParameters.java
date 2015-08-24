package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ContentsParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine name;
	public static final ParameterDefine omittable;
	public static final ParameterDefine contents;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		name = new ParameterDefine("name", ParameterValueType.STRING);
		omittable = new ParameterDefine("omittable", ParameterValueType.BOOLEAN);
		contents = new ParameterDefine("content", ContentParameters.class, true, true);
		
		parameterDefines = new ParameterDefine[] {
				name,
				omittable,
				contents
		};
	}
	
	public ContentsParameters() {
		super(parameterDefines);
	}
	
	public ContentsParameters(String text) {
		super(parameterDefines, text);
	}
	
}
