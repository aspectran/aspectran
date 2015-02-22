package com.aspectran.core.util.apon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import com.aspectran.core.util.StringUtils;

public class AponReader {

	protected static final String CURLY_BRAKET_OPEN = "{";

	protected static final String CURLY_BRAKET_CLOSE = "}";
	
	protected static final String SQUARE_BRAKET_OPEN = "[";
	
	protected static final String SQUARE_BRAKET_CLOSE = "]";

	//private static final String DELIMITERS = "\n\r\f";
	
	//private Map<String, ParameterDefine> parameterDefineMap;
	
	//private Parameters holder;
	
	//private boolean preparsed;
	
	private boolean addable;
	
	public AponReader() {
	}
	/*
	public AponReader(Parameters holder) {
		this.holder = holder;
	}
	*/
	/*
	public void read(String text) throws IOException {
		read(text, null);
	}
	public void read(String text, ParameterDefine[] parameterDefines) throws IOException {
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
	*/

	public Parameters read(String text, Parameters parameters) {
		read(text, parameters.getParameterValueMap());
		return parameters;
	}

	public Map<String, ParameterValue> read(String text, Map<String, ParameterValue> parameterValueMap) {
		try {
			Reader reader = new StringReader(text);
			read(reader, parameterValueMap);
			reader.close();
	
			return parameterValueMap;
		} catch(IOException e) {
			throw new AponReadFailedException(e);
		}
	}
/*
	public Parameters read(InputStream inputStream, Parameters parameters) {
		read(inputStream, parameters.getParameterDefines());
		return parameters;
	}

	public Map<String, ParameterDefine> read(InputStream inputStream, ParameterDefine[] parameterDefines) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			Map<String, ParameterDefine> parameterDefineMap = read(reader, parameterDefines);
			reader.close();
	
			return parameterDefineMap;
		} catch(IOException e) {
			throw new AponReadFailedException(e);
		}
	}
*/
	public Parameters read(Reader reader) throws IOException {
		Parameters parameters = new GenericParameters();
		read(reader, parameters);
		return parameters;
	}
	
	public Parameters read(Reader reader, Parameters parameters) throws IOException {
		read(reader, parameters.getParameterValueMap());
		return parameters;
	}
	
	public Map<String, ParameterValue> read(Reader reader, Map<String, ParameterValue> parameterValueMap) throws IOException {
		if(parameterValueMap != null && !parameterValueMap.isEmpty()) {
			addable = false;
		} else {
			addable = true;
		}

		BufferedReader br = new BufferedReader(reader);
		valuelize(br, parameterValueMap);
		
		return parameterValueMap;
	}
	
	/*
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
	private ParameterDefine[] preparse(String text) throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(text));
		ParameterDefine[] parameterDefines = preparse(reader, null);
		reader.close();
		
		preparsed = true;
		
		return parameterDefines;
	}
	
	private ParameterDefine[] preparse(BufferedReader reader, ParameterDefine parentParameterDefine) throws IOException {
		List<ParameterDefine> parameterDefineList = new ArrayList<ParameterDefine>();
		
		String openBraket = parentParameterDefine != null ? CURLY_BRAKET_OPEN : null;
		
		preparse(reader, parameterDefineList, openBraket, null);
		
		ParameterDefine[] parameterDefines = parameterDefineList.toArray(new ParameterDefine[parameterDefineList.size()]);
		
		if(parentParameterDefine != null) {
			Parameters parameters = new GenericParameters(parentParameterDefine.getName(), parameterDefines);
			parentParameterDefine.putValue(parameters);
		}
		
		return parameterDefines;
	}
	
	private void preparse(BufferedReader reader, List<ParameterDefine> parameterDefineList, String openBraket, ParameterDefine parameterDefine) throws IOException {
		String buffer = null;
		String name = null;
		String value = null;
		
		while((buffer = reader.readLine()) != null) {
			if(StringUtils.hasText(buffer)) {
				buffer = buffer.trim();

				if(openBraket != null) {
					if(openBraket == CURLY_BRAKET_OPEN && CURLY_BRAKET_CLOSE.equals(buffer) ||
							openBraket == SQUARE_BRAKET_OPEN && SQUARE_BRAKET_CLOSE.equals(buffer))
						return;
				}
				
				if(openBraket == SQUARE_BRAKET_OPEN) {
					value = buffer;
				} else {
					int index = buffer.indexOf(":");

					if(index == -1)
						throw new InvalidParameterException("Cannot parse into name-value pair. \"" + buffer + "\"");

					name = buffer.substring(0, index).trim();
					value = buffer.substring(index + 1).trim();
				}

				if(StringUtils.hasText(value)) {
					if(CURLY_BRAKET_OPEN.equals(value)) {
						if(openBraket == SQUARE_BRAKET_OPEN) {
							preparse(reader, parameterDefine);
						} else {
							ParameterDefine pd = new ParameterDefine(name, ParameterValueType.PARAMETERS);
							parameterDefineList.add(pd);
							preparse(reader, pd);
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
	
							preparse(reader, parameterDefineList, SQUARE_BRAKET_OPEN, pd);
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
	*/
	private void valuelize(BufferedReader reader, Map<String, ParameterValue> parameterValueMap) throws IOException {
		valuelize(reader, parameterValueMap, null, null, null);
	}
	
