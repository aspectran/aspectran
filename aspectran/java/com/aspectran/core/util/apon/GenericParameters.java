package com.aspectran.core.util.apon;


public class GenericParameters extends AbstractParameters implements Parameters {

	public GenericParameters() {
		super(null, null);
	}
	
	public GenericParameters(String text) {
		super(null, null, text);
	}

	public GenericParameters(String title, String text) {
		super(title, null, text);
	}

	public GenericParameters(String title, ParameterDefine[] parameterDefines) {
		super(title, parameterDefines);
	}
	
	public GenericParameters(String title, ParameterDefine[] parameterDefines, String text) {
		super(title, parameterDefines, text);
	}

	public void setValue(String name, Object value) {
		ParameterValue p = newParameterValue(name, value);
		p.setValue(value);
	}
	
	public void putValue(String name, Object value) {
		ParameterValue p = newParameterValue(name, value);
		p.putValue(value);
	}

	private ParameterValue newParameterValue(String name, ParameterValueType parameterValueType) {
		ParameterValue p = new ParameterValue(name, parameterValueType);
		parameterValueMap.put(name, p);
		return p;
	}
	
	private ParameterValue newParameterValue(String name, Object value) {
		return newParameterValue(name, determineParameterValueType(value));
	}
	
	private ParameterValueType determineParameterValueType(Object value) {
		ParameterValueType parameterValueType;
		
		if(value instanceof String) {
			if(((String) value).indexOf(AponFormat.NEXT_LINE_CHAR) == -1)
				parameterValueType = ParameterValueType.STRING;
			else
				parameterValueType = ParameterValueType.TEXT;
		} else if(value instanceof Integer) {
			parameterValueType = ParameterValueType.INT;
		} else if(value instanceof Long) {
			parameterValueType = ParameterValueType.LONG;
		} else if(value instanceof Float) {
			parameterValueType = ParameterValueType.FLOAT;
		} else if(value instanceof Double) {
			parameterValueType = ParameterValueType.DOUBLE;
		} else if(value instanceof Boolean) {
			parameterValueType = ParameterValueType.BOOLEAN;
		} else if(value instanceof Parameters) {
			parameterValueType = ParameterValueType.PARAMETERS;
		} else {
			parameterValueType = ParameterValueType.STRING;
		}
		
		return parameterValueType;
	}

}
