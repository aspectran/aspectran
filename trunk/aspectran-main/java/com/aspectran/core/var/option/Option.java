package com.aspectran.core.var.option;

public class Option {

	private final String name;
	
	private Object value;
	
	private final OptionValueType valueType;
	
	private final Options options;
	
	public Option(String name, OptionValueType valueType) {
		this(name, valueType, null);
	}
	
	public Option(String name, OptionValueType valueType, Options options) {
		this.name = name;
		this.valueType = valueType;
		
		if(valueType == OptionValueType.OPTIONS)
			this.options = options;
		else
			this.options = null;
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

	public Options getOptions() {
		return options;
	}
	
}
