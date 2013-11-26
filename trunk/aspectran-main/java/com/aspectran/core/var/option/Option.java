package com.aspectran.core.var.option;

public class Option {

	private String name;
	
	private Object value;
	
	private OptionValueType valueType;
	
	public Option(String name, OptionValueType valueType) {
		this.name = name;
		this.valueType = valueType;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public OptionValueType getValueType() {
		return valueType;
	}
	
}
