package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ContentParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine name;
	public static final ParameterDefine omittable;
	public static final ParameterDefine hidden;
	public static final ParameterDefine actions;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		name = new ParameterDefine("name", ParameterValueType.STRING);
		omittable = new ParameterDefine("omittable", ParameterValueType.BOOLEAN);
		hidden = new ParameterDefine("hidden", ParameterValueType.BOOLEAN);
		actions = new ParameterDefine("action", ActionParameters.class, true, true);
		
		parameterDefines = new ParameterDefine[] {
				id,
				name,
				omittable,
				hidden,
				actions
		};
	}
	
	public ContentParameters() {
		super(parameterDefines);
	}
	
	public ContentParameters(String text) {
		super(parameterDefines, text);
	}
	
}
