package com.aspectran.core.util.apon;


public class GenericParameters extends AbstractParameters implements Parameters {

	public GenericParameters() {
		super(null, null, null);
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

	/*	
	public void putString(String name, String value) {
		ParameterValue p = newParameterValue(name, ParameterValueType.STRING);
		p.putValue(value);
	}

	public void putString(String name, String[] values) {
		ParameterValue p = newParameterValue(name, ParameterValueType.STRING);
		for(String value : values) {
			p.putValue(value);
		}
	}
	
	public void putInt(String name, Integer value) {
		ParameterValue p = newParameterValue(name, ParameterValueType.INT);
		p.putValue(value);
	}
	
	public void putInt(String name, Integer[] values) {
		ParameterValue p = newParameterValue(name, ParameterValueType.INT);
		for(Integer value : values) {
			p.putValue(value);
		}
	}
	
	public void putValue(String name, Float value) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.FLOAT);
		p.putValue(value);
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Float[] values) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.FLOAT, true);
		for(Float value : values) {
			p.putValue(value);
		}
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Float value) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.FLOAT);
		p.putValue(value);
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Float[] values) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.FLOAT, true);
		for(Float value : values) {
			p.putValue(value);
		}
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Double value) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.DOUBLE);
		p.putValue(value);
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Double[] values) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.DOUBLE, true);
		for(Double value : values) {
			p.putValue(value);
		}
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Boolean value) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.BOOLEAN);
		p.putValue(value);
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Boolean[] values) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.BOOLEAN, true);
		for(Boolean value : values) {
			p.putValue(value);
		}
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Parameters value) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.PARAMETERS);
		p.putValue(value);
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Parameters[] values) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.PARAMETERS, true);
		for(Parameters value : values) {
			p.putValue(value);
		}
		parameterValueMap.put(name, p);
	}
	
	public static Parameters toParameters(String text) {
		Parameters parameters = new GenericParameters(text);
		return parameters;
	}
*/
}
