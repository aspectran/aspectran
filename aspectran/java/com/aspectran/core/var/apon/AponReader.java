package com.aspectran.core.var.apon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.aspectran.core.util.StringUtils;

public class AponReader {

	protected static final String CURLY_BRAKET_OPEN = "{";

	protected static final String CURLY_BRAKET_CLOSE = "}";
	
	protected static final String SQUARE_BRAKET_OPEN = "[";
	
	protected static final String SQUARE_BRAKET_CLOSE = "]";

	private static final String DELIMITERS = "\n\r\f";
	
	private Map<String, ParameterDefine> parameterDefineMap;
	
	private Parameters holder;
	
	private boolean preparsed;
	
	private boolean addable;
	
	public AponReader() {
	}

	public AponReader(Parameters holder) {
		this.holder = holder;
	}

	public void read(String text) {
		read(text, null);
	}
	
	public void read(String text, ParameterDefine[] parameterDefines) {
		if(parameterDefines == null && text != null) {
			parameterDefines = preparse(text);
		}
		
		this.parameterDefineMap = new LinkedHashMap<String, ParameterDefine>();

		if(parameterDefines != null) {
			for(ParameterDefine pd : parameterDefines) {
				if(holder != null)
					pd.setHolder(holder);
				
				parameterDefineMap.put(pd.getName(), pd);
			}
		}

		if(parameterDefines == null && text == null)
			addable = true;
		else
			addable = false;
		
		if(text != null)
			valuelize(text);
	}
	
	public ParameterDefine[] getParameterDefines() {
		Collection<ParameterDefine> values = parameterDefineMap.values();
		return values.toArray(new ParameterDefine[values.size()]);
	}
	
	public Map<String, ParameterDefine> getParameterDefineMap() {
		return parameterDefineMap;
	}
	
	public void addParameterDefine(ParameterDefine parameterDefine) {
		parameterDefineMap.put(parameterDefine.getName(), parameterDefine);
	}
	
	private ParameterDefine[] preparse(String text) {
		StringTokenizer st = new StringTokenizer(text, DELIMITERS);
		
		ParameterDefine[] parameterDefines = preparse(st, null);
		
		preparsed = true;
		
		return parameterDefines;
	}
	
	private ParameterDefine[] preparse(StringTokenizer st, ParameterDefine parentParameterDefine) {
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
	
	private void preparse(StringTokenizer st, List<ParameterDefine> parameterDefineList, String openBraket, ParameterDefine parameterDefine) {
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
						throw new InvalidParameterException("Cannot parse into name-value pair. \"" + token + "\"");

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
				throw new InvalidParameterException("Cannot parse value of '" + name + "' to an array of strings.");
			} else if(openBraket == SQUARE_BRAKET_OPEN) {
				throw new InvalidParameterException("Cannot parse value of '" + name + "' to an array of strings.");
			}
		}
	}
	
	private void valuelize(String text) {
		StringTokenizer st = new StringTokenizer(text, DELIMITERS);

		valuelize(parameterDefineMap, st, null, null, null);
	}
	
	private void valuelize(Map<String, ParameterDefine> parameterDefineMap, StringTokenizer st, String openBraket, String name, ParameterDefine parameterDefine) {
		ParameterValueType parameterValueType = null; 
		String value = null;
		
		//int curlyBraketCount = 0;
		
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
						throw new InvalidParameterException("Cannot parse into name-value pair. \"" + token + "\"");
					
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
							throw new InvalidParameterException("invalid parameter \"" + token + "\"");
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
							valuelize(parameterDefineMap, st, SQUARE_BRAKET_OPEN, name, parameterDefine);
							continue;
						}
					}
					
					if(parameterValueType == ParameterValueType.PARAMETERS) {
						if(openBraket == SQUARE_BRAKET_OPEN) {
							if(parameterDefine == null) {
								parameterDefine = new ParameterDefine(name, parameterValueType, true);
								parameterDefineMap.put(name, parameterDefine);
							}

							//AbstractParameters parameters2 = (AbstractParameters)parameterDefine.getParameters(curlyBraketCount++);
							AbstractParameters parameters2 = (AbstractParameters)parameterDefine.addParameters();
							
							if(parameters2 == null)
								parameters2 = (AbstractParameters)parameterDefine.getParameters();
							
							if(parameters2 == null)
								throw new InvalidParameterException("Cannot parse parameter value of '" + name + "'. parameters is null.");
							
							valuelize(parameters2.getParameterDefineMap(), st, CURLY_BRAKET_OPEN, null, null);
						} else {
							if(parameterDefine == null) {
								parameterDefine = new ParameterDefine(name, parameterValueType);
								parameterDefineMap.put(name, parameterDefine);
							}

							AbstractParameters parameters2 = (AbstractParameters)parameterDefine.getParameters();
							valuelize(parameters2.getParameterDefineMap(), st, CURLY_BRAKET_OPEN, null, null);
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
								throw new InvalidParameterException("Cannot parse value of '" + name + "' to an integer. \"" + token + "\"");
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
				throw new InvalidParameterException("Cannot parse value of '" + name + "' to an array of strings.");
			} else if(openBraket == SQUARE_BRAKET_OPEN) {
				throw new InvalidParameterException("Cannot parse value of '" + name + "' to an array of strings.");
			}
		}
	}
	
}
