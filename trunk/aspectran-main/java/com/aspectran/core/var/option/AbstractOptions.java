package com.aspectran.core.var.option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.aspectran.core.util.StringUtils;

public abstract class AbstractOptions implements Options {

	private static final String DELIMITERS = "\n\r\f";
	
	protected final Map<String, Option> optionMap;
	
	private final String description;
	
	protected AbstractOptions(Option[] options, String description) {
		this.optionMap = new HashMap<String, Option>();
		
		for(Option option : options) {
			optionMap.put(option.getName(), option);
		}

		this.description = description;
	}

	public Map<String, Option> getOptionMap() {
		return optionMap;
	}
	
	public Object getValue(String name) {
		Option o = optionMap.get(name);
		
		if(o == null)
			return null;
		
		return o.getValue();
	}

	public Object getValue(Option option) {
		return getValue(option.getName());
	}
	
	public String getString(String name) {
		Object o = getValue(name);
		
		if(o == null)
			return null;
		
		return o.toString();
	}
	
	public String getString(String name, String defaultValue) {
		String val = getString(name);
		
		if(val == null)
			return defaultValue;
		
		return val;
	}

	public int getInt(String name, int defaultValue) {
		Option o = optionMap.get(name);
		
		if(o == null || o.getValueType() != OptionValueType.INTEGER)
			return defaultValue;
		
		return ((Integer)o.getValue()).intValue();
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		Option o = optionMap.get(name);
		
		if(o == null || o.getValueType() != OptionValueType.BOOLEAN)
			return defaultValue;
		
		return ((Boolean)o.getValue()).booleanValue();
		
	}
	
	public String[] getStringArray(String name) {
		Option o = optionMap.get(name);
		
		if(o == null)
			return null;
		
		return (String[])o.getValue();
	}
	
	public String getString(Option option) {
		return getString(option.getName());
	}
	
	public String getString(Option option, String defaultValue) {
		return getString(option.getName(), defaultValue);
	}
	
	public int getInt(Option option, int defaultValue) {
		return getInt(option.getName(), defaultValue);
	}
	
	public boolean getBoolean(Option option, boolean defaultValue) {
		return getBoolean(option.getName(), defaultValue);
	}
	
	public String[] getStringArray(Option option) {
		return getStringArray(option.getName());
	}

	protected void parse(String statement) throws InvalidOptionException {
		StringTokenizer st = new StringTokenizer(statement, DELIMITERS);

		parse(st, false);
	}
	
	protected void parse(StringTokenizer st, boolean subOptions) throws InvalidOptionException {
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			
			if(StringUtils.hasText(token)) {
				token = token.trim();
				
				if(subOptions) {
					if("}".equals(token))
						return;
				}
				
				int index = token.indexOf(":");

				String name = token.substring(0, index).trim();
				String value = token.substring(index + 1).trim();

				Option option = optionMap.get(name);
				
				if(option == null)
					throw new InvalidOptionException(description + ": invalid option \"" + token + "\"");
				
				if(StringUtils.hasText(value)) {
					if(option.getValueType() == OptionValueType.STRING) {
						option.setValue(value);
					} else if(option.getValueType() == OptionValueType.INTEGER) {
						try {
							option.setValue(new Integer(value));
						} catch(NumberFormatException ex) {
							throw new InvalidOptionException(description + ": Cannot parse value of '" + name + "' to an integer. \"" + token + "\"");
						}
					} else if(option.getValueType() == OptionValueType.BOOLEAN) {
						option.setValue(Boolean.valueOf(value));
					} else if(option.getValueType() == OptionValueType.STRING_ARRAY) {
						if("[".equals(value)) {
							List<String> stringList = new ArrayList<String>();
							boolean braceClosed = false;

							while(st.hasMoreTokens()) {
								token = st.nextToken();
								value = token.trim();
								
								if("]".equals(value)) {
									braceClosed = true;
									break;
								}
								
								if(StringUtils.hasText(value)) {
									stringList.add(value);
								}
							}
							
							if(!braceClosed)
								throw new InvalidOptionException(description + ": Cannot parse value of '" + name + "' to an array of strings. \"" + token + "\"");
							
							option.setValue(stringList.toArray(new String[stringList.size()]));
						} else {
							String[] stringArray = new String[] { value };
							option.setValue(stringArray);
						}
					} else if(option.getValueType() == OptionValueType.OPTIONS) {
						if("{".equals(value)) {
							AbstractOptions options2 = (AbstractOptions)option.getOptions();
							options2.parse(st, true);
						}
					}
				}
			}
		}
	}
	
}
