package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class BeanParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine clazz;
	public static final ParameterDefine scope;
	public static final ParameterDefine factoryMethod;
	public static final ParameterDefine initMethod;
	public static final ParameterDefine destroyMethod;
	public static final ParameterDefine lazyInit;
	public static final ParameterDefine parent;
	public static final ParameterDefine override;
	public static final ParameterDefine constructor;
	public static final ParameterDefine properties;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		clazz = new ParameterDefine("class", ParameterValueType.STRING);
		scope = new ParameterDefine("scope", ParameterValueType.STRING);
		factoryMethod = new ParameterDefine("factoryMethod", ParameterValueType.STRING);
		initMethod = new ParameterDefine("initMethod", ParameterValueType.STRING);
		destroyMethod = new ParameterDefine("destroyMethod", ParameterValueType.STRING);
		lazyInit = new ParameterDefine("lazyInit", ParameterValueType.BOOLEAN);
		parent = new ParameterDefine("parent", ParameterValueType.STRING);
		override = new ParameterDefine("override", ParameterValueType.BOOLEAN);
		constructor = new ParameterDefine("constructor", new ConstructorParameters());
		properties = new ParameterDefine("properties", new ItemParameters(), true);
		
		parameterDefines = new ParameterDefine[] {
				id,
				clazz,
				scope,
				factoryMethod,
				initMethod,
				destroyMethod,
				lazyInit,
				parent,
				override,
				constructor,
				properties
		};
	}
	
	public BeanParameters() {
		super(BeanParameters.class.getName(), parameterDefines);
	}
	
	public BeanParameters(String plaintext) {
		super(BeanParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
