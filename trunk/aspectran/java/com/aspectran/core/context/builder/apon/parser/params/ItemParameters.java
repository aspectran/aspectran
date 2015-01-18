package com.aspectran.core.context.builder.apon.parser.params;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class ItemParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine type;
	public static final ParameterDefine name;
	public static final ParameterDefine value;
	public static final ParameterDefine valueType;
	public static final ParameterDefine defaultValue;
	public static final ParameterDefine tokenize;
	public static final ParameterDefine reference;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		type = new ParameterDefine("type", ParameterValueType.STRING);
		name = new ParameterDefine("name", ParameterValueType.STRING);
		value = new ParameterDefine("value", ParameterValueType.VARIABLE);
		valueType = new ParameterDefine("valueType", ParameterValueType.STRING);
		defaultValue = new ParameterDefine("defaultValue", ParameterValueType.STRING);
		tokenize = new ParameterDefine("tokenize", ParameterValueType.BOOLEAN);
		reference = new ParameterDefine("reference", new ReferenceParameters());
		
		parameterDefines = new ParameterDefine[] {
				name,
				value,
				type,
				valueType,
				defaultValue,
				tokenize,
				reference
		};
	}
	
	public ItemParameters() {
		super(ItemParameters.class.getName(), parameterDefines);
	}
	
	public ItemParameters(String plaintext) {
		super(ItemParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
