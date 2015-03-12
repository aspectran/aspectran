package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class RedirectParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine contentType;
	public static final ParameterDefine translet;
	public static final ParameterDefine url;
	public static final ParameterDefine parameters;
	public static final ParameterDefine excludeNullParameter;
	public static final ParameterDefine actions;
	public static final ParameterDefine defaultResponse;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		url = new ParameterDefine("url", ParameterValueType.STRING);
		parameters = new ParameterDefine("parameter", ItemHolderParameters.class);
		excludeNullParameter = new ParameterDefine("excludeNullParameter", ParameterValueType.BOOLEAN);
		actions = new ParameterDefine("action", ActionParameters.class, true, true);
		defaultResponse = new ParameterDefine("defaultResponse", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				contentType,
				translet,
				url,
				parameters,
				excludeNullParameter,
				actions,
				defaultResponse
		};
	}
	
	public RedirectParameters() {
		super(parameterDefines);
	}
	
	public RedirectParameters(String text) {
		super(parameterDefines, text);
	}
	
}
