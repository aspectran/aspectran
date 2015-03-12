package com.aspectran.core.util.apon;


public class GenericParameters extends AbstractParameters implements Parameters {

	public GenericParameters() {
		super(null);
	}
	
	public GenericParameters(String text) {
		super(null, text);
	}

	public GenericParameters(ParameterDefine[] parameterDefines) {
		super(parameterDefines);
	}
	
	public GenericParameters(ParameterDefine[] parameterDefines, String text) {
		super(parameterDefines, text);
	}
	
	public void putValue(String name, Object value) {
		Parameter p = touchParameterValue(name, value);
		p.putValue(value);
	}
	
	private Parameter touchParameterValue(String name, Object value) {
		Parameter p = parameterValueMap.get(name);
		
		if(p == null && isAddable())
			p = newParameterValue(name, determineParameterValueType(value));
		
		if(p == null)
			throw new UnknownParameterException(name, this);
		
		return p;
	}

	private ParameterValue newParameterValue(String name, ParameterValueType parameterValueType) {
		ParameterValue p = new ParameterValue(name, parameterValueType);
		parameterValueMap.put(name, p);
		return p;
	}

	private ParameterValueType determineParameterValueType(Object value) {
		ParameterValueType parameterValueType;
		
		if(value instanceof String) {
			if(value.toString().indexOf(AponFormat.NEXT_LINE_CHAR) == -1)
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
