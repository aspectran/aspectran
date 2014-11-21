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

	protected final Map<String, ParameterValue> parameterValueMap;
	
	private final String title;
	
	private boolean preparsed;
	
	protected AbstractParameters(String title, ParameterValue[] parameterValues) {
		this(title, parameterValues, null);
	}

	protected AbstractParameters(String title, ParameterValue[] parameterValues, String plaintext) {
		this.title = title;
		
		// pre parsing
		if(parameterValues == null && plaintext != null) {
			parameterValues = preparse(plaintext);
		}
		
		this.parameterValueMap = new HashMap<String, ParameterValue>();
		
		for(ParameterValue parameterValue : parameterValues) {
			parameterValueMap.put(parameterValue.getName(), parameterValue);
		}

		if(plaintext != null)
			parse(plaintext);
	}

	public Object getValue(String name) {
		ParameterValue o = parameterValueMap.get(name);
		
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
		ParameterValue o = parameterValueMap.get(name);
		
		if(o == null || o.getValue() == null || o.getParameterValueType() != ParameterValueType.INTEGER)
			return defaultValue;
		
		return ((Integer)o.getValue()).intValue();
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		ParameterValue o = parameterValueMap.get(name);
		
		if(o == null || o.getValue() == null || o.getParameterValueType() != ParameterValueType.BOOLEAN)
			return defaultValue;
		
		return ((Boolean)o.getValue()).booleanValue();
		
	}
	
	public String[] getStringArray(String name) {
		ParameterValue o = parameterValueMap.get(name);
		
		if(o == null)
			return null;
		
		return (String[])o.getValue();
	}
	
	public Parameters getParameters(String name) {
		ParameterValue o = parameterValueMap.get(name);
		
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

	public String toString() {
		return "";
	}
	
	protected ParameterValue[] preparse(String plaintext) {
		StringTokenizer st = new StringTokenizer(plaintext, DELIMITERS);
		List<ParameterValue> parameterValueList = new ArrayList<ParameterValue>();
		
		preparse(st, parameterValueList, null);
		
		preparsed = true;
		
		return parameterValueList.toArray(new ParameterValue[parameterValueList.size()]);
	}
	
	protected void preparse(StringTokenizer st, List<ParameterValue> parameterValueList, String openBraket) {
		String name = null;
		String value = null;
		
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			
			if(StringUtils.hasText(token)) {
				token = token.trim();

				if(openBraket != null) {
					if(openBraket == CURLY_BRAKET_OPEN && CURLY_BRAKET_CLOSE.equals(token) ||
							openBraket == SQUARE_BRAKET_OPEN && SQUARE_BRAKET_CLOSE.equals(token))
						return;
				}
				
				if(openBraket == SQUARE_BRAKET_OPEN) {
					value = token;
				} else {
					int index = token.indexOf(":");
	
					if(index == -1)
						throw new InvalidParameterException(title + ": Cannot parse into name-value pair. \"" + token + "\"");
					
					name = token.substring(0, index).trim();
					value = token.substring(index + 1).trim();
				}

				if(StringUtils.hasText(value)) {
					if(CURLY_BRAKET_OPEN.equals(value)) {
						ParameterValue pv = new ParameterValue(name, ParameterValueType.PARAMETERS);
						parameterValueList.add(pv);
						
						preparse(st, parameterValueList, CURLY_BRAKET_OPEN);
					} else if(SQUARE_BRAKET_OPEN.equals(value)) {
						ParameterValue pv = new ParameterArrayValue(name, ParameterValueType.STRING);
						parameterValueList.add(pv);

						preparse(st, parameterValueList, SQUARE_BRAKET_OPEN);
					} else {
						ParameterValue pv = new ParameterValue(name, ParameterValueType.STRING);
						parameterValueList.add(pv);
					}
				}
			}
		}
		
		if(openBraket != null) {
			if(openBraket == CURLY_BRAKET_OPEN) {
				throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an array of strings.");
			} else if(openBraket == SQUARE_BRAKET_OPEN) {
				throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an array of strings.");
			}
		}
	}
	
	protected void parse(String plaintext) {
		StringTokenizer st = new StringTokenizer(plaintext, DELIMITERS);

		parse(st, null, null);
	}
	
	protected void parse(StringTokenizer st, String openBraket, ParameterValue pv) {
		String name = null;
		String value = null;
		
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			
			if(StringUtils.hasText(token)) {
				token = token.trim();
				
				if(openBraket != null) {
					if(openBraket == CURLY_BRAKET_OPEN && CURLY_BRAKET_CLOSE.equals(token) ||
							openBraket == SQUARE_BRAKET_OPEN && SQUARE_BRAKET_CLOSE.equals(token))
						return;
				}
				
				if(openBraket == SQUARE_BRAKET_OPEN) {
					value = token;
				} else {
					int index = token.indexOf(":");
					
					if(index == -1)
						throw new InvalidParameterException(title + ": Cannot parse into name-value pair. \"" + token + "\"");
					
					name = token.substring(0, index).trim();
					value = token.substring(index + 1).trim();
					
					pv = parameterValueMap.get(name);
					
					if(pv == null)
						throw new InvalidParameterException(title + ": invalid parameter \"" + token + "\"");
				}
				
				if(StringUtils.hasText(value)) {
					if(pv.getParameterValueType() == ParameterValueType.PARAMETERS) {
						AbstractParameters parameters2 = (AbstractParameters)pv.getParameters();
						parameters2.parse(st, CURLY_BRAKET_OPEN, null);
					} else if(pv.isArray() && SQUARE_BRAKET_OPEN.equals(value)) {
						parse(st, SQUARE_BRAKET_OPEN, pv);
					} else if(pv.getParameterValueType() == ParameterValueType.STRING) {
						pv.setValue(value);
					} else if(pv.getParameterValueType() == ParameterValueType.INTEGER) {
						try {
							pv.setValue(new Integer(value));
						} catch(NumberFormatException ex) {
							throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an integer. \"" + token + "\"");
						}
					} else if(pv.getParameterValueType() == ParameterValueType.BOOLEAN) {
						pv.setValue(Boolean.valueOf(value));
					}
				}
			}
		}
		
		if(!preparsed && openBraket != null) {
			if(openBraket == CURLY_BRAKET_OPEN) {
				throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an array of strings.");
			} else if(openBraket == SQUARE_BRAKET_OPEN) {
				throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an array of strings.");
			}
		}
		
	}
	
}
