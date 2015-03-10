package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class BeanParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	public static final ParameterDefine className;
	public static final ParameterDefine scope;
	public static final ParameterDefine singleton;
	public static final ParameterDefine factoryMethod;
	public static final ParameterDefine initMethod;
	public static final ParameterDefine destroyMethod;
	public static final ParameterDefine lazyInit;
	public static final ParameterDefine important;
	public static final ParameterDefine constructor;
	public static final ParameterDefine properties;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		className = new ParameterDefine("class", ParameterValueType.STRING);
		scope = new ParameterDefine("scope", ParameterValueType.STRING);
		singleton = new ParameterDefine("singleton", ParameterValueType.BOOLEAN);
		factoryMethod = new ParameterDefine("factoryMethod", ParameterValueType.STRING);
		initMethod = new ParameterDefine("initMethod", ParameterValueType.STRING);
		destroyMethod = new ParameterDefine("destroyMethod", ParameterValueType.STRING);
		lazyInit = new ParameterDefine("lazyInit", ParameterValueType.BOOLEAN);
		important = new ParameterDefine("important", ParameterValueType.BOOLEAN);
		constructor = new ParameterDefine("constructor", ConstructorParameters.class);
		properties = new ParameterDefine("property", ItemParameters.class, true);
		
		parameterDefines = new ParameterDefine[] {
				id,
				className,
				scope,
				singleton,
				factoryMethod,
				initMethod,
				destroyMethod,
				lazyInit,
				important,
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
