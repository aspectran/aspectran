package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.GenericParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ActionParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine bean;
	public static final ParameterDefine method;
	public static final ParameterDefine arguments;
	public static final ParameterDefine properties;
	public static final ParameterDefine include;
	public static final ParameterDefine echo;
	public static final ParameterDefine hidden;
	
	private static final ParameterDefine[] parameterDefines;

	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		bean = new ParameterDefine("class", ParameterValueType.STRING);
		method = new ParameterDefine("scope", ParameterValueType.STRING);
		arguments = new ParameterDefine("argument", new ItemParameters(), true);
		properties = new ParameterDefine("property", new ItemParameters(), true);
		include = new ParameterDefine("include", ParameterValueType.STRING);
		echo = new ParameterDefine("echo", new GenericParameters());
		hidden = new ParameterDefine("hidden", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				id,
				bean,
				method,
				hidden,
				arguments,
				include,
				echo,
				hidden
				
		};
	}
	
	public ActionParameters() {
		super(ActionParameters.class.getName(), parameterDefines);
	}
	
	public ActionParameters(String plaintext) {
		super(ActionParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
