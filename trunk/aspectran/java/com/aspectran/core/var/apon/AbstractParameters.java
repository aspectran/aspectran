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
	
	private final String plaintext;
	
	private boolean preparsed;
	
	protected AbstractParameters(String title, ParameterValue[] parameterValues) {
		this(title, parameterValues, null);
	}

	protected AbstractParameters(String title, ParameterValue[] parameterValues, String plaintext) {
		this.title = title;
		this.plaintext = plaintext;
		
		if(parameterValues == null && plaintext != null) {
			parameterValues = preparse(plaintext);
		}
		
		this.parameterValueMap = new HashMap<String, ParameterValue>();
		
		for(ParameterValue parameterValue : parameterValues) {
			parameterValueMap.put(parameterValue.getName(), parameterValue);
		}

		if(plaintext != null)
			valuelize(plaintext);
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
		ParameterValue v = parameterValueMap.get(name);
		
		if(v == null)
			return null;
		
		return v.getString();
	}
	
	public String getString(String name, String defaultValue) {
		String s = getString(name);
		
		if(s == null)
			return defaultValue;
		
		return s;
	}

	public String[] getStringArray(String name) {
		ParameterValue v = parameterValueMap.get(name);
		
		if(v == null)
			return null;
		
		return (String[])v.getValues();
	}
	
	public int getInt(String name) {
		ParameterValue v = parameterValueMap.get(name);
		
		if(v == null)
			throw new UnknownParameterException(name);
			
		return v.getInt();
	}
	
	public int getInt(String name, int defaultValue) {
		ParameterValue v = parameterValueMap.get(name);
		
		if(v == null)
			throw new UnknownParameterException(name);
		
		return v.getInt();
	}
	
	public boolean getBoolean(String name) {
		ParameterValue v = parameterValueMap.get(name);
		
		if(v == null)
			throw new UnknownParameterException(name);
		
		return v.getBoolean();
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		ParameterValue v = parameterValueMap.get(name);
		
		if(v == null)
			throw new UnknownParameterException(name);
		
		return v.getBoolean();
	}
	
	public Parameters getParameters(String name) {
		ParameterValue v = parameterValueMap.get(name);
		
		if(v == null)
			return null;
		
		return (Parameters)v.getValue();
	}
	
	public String getString(ParameterValue parameter) {
		return getString(parameter.getName());
	}
	
	public String getString(ParameterValue parameter, String defaultValue) {
		return getString(parameter.getName(), defaultValue);
	}

	public String[] getStringArray(ParameterValue parameter) {
		return getStringArray(parameter.getName());
	}
	
	public int getInt(ParameterValue parameter, int defaultValue) {
		return getInt(parameter.getName(), defaultValue);
	}
	
	public boolean getBoolean(ParameterValue parameter, boolean defaultValue) {
		return getBoolean(parameter.getName(), defaultValue);
	}
	
	public Parameters getParameters(ParameterValue parameter) {
		return getParameters(parameter.getName());
	}

	public String toString() {
		return plaintext;
	}
	
	private boolean isValidType(ParameterValue v, ParameterValueType parameterValueType) {
		if(v == null || v.getValue() == null || v.getParameterValueType() != parameterValueType)
			return false;
		
		return true;
	}
	
	protected ParameterValue[] preparse(String plaintext) {
		StringTokenizer st = new StringTokenizer(plaintext, DELIMITERS);
		
		ParameterValue[] parameterValues = preparse(st, null);
		
		preparsed = true;
		
		return parameterValues;
	}
	
	protected ParameterValue[] preparse(StringTokenizer st, ParameterValue parentParameterValue) {
		List<ParameterValue> parameterValueList = new ArrayList<ParameterValue>();
		
		preparse(st, parameterValueList, parentParameterValue != null ? CURLY_BRAKET_OPEN : null, null);
		
		ParameterValue[] parameterValues = parameterValueList.toArray(new ParameterValue[parameterValueList.size()]);
		
		if(parentParameterValue != null) {
			Parameters parameters = new GenericParameters(parentParameterValue.getName(), parameterValues);
			parentParameterValue.setParameters(parameters);
		}
		
		return parameterValues;
	}
	
	protected void preparse(StringTokenizer st, List<ParameterValue> parameterValueList, String openBraket, ParameterValue parameterValue) {
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
						if(openBraket == SQUARE_BRAKET_OPEN) {
							preparse(st, parameterValue);
						} else {
							ParameterValue pv = new ParameterValue(name, ParameterValueType.PARAMETERS);
							parameterValueList.add(pv);
							preparse(st, pv);
						}
					} else if(openBraket != SQUARE_BRAKET_OPEN) {
						ParameterValueType parameterValueType = ParameterValueType.valueOfHint(name);
						
						if(parameterValueType == null)
							parameterValueType = ParameterValueType.STRING;

						if(SQUARE_BRAKET_OPEN.equals(value)) {
							ParameterValue pv = new ParameterArrayValue(name, parameterValueType);
							parameterValueList.add(pv);
	
							preparse(st, parameterValueList, SQUARE_BRAKET_OPEN, pv);
						} else {
							ParameterValue pv = new ParameterValue(name, parameterValueType);
							parameterValueList.add(pv);
						}
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
	
	protected void valuelize(String plaintext) {
		StringTokenizer st = new StringTokenizer(plaintext, DELIMITERS);

		valuelize(st, null, null);
	}
	
	protected void valuelize(StringTokenizer st, String openBraket, ParameterValue parameterValue) {
		String name = null;
		String value = null;
		
		int curlyBraketCount = 0;
		
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
					name = parameterValue.getName();
					value = token;
				} else {
					int index = token.indexOf(":");
					
					if(index == -1)
						throw new InvalidParameterException(title + ": Cannot parse into name-value pair. \"" + token + "\"");
					
					name = token.substring(0, index).trim();
					value = token.substring(index + 1).trim();
					
					parameterValue = parameterValueMap.get(name);
					
					if(parameterValue == null)
						throw new InvalidParameterException(title + ": invalid parameter \"" + token + "\"");
				}
				
				if(StringUtils.hasText(value)) {
					ParameterValueType parameterValueType = parameterValue.getParameterValueType();
					
					if(CURLY_BRAKET_OPEN.equals(value)) {
						if(openBraket == SQUARE_BRAKET_OPEN) {
							AbstractParameters parameters2 = (AbstractParameters)parameterValue.getParameters(curlyBraketCount++);
							
							if(parameters2 == null)
								parameters2 = (AbstractParameters)parameterValue.getParameters();
							
							if(parameters2 == null)
								throw new InvalidParameterException("Cannot parse parameter value of '" + name + "'. parameters is null.");
							
							parameters2.valuelize(st, CURLY_BRAKET_OPEN, null);
						} else {
							AbstractParameters parameters2 = (AbstractParameters)parameterValue.getParameters();
							parameters2.valuelize(st, CURLY_BRAKET_OPEN, null);
						}
					} else if(SQUARE_BRAKET_OPEN.equals(value)) {
						valuelize(st, SQUARE_BRAKET_OPEN, parameterValue);
					} else if(parameterValueType == ParameterValueType.STRING) {
						parameterValue.setValue(value);
					} else if(parameterValueType == ParameterValueType.INTEGER) {
						try {
							parameterValue.setValue(new Integer(value));
						} catch(NumberFormatException ex) {
							throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an integer. \"" + token + "\"");
						}
					} else if(parameterValueType == ParameterValueType.BOOLEAN) {
						parameterValue.setValue(Boolean.valueOf(value));
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
