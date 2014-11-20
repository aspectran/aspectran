package com.aspectran.core.var.apon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.aspectran.core.util.StringUtils;

public abstract class AbstractParameters implements Parameters {

	private static final String DELIMITERS = "\n\r\f";
	
	private static final String CURLY_BRAKET_OPEN = "{";

	private static final String CURLY_BRAKET_CLOSE = "}";
	
	private static final String SQUARE_BRAKET_OPEN = "[";
	
	private static final String SQUARE_BRAKET_CLOSE = "]";
	
	protected final Map<String, ParameterValue> parameterMap;
	
	private final String title;
	
	protected AbstractParameters(String title, ParameterValue[] Parameters) {
		this(title, Parameters, null);
	}

	protected AbstractParameters(String title, ParameterValue[] Parameters, String plaintext) {
		this.title = title;
		
		this.parameterMap = new HashMap<String, ParameterValue>();
		
		for(ParameterValue parameter : Parameters) {
			parameterMap.put(parameter.getName(), parameter);
		}

		if(plaintext != null)
			parse(plaintext);
	}

	public Object getValue(String name) {
		ParameterValue o = parameterMap.get(name);
		
		if(o == null)
			return null;
		
		return o.getValue();
	}

	public Object getValue(ParameterValue parameter) {
		return getValue(parameter.getName());
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
		ParameterValue o = parameterMap.get(name);
		
		if(o == null || o.getValue() == null || o.getParameterValueType() != ParameterValueType.INTEGER)
			return defaultValue;
		
		return ((Integer)o.getValue()).intValue();
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		ParameterValue o = parameterMap.get(name);
		
		if(o == null || o.getValue() == null || o.getParameterValueType() != ParameterValueType.BOOLEAN)
			return defaultValue;
		
		return ((Boolean)o.getValue()).booleanValue();
		
	}
	
	public String[] getStringArray(String name) {
		ParameterValue o = parameterMap.get(name);
		
		if(o == null)
			return null;
		
		return (String[])o.getValue();
	}
	
	public Parameters getParameters(String name) {
		ParameterValue o = parameterMap.get(name);
		
		if(o == null)
			return null;
		
		return (Parameters)o.getValue();
	}
	
	public String getString(ParameterValue parameter) {
		return getString(parameter.getName());
	}
	
	public String getString(ParameterValue parameter, String defaultValue) {
		return getString(parameter.getName(), defaultValue);
	}
	
	public int getInt(ParameterValue parameter, int defaultValue) {
		return getInt(parameter.getName(), defaultValue);
	}
	
	public boolean getBoolean(ParameterValue parameter, boolean defaultValue) {
		return getBoolean(parameter.getName(), defaultValue);
	}
	
	public String[] getStringArray(ParameterValue parameter) {
		return getStringArray(parameter.getName());
	}

	public Parameters getParameters(ParameterValue parameter) {
		return getParameters(parameter.getName());
	}

	protected void parse(String plaintext) {
		StringTokenizer st = new StringTokenizer(plaintext, DELIMITERS);

		parse(st, false);
	}
	
	protected void parse(StringTokenizer st, boolean subParameters) {
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			
			if(StringUtils.hasText(token)) {
				token = token.trim();

				if(subParameters) {
					if(CURLY_BRAKET_CLOSE.equals(token))
						return;
				}
				
				int index = token.indexOf(":");

				if(index == -1)
					throw new InvalidParameterException(title + ": Cannot parse into name-value pair. \"" + token + "\"");
				
				String name = token.substring(0, index).trim();
				String value = token.substring(index + 1).trim();

				ParameterValue parameter = parameterMap.get(name);
				
				if(parameter == null)
					throw new InvalidParameterException(title + ": invalid parameter \"" + token + "\"");
				
				if(StringUtils.hasText(value)) {
					if(parameter.getParameterValueType() == ParameterValueType.STRING) {
						parameter.setValue(value);
					} else if(parameter.getParameterValueType() == ParameterValueType.INTEGER) {
						try {
							parameter.setValue(new Integer(value));
						} catch(NumberFormatException ex) {
							throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an integer. \"" + token + "\"");
						}
					} else if(parameter.getParameterValueType() == ParameterValueType.BOOLEAN) {
						parameter.setValue(Boolean.valueOf(value));
					} else if(parameter.getParameterValueType() == ParameterValueType.STRING_ARRAY) {
						if(SQUARE_BRAKET_OPEN.equals(value)) {
							List<String> stringList = new ArrayList<String>();
							boolean squareBraceClosed = false;

							while(st.hasMoreTokens()) {
								token = st.nextToken();
								value = token.trim();
								
								if(SQUARE_BRAKET_CLOSE.equals(value)) {
									squareBraceClosed = true;
									break;
								}
								
								if(StringUtils.hasText(value)) {
									stringList.add(value);
								}
							}
							
							if(!squareBraceClosed)
								throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an array of strings. \"" + token + "\"");
							
							parameter.setValue(stringList.toArray(new String[stringList.size()]));
						} else {
							String[] stringArray = new String[] { value };
							parameter.setValue(stringArray);
						}
					} else if(parameter.getParameterValueType() == ParameterValueType.PARAMETERS) {
						if(CURLY_BRAKET_OPEN.equals(value)) {
							AbstractParameters Parameters2 = (AbstractParameters)parameter.getParameters();
							Parameters2.parse(st, true);
						}
					}
				}
			}
		}
	}
	
}
