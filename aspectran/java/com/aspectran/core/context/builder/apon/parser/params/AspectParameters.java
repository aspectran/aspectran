package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.GenericParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class AspectParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine useFor;
	public static final ParameterDefine jointpoint;
	public static final ParameterDefine settings;
	public static final ParameterDefine apons;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		useFor = new ParameterDefine("useFor", ParameterValueType.STRING);
		jointpoint = new ParameterDefine("joinpoint", new JoinpointParameters());
		settings = new ParameterDefine("settings", new GenericParameters());
		apons = new ParameterDefine("apon", new AponParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				id,
				useFor,
				jointpoint,
				settings,
				apons
				
		};
	}
	
	public AspectParameters() {
		super(AspectParameters.class.getName(), parameterDefines);
	}
	
	public AspectParameters(String plaintext) {
		super(AspectParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