	private void valuelize(BufferedReader reader, Map<String, ParameterValue> parameterValueMap, String openBraket, String name, ParameterValue parameterValue) throws IOException {
		String buffer = null;
		String value = null;
		ParameterValueType parameterValueType = null;
		
		while((buffer = reader.readLine()) != null) {
			if(StringUtils.hasText(buffer)) {
				buffer = buffer.trim();
				
				if(openBraket != null) {
					if(openBraket == CURLY_BRAKET_OPEN && CURLY_BRAKET_CLOSE.equals(buffer) ||
							openBraket == SQUARE_BRAKET_OPEN && SQUARE_BRAKET_CLOSE.equals(buffer)) {
						//System.out.println("*****return********* openBraket: " + openBraket + ", token: " + buffer);
						return;
					}
				}
				
				if(openBraket == SQUARE_BRAKET_OPEN) {
					value = buffer;
				} else {
					int index = buffer.indexOf(":");
					
					if(index == -1)
						throw new InvalidParameterException("Cannot parse into name-value pair. \"" + buffer + "\"");
					
					name = buffer.substring(0, index).trim();
					value = buffer.substring(index + 1).trim();
					
					parameterValue = parameterValueMap.get(name);

					//System.out.println("************** title: " + title);
					//System.out.println("0************** name: " + name + ", value: " + value + ", buffer: " + buffer);
					//System.out.println("0************** parameterValue: " + parameterValue);
					
					if(parameterValue == null) {
						if(addable) {
							parameterValueType = ParameterValueType.valueOfHint(name);
							if(parameterValueType != null) {
								name = ParameterValueType.stripValueTypeHint(name);
								parameterValue = parameterValueMap.get(name);
							}
						} else {
							throw new InvalidParameterException("invalid parameter \"" + buffer + "\"");
						}
					}
				}

				if(parameterValue != null && parameterValue.getParameterValueType() != ParameterValueType.VARIABLE) {
					parameterValueType = parameterValue.getParameterValueType();
				} else {
					parameterValueType = null;
				}
				
				//System.out.println("01************** parameterValueType: " + parameterValueType);
				
				if(StringUtils.hasText(value)) {
					if(parameterValueType == null && CURLY_BRAKET_OPEN.equals(value)) {
						parameterValueType = ParameterValueType.PARAMETERS;
						//System.out.println("02************** parameterValueType: " + parameterValueType);
					} else if(SQUARE_BRAKET_OPEN.equals(value)) {
						if(parameterValue == null || (parameterValue != null && parameterValue.isArray())) {
							//System.out.println("1**************[ name: " + name);
							//System.out.println("1**************[ parameterValue: " + parameterValue);
							valuelize(reader, parameterValueMap, SQUARE_BRAKET_OPEN, name, parameterValue);
							continue;
						}
					}
					
					if(parameterValueType == ParameterValueType.PARAMETERS) {
						//System.out.println("03************** parameterValue: " + parameterValue);
						if(parameterValue == null) {
							parameterValue = new ParameterValue(name, parameterValueType, (openBraket == SQUARE_BRAKET_OPEN));
							parameterValueMap.put(name, parameterValue);
						}
						//System.out.println("04************** parameterValue: " + parameterValue);

						Parameters parameters2 = parameterValue.newParameters();
						//System.out.println("05************** parameters2: " + parameters2);
						//System.out.println("new************** parameterValue.newParameters(): " + parameterValue);
						valuelize(reader, parameters2.getParameterValueMap(), CURLY_BRAKET_OPEN, null, null);
						parameterValue.putValue(parameters2);

//							AbstractParameters parameters2 = (AbstractParameters)parameterValue.touchValueAsParameters();
//							System.out.println("************** parameterValue.touchValueAsParameters(): " + parameterValue);
//							valuelize(reader, parameters2.getParameterValueMap(), CURLY_BRAKET_OPEN, null, null);
					} else {
						if(parameterValueType == null)
							parameterValueType = ParameterValueType.STRING;

						if(parameterValue == null) {
							parameterValue = new ParameterValue(name, parameterValueType, (openBraket == SQUARE_BRAKET_OPEN));
							parameterValueMap.put(name, parameterValue);
						}

						if(parameterValueType == ParameterValueType.INTEGER) {
							try {
								parameterValue.putValue(new Integer(value));
							} catch(NumberFormatException ex) {
								throw new IncompatibleParameterValueTypeException(parameterValue, ParameterValueType.INTEGER);
								//throw new InvalidParameterException("Cannot parse value of '" + name + "' to an Integer. \"" + buffer + "\"");
							}
						} else if(parameterValueType == ParameterValueType.LONG) {
							try {
								parameterValue.putValue(new Long(value));
							} catch(NumberFormatException ex) {
								throw new IncompatibleParameterValueTypeException(parameterValue, ParameterValueType.LONG);
								//throw new InvalidParameterException("Cannot parse value of '" + name + "' to an Long. \"" + buffer + "\"");
							}
						} else if(parameterValueType == ParameterValueType.FLOAT) {
							try {
								parameterValue.putValue(new Float(value));
							} catch(NumberFormatException ex) {
								throw new IncompatibleParameterValueTypeException(parameterValue, ParameterValueType.FLOAT);
								//throw new InvalidParameterException("Cannot parse value of '" + name + "' to an Float. \"" + buffer + "\"");
							}
						} else if(parameterValueType == ParameterValueType.DOUBLE) {
							try {
								parameterValue.putValue(new Double(value));
							} catch(NumberFormatException ex) {
								throw new IncompatibleParameterValueTypeException(parameterValue, ParameterValueType.DOUBLE);
								//throw new InvalidParameterException("Cannot parse value of '" + name + "' to an Double. \"" + buffer + "\"");
							}
						} else if(parameterValueType == ParameterValueType.BOOLEAN) {
							parameterValue.putValue(Boolean.valueOf(value));
						} else {
							parameterValue.putValue(value);
						}
						
						//System.out.println("val************ parameterValue.putValue(): name=" + name + ", value=" + value);
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
	
}
