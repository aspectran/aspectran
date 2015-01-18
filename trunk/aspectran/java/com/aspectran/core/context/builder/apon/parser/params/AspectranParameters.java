package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.GenericParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.Parameters;

public class AspectranParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine settings;
	public static final ParameterDefine typeAliases;
	public static final ParameterDefine apons;
	public static final ParameterDefine beans;
	public static final ParameterDefine imports;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		settings = new ParameterDefine("settings", new DefaultSettingsParameters());
		typeAliases = new ParameterDefine("typeAliases", new GenericParameters());
		apons = new ParameterDefine("apon", new AponParameters(), true);
		beans = new ParameterDefine("bean", new BeanParameters(), true);
		imports = new ParameterDefine("import", new ImportParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
			settings,
			typeAliases,
			beans,
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
