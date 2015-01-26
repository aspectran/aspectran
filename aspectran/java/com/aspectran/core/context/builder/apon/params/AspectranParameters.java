package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.GenericParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class AspectranParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine setting;
	public static final ParameterDefine typeAlias;
	public static final ParameterDefine aspects;
	public static final ParameterDefine beans;
	public static final ParameterDefine translets;
	public static final ParameterDefine imports;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		setting = new ParameterDefine("setting", new DefaultSettingsParameters());
		typeAlias = new ParameterDefine("typeAlias", new GenericParameters());
		aspects = new ParameterDefine("aspect", new AspectParameters(), true);
		beans = new ParameterDefine("bean", new BeanParameters(), true);
		translets = new ParameterDefine("translet", new TransletParameters(), true);
		imports = new ParameterDefine("import", new ImportParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
			setting,
			typeAlias,
			aspects,
			beans,
			translets,
			imports
		};
	}
	
	public AspectranParameters() {
		super(AspectranParameters.class.getName(), parameterDefines);
	}
	
	public AspectranParameters(String plaintext) {
		super(AspectranParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
