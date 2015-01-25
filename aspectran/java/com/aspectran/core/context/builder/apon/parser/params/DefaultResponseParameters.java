package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class DefaultResponseParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine transforms;
	public static final ParameterDefine dispatchs;
	public static final ParameterDefine redirects;
	public static final ParameterDefine forwards;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		dispatchs = new ParameterDefine("dispatch", new DispatchParameters(), true);
		transforms = new ParameterDefine("transform", new TransformParameters(), true);
		redirects = new ParameterDefine("redirect", new RedirectParameters(), true);
		forwards = new ParameterDefine("forward", new ForwardParameters(), true);

		parameterDefines = new ParameterDefine[] {
				dispatchs,
				transforms,
				redirects,
				forwards
		};
	}
	
	public DefaultResponseParameters() {
		super(DefaultResponseParameters.class.getName(), parameterDefines);
	}
	
	public DefaultResponseParameters(String plaintext) {
		super(DefaultResponseParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
