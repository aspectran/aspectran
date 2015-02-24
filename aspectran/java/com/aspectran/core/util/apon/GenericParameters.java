package com.aspectran.core.util.apon;


public class GenericParameters extends AbstractParameters {

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
	
	public void putValue(String name, String value) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.STRING);
		p.putValue(value);
		parameterValueMap.put(name, p);
	}

	public void putValue(String name, String[] values) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.STRING, true);
		for(String value : values) {
			p.putValue(value);
		}
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Integer value) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.INT);
		p.putValue(value);
		parameterValueMap.put(name, p);
	}
	
	public void putValue(String name, Integer[] values) {
		ParameterValue p = new ParameterValue(name, ParameterValueType.INT, true);
		for(Integer value : values) {
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

}
