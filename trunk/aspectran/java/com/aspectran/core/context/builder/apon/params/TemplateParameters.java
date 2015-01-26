package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class TemplateParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine file;
	public static final ParameterDefine url;
	public static final ParameterDefine content;
	public static final ParameterDefine encoding;
	public static final ParameterDefine noCache;

	private static final ParameterDefine[] parameterDefines;

	
	static {
		file = new ParameterDefine("file", ParameterValueType.STRING);
		url = new ParameterDefine("url", ParameterValueType.STRING);
		content = new ParameterDefine("content", ParameterValueType.STRING, true);
		encoding = new ParameterDefine("encoding", ParameterValueType.STRING);
		noCache = new ParameterDefine("noCache", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				file,
				url,
				content,
				encoding,
				noCache
		};
	}
	
	public TemplateParameters() {
		super(TemplateParameters.class.getName(), parameterDefines);
	}
	
	public TemplateParameters(String plaintext) {
		super(TemplateParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
