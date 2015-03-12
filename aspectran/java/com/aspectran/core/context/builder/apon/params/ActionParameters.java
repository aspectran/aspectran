package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ActionParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	
	public static final ParameterDefine beanId;
	public static final ParameterDefine methodName;
	public static final ParameterDefine arguments;
	public static final ParameterDefine properties;
	
	public static final ParameterDefine include;
	public static final ParameterDefine attributes;

	public static final ParameterDefine echo;
	
	public static final ParameterDefine hidden;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		beanId = new ParameterDefine("beanId", ParameterValueType.STRING);
		methodName = new ParameterDefine("method", ParameterValueType.STRING);
		arguments = new ParameterDefine("argument", ItemHolderParameters.class);
		properties = new ParameterDefine("property", ItemHolderParameters.class);
		include = new ParameterDefine("include", ParameterValueType.STRING);
		attributes = new ParameterDefine("attribute", ItemHolderParameters.class);
		echo = new ParameterDefine("echo", ItemHolderParameters.class);
		hidden = new ParameterDefine("hidden", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				id,
				beanId,
				methodName,
				arguments,
				properties,
				include,
				attributes,
				echo,
				hidden
		};
	}
	
	public ActionParameters() {
		super(parameterDefines);
	}
	
	public ActionParameters(String plaintext) {
		super(parameterDefines, plaintext);
	}
	
}
