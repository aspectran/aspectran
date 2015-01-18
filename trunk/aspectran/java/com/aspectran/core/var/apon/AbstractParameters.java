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

	protected Map<String, ParameterDefine> parameterDefineMap;
	
	private final String title;
	
	private String text;
	
	private ParameterDefine parent;
	
	private boolean preparsed;
	
	private boolean addable;
	
	protected AbstractParameters(String title, ParameterDefine[] parameterDefines) {
		this(title, parameterDefines, null);
	}

	protected AbstractParameters(String title, ParameterDefine[] parameterDefines, String text) {
		this.title = title;
		
		parseText(text, parameterDefines);
		
		if(parameterDefines == null && text == null)
			addable = true;
	}

	public void parseText(String text) {
		parseText(text, null);
	}
	
	public void parseText(String text, ParameterDefine[] parameterDefines) {
		this.text = text;
		
		if(parameterDefines == null && text != null) {
			parameterDefines = preparse(text);
		}
		
		this.parameterDefineMap = new HashMap<String, ParameterDefine>();

		if(parameterDefines != null) {
			for(ParameterDefine pd : parameterDefines) {
				pd.setHolder(this);
				parameterDefineMap.put(pd.getName(), pd);
			}
		}

		if(text != null)
			valuelize(text);
	}
	
	public void addParameterDefine(ParameterDefine parameterDefine) {
		parameterDefineMap.put(parameterDefine.getName(), parameterDefine);
	}
	
	public ParameterDefine getParent() {
		return parent;
	}

	public void setParent(ParameterDefine parent) {
		this.parent = parent;
	}

	public String getTitle() {
		return title;
	}

	public String getQualifiedName() {
		if(parent != null)
			return parent.getQualifiedName();
		
		return title;
	}

	public Parameter getParameter(String name) {
		Parameter p = parameterDefineMap.get(name);
		
		if(p == null)
			throw new UnknownParameterException(name, this);
		
		return p;
	}

	public Parameter getParameter(ParameterDefine parameterDefine) {
		return getParameter(parameterDefine.getName());
	}
	
	public Object getValue(String name) {
		Parameter p = getParameter(name);
		return p.getValue();
	}
	
	public Object getValue(ParameterDefine parameter) {
		return getValue(parameter.getName());
	}
	
	public String getString(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsString();
	}

	public String getString(String name, String defaultValue) {
		String s = getString(name);
		
		if(s == null)
			return defaultValue;
		
		return s;
	}

	public String[] getStringArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsStringArray();
	}

	public String getString(ParameterDefine parameter) {
		return getString(parameter.getName());
	}
	
	public String getString(ParameterDefine parameter, String defaultValue) {
		return getString(parameter.getName(), defaultValue);
	}

	public String[] getStringArray(ParameterDefine parameter) {
		return getStringArray(parameter.getName());
	}
	
	public int getInt(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsInt();
	}
	
	public int getInt(String name, int defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsInt();
	}

	public int[] getIntArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsIntArray();
	}

	public int getInt(ParameterDefine parameter) {
		return getInt(parameter.getName());
	}

	public int getInt(ParameterDefine parameter, int defaultValue) {
		return getInt(parameter.getName(), defaultValue);
	}

	public int[] getIntArray(ParameterDefine parameter) {
		return getIntArray(parameter.getName());
	}
	
	public long getLong(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsLong();
	}
	
	public long getLong(String name, long defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsLong();
	}
	
	public long[] getLongArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsLongArray();
	}
	
	public long getLong(ParameterDefine parameter) {
		return getLong(parameter.getName());
	}
	
	public long getLong(ParameterDefine parameter, long defaultValue) {
		return getLong(parameter.getName());
	}
	
	public long[] getLongArray(ParameterDefine parameter) {
		return getLongArray(parameter.getName());
	}
	
	public float getFloat(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsFloat();
	}
	
	public float getFloat(String name, float defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsFloat();
	}

	public float[] getFloatArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsFloatArray();
	}
	
	public float getFloat(ParameterDefine parameter) {
		return getFloat(parameter.getName());
	}
	
	public float getFloat(ParameterDefine parameter, float defaultValue) {
		return getFloat(parameter.getName(), defaultValue);
	}
	
	public float[] getFloatArray(ParameterDefine parameter) {
		return getFloatArray(parameter.getName());
	}
	
	public double getDouble(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsDouble();
	}
	
	public double getDouble(String name, double defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsDouble();
	}

	public double[] getDoubleArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsDoubleArray();
	}
	
	public double getDouble(ParameterDefine parameter) {
		return getDouble(parameter.getName());
	}
	
	public double getDouble(ParameterDefine parameter, double defaultValue) {
		return getDouble(parameter.getName(), defaultValue);
	}
	
	public double[] getDoubleArray(ParameterDefine parameter) {
		return getDoubleArray(parameter.getName());
	}
	
	public boolean getBoolean(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsBoolean();
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		Parameter p = getParameter(name);
		
		if(p == null)
			return defaultValue;
		
		return p.getValueAsBoolean();
	}
	
	public boolean[] getBooleanArray(String name) {
		Parameter p = getParameter(name);
		return p.getValueAsBooleanArray();
	}
	
	public boolean getBoolean(ParameterDefine parameter) {
		return getBoolean(parameter.getName());
	}
	
	public boolean getBoolean(ParameterDefine parameter, boolean defaultValue) {
		return getBoolean(parameter.getName(), defaultValue);
	}
	
	public boolean[] getBooleanArray(ParameterDefine parameter) {
		return getBooleanArray(parameter.getName());
	}
	
	public Parameters getParameters(String name) {
		Parameter p = getParameter(name);
		return (Parameters)p.getValue();
	}
	
	public Parameters[] getParametersArray(String name) {
		Parameter p = getParameter(name);
		return p.getParametersArray();
	}
	
	public Parameters getParameters(ParameterDefine parameter) {
		return getParameters(parameter.getName());
	}
	
	public Parameters[] getParametersArray(ParameterDefine parameter) {
		return getParametersArray(parameter.getName());
	}
	
	public String toText() {
		return text;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{title=").append(title);
		sb.append(", qualifiedName=").append(getQualifiedName());
		sb.append("}");
		
		return sb.toString();
	}
	
	protected ParameterDefine[] preparse(String text) {
		StringTokenizer st = new StringTokenizer(text, DELIMITERS);
		
		ParameterDefine[] parameterDefines = preparse(st, null);
		
		preparsed = true;
		
		return parameterDefines;
	}
	
	protected ParameterDefine[] preparse(StringTokenizer st, ParameterDefine parentParameterDefine) {
		List<ParameterDefine> parameterDefineList = new ArrayList<ParameterDefine>();
		
		String openBraket = parentParameterDefine != null ? CURLY_BRAKET_OPEN : null;
		
		preparse(st, parameterDefineList, openBraket, null);
		
		ParameterDefine[] parameterDefines = parameterDefineList.toArray(new ParameterDefine[parameterDefineList.size()]);
		
		if(parentParameterDefine != null) {
			Parameters parameters = new GenericParameters(parentParameterDefine.getName(), parameterDefines);
			parentParameterDefine.setValue(parameters);
		}
		
		return parameterDefines;
	}
	
	protected void preparse(StringTokenizer st, List<ParameterDefine> parameterDefineList, String openBraket, ParameterDefine parameterDefine) {
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
							preparse(st, parameterDefine);
						} else {
							ParameterDefine pd = new ParameterDefine(name, ParameterValueType.PARAMETERS);
							parameterDefineList.add(pd);
							preparse(st, pd);
						}
					} else if(openBraket != SQUARE_BRAKET_OPEN) {
						ParameterValueType valueType = ParameterValueType.valueOfHint(name);
						
						if(valueType != null) {
							name = ParameterValueType.stripValueTypeHint(name);
						} else {
							valueType = ParameterValueType.STRING;
						}

						if(SQUARE_BRAKET_OPEN.equals(value)) {
							ParameterDefine pd = new ParameterDefine(name, valueType, true);
							parameterDefineList.add(pd);
	
							preparse(st, parameterDefineList, SQUARE_BRAKET_OPEN, pd);
						} else {
							ParameterDefine pd = new ParameterDefine(name, valueType);
							parameterDefineList.add(pd);
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
	
	protected void valuelize(String text) {
		StringTokenizer st = new StringTokenizer(text, DELIMITERS);

		valuelize(st, null, null, null);
	}
	
	protected void valuelize(StringTokenizer st, String openBraket, String name, ParameterDefine parameterDefine) {
		ParameterValueType parameterValueType = null; 
		String value = null;
		
		int curlyBraketCount = 0;
		
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			
			if(StringUtils.hasText(token)) {
				token = token.trim();
				
				if(openBraket != null) {
					if(openBraket == CURLY_BRAKET_OPEN && CURLY_BRAKET_CLOSE.equals(token) ||
							openBraket == SQUARE_BRAKET_OPEN && SQUARE_BRAKET_CLOSE.equals(token)) {
						//System.out.println("*****return********* openBraket: " + openBraket + ", token: " + token);
						return;
					}
				}
				
				if(openBraket == SQUARE_BRAKET_OPEN) {
					value = token;
				} else {
					int index = token.indexOf(":");
					
					if(index == -1)
						throw new InvalidParameterException(title + ": Cannot parse into name-value pair. \"" + token + "\"");
					
					name = token.substring(0, index).trim();
					value = token.substring(index + 1).trim();
					
					parameterDefine = parameterDefineMap.get(name);

					//System.out.println("************** title: " + title);
					//System.out.println("************** name: " + name + ", value: " + value + ", token: " + token);
					//System.out.println("************** parameterDefine: " + parameterDefine);
					
					if(parameterDefine == null) {
						if(addable) {
							parameterValueType = ParameterValueType.valueOfHint(name);
							if(parameterValueType != null) {
								name = ParameterValueType.stripValueTypeHint(name);
								parameterDefine = parameterDefineMap.get(name);
							}
						} else {
							throw new InvalidParameterException(title + ": invalid parameter \"" + token + "\"");
						}
					}
				}

				if(parameterDefine != null && parameterDefine.getParameterValueType() != ParameterValueType.VARIABLE) {
					parameterValueType = parameterDefine.getParameterValueType();
				} else {
					parameterValueType = null;
				}
				
				if(StringUtils.hasText(value)) {
					if(parameterValueType == null && CURLY_BRAKET_OPEN.equals(value)) {
						parameterValueType = ParameterValueType.PARAMETERS;
					} else if(SQUARE_BRAKET_OPEN.equals(value)) {
						if(parameterDefine == null || (parameterDefine != null && parameterDefine.isArray())) {
							//System.out.println("************** name: " + name);
							//System.out.println("************** parameterDefine: " + parameterDefine);
							valuelize(st, SQUARE_BRAKET_OPEN, name, parameterDefine);
							continue;
						}
					}
					
					if(parameterValueType == ParameterValueType.PARAMETERS) {
						if(openBraket == SQUARE_BRAKET_OPEN) {
							if(parameterDefine == null) {
								parameterDefine = new ParameterDefine(name, parameterValueType, true);
								parameterDefineMap.put(name, parameterDefine);
							}

							AbstractParameters parameters2 = (AbstractParameters)parameterDefine.getParameters(curlyBraketCount++);
							
							if(parameters2 == null)
								parameters2 = (AbstractParameters)parameterDefine.getParameters();
							
							if(parameters2 == null)
								throw new InvalidParameterException("Cannot parse parameter value of '" + name + "'. parameters is null.");
							
							parameters2.valuelize(st, CURLY_BRAKET_OPEN, null, null);
						} else {
							if(parameterDefine == null) {
								parameterDefine = new ParameterDefine(name, parameterValueType);
								parameterDefineMap.put(name, parameterDefine);
							}

							AbstractParameters parameters2 = (AbstractParameters)parameterDefine.getParameters();
							parameters2.valuelize(st, CURLY_BRAKET_OPEN, null, null);
						}
					} else {
						if(parameterValueType == null)
							parameterValueType = ParameterValueType.STRING;

						if(parameterDefine == null) {
							parameterDefine = new ParameterDefine(name, parameterValueType, (openBraket == SQUARE_BRAKET_OPEN));
							parameterDefineMap.put(name, parameterDefine);
						}

						if(parameterValueType == ParameterValueType.INTEGER) {
							try {
								parameterDefine.setValue(new Integer(value));
							} catch(NumberFormatException ex) {
								throw new InvalidParameterException(title + ": Cannot parse value of '" + name + "' to an integer. \"" + token + "\"");
							}
						} else if(parameterValueType == ParameterValueType.BOOLEAN) {
							parameterDefine.setValue(Boolean.valueOf(value));
						} else {
							parameterDefine.setValue(value);
						}
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
