package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ActionParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine bean;
	public static final ParameterDefine method;
	public static final ParameterDefine hidden;
	public static final ParameterDefine arguments;
	public static final ParameterDefine properties;
	public static final ParameterDefine destroyMethod;
	public static final ParameterDefine lazyInit;
	public static final ParameterDefine parent;
	public static final ParameterDefine override;
	public static final ParameterDefine constructor;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		bean = new ParameterDefine("class", ParameterValueType.STRING);
		method = new ParameterDefine("scope", ParameterValueType.STRING);
		hidden = new ParameterDefine("factoryMethod", ParameterValueType.BOOLEAN);
		arguments = new ParameterDefine("arguments", new ItemParameters(), true);
		properties = new ParameterDefine("properties", new ItemParameters(), true);
		destroyMethod = new ParameterDefine("destroyMethod", ParameterValueType.STRING);
		lazyInit = new ParameterDefine("lazyInit", ParameterValueType.BOOLEAN);
		parent = new ParameterDefine("parent", ParameterValueType.STRING);
		override = new ParameterDefine("override", ParameterValueType.BOOLEAN);
		constructor = new ParameterDefine("constructor", new ConstructorParameters());
		
		parameterDefines = new ParameterDefine[] {
				id,
				bean,
				method,
				hidden,
				arguments,
				destroyMethod,
				lazyInit,
				parent,
				override,
				constructor,
				
		};
	}
	
	public ActionParameters() {
		super(ActionParameters.class.getName(), parameterDefines);
	}
	
	public ActionParameters(String plaintext) {
		super(ActionParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
