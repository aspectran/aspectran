package com.aspectran.core.var.apon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aspectran.core.util.StringUtils;

public class AponReader {

	protected static final String CURLY_BRAKET_OPEN = "{";

	protected static final String CURLY_BRAKET_CLOSE = "}";
	
	protected static final String SQUARE_BRAKET_OPEN = "[";
	
	protected static final String SQUARE_BRAKET_CLOSE = "]";

	//private static final String DELIMITERS = "\n\r\f";
	
	//private Map<String, ParameterDefine> parameterDefineMap;
	
	private Parameters holder;
	
	//private boolean preparsed;
	
	private boolean addable;
	
	public AponReader() {
	}

	public AponReader(Parameters holder) {
		this.holder = holder;
	}

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
		read(text, parameters.getParameterDefines());
		return parameters;
	}

	public Map<String, ParameterDefine> read(String text, ParameterDefine[] parameterDefines) {
		try {
			BufferedReader reader = new BufferedReader(new StringReader(text));
			Map<String, ParameterDefine> parameterDefineMap = read(reader, parameterDefines);
			reader.close();
	
			return parameterDefineMap;
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
	public Parameters read(BufferedReader reader) throws IOException {
		Parameters parameters = new GenericParameters();
		read(reader, parameters);
		return parameters;
	}
	
	public Parameters read(BufferedReader reader, Parameters parameters) throws IOException {
		read(reader, parameters.getParameterDefines());
		return parameters;
	}
	
	public Map<String, ParameterDefine> read(BufferedReader reader, ParameterDefine[] parameterDefines) throws IOException {
		Map<String, ParameterDefine> parameterDefineMap = new LinkedHashMap<String, ParameterDefine>();
		
		if(parameterDefines != null) {
			for(ParameterDefine pd : parameterDefines) {
				if(holder != null)
					pd.setHolder(holder);
				
				parameterDefineMap.put(pd.getName(), pd);
			}
			
			addable = false;
		} else {
			addable = true;
		}

		valuelize(reader, parameterDefineMap);
		
		return parameterDefineMap;
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
	private void valuelize(BufferedReader reader, Map<String, ParameterDefine> parameterDefineMap) throws IOException {
		valuelize(reader, parameterDefineMap, null, null, null);
	}
	
	private void valuelize(BufferedReader reader, Map<String, ParameterDefine> parameterDefineMap, String openBraket, String name, ParameterDefine parameterDefine) throws IOException {
		String buffer = null;
		String value = null;
		ParameterValueType parameterValueType = null;
		
		while((buffer = reader.readLine()) != null) {
			
			if(StringUtils.hasText(buffer)) {
				buffer = buffer.trim();
				
				if(openBraket != null) {
					if(openBraket == CURLY_BRAKET_OPEN && CURLY_BRAKET_CLOSE.equals(buffer) ||
							openBraket == SQUARE_BRAKET_OPEN && SQUARE_BRAKET_CLOSE.equals(buffer)) {
						//System.out.println("*****return********* openBraket: " + openBraket + ", token: " + token);
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
							throw new InvalidParameterException("invalid parameter \"" + buffer + "\"");
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
							valuelize(reader, parameterDefineMap, SQUARE_BRAKET_OPEN, name, parameterDefine);
							continue;
						}
					}
					
					if(parameterValueType == ParameterValueType.PARAMETERS) {
						if(openBraket == SQUARE_BRAKET_OPEN) {
							if(parameterDefine == null) {
								parameterDefine = new ParameterDefine(name, parameterValueType, true);
								parameterDefineMap.put(name, parameterDefine);
							}

							AbstractParameters parameters2 = (AbstractParameters)parameterDefine.newParameters();
							valuelize(reader, parameters2.getParameterDefineMap(), CURLY_BRAKET_OPEN, null, null);
							parameterDefine.putValue(parameters2);
						} else {
							if(parameterDefine == null) {
								parameterDefine = new ParameterDefine(name, parameterValueType);
								parameterDefineMap.put(name, parameterDefine);
							}

							AbstractParameters parameters2 = (AbstractParameters)parameterDefine.getValueAsParameters();
							valuelize(reader, parameters2.getParameterDefineMap(), CURLY_BRAKET_OPEN, null, null);
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
								parameterDefine.putValue(new Integer(value));
							} catch(NumberFormatException ex) {
								throw new InvalidParameterException("Cannot parse value of '" + name + "' to an integer. \"" + buffer + "\"");
							}
						} else if(parameterValueType == ParameterValueType.BOOLEAN) {
							parameterDefine.putValue(Boolean.valueOf(value));
						} else {
							parameterDefine.putValue(value);
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
	
}
