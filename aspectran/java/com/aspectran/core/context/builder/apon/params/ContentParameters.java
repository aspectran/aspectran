package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ContentParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine hidden;
	public static final ParameterDefine actions;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		hidden = new ParameterDefine("hidden", ParameterValueType.BOOLEAN);
		actions = new ParameterDefine("action", new ActionParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				id,
				hidden,
				actions
		};
	}
	
	public ContentParameters() {
		super(ContentParameters.class.getName(), parameterDefines);
	}
	
	public ContentParameters(String plaintext) {
		super(ContentParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
