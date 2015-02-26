package com.aspectran.core.util.apon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import com.aspectran.core.context.builder.Importable;
import com.aspectran.core.context.builder.ImportableFile;
import com.aspectran.core.context.rule.type.ImportFileType;

public class AponReader {

	private static final char CURLY_BRACKET_OPEN = '{';

	private static final char CURLY_BRACKET_CLOSE = '}';
	
	private static final char SQUARE_BRACKET_OPEN = '[';
	
	private static final char SQUARE_BRACKET_CLOSE = ']';

	private static final char ROUND_BRACKET_OPEN = '(';
	
	private static final char ROUND_BRACKET_CLOSE = ')';
	
	private static final char TEXT_LINE_START = '|';
		
	private static final char NAME_VALUE_SEPARATOR = ':';
	
	private static final char COMMENT_LINE_START = '#';
	
	private static final char NO_CONTROL_CHAR = 0;
	
	private static final char QUOTE_CHAR = '"';
	
	private static final char ESCAPE_CHAR = '\\';
	
	private static final String NULL = "null";
	
	private static final String TRUE = "true";
	
	private static final String FALSE = "false";
	
	private boolean addable;
	
	public AponReader() {
	}

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

	private void valuelize(BufferedReader reader, Map<String, ParameterValue> parameterValueMap) throws IOException {
		valuelize(parameterValueMap, reader, 0, NO_CONTROL_CHAR, null, null, null);
	}
	
	/**
	 * @param parameterValueMap
	 * @param reader
	 * @param lineNumber
	 * @param openBracket
	 * @param name
	 * @param parameterValue
	 * @param parameterValueType
	 * @return
	 * @throws IOException
	 */
	private int valuelize(Map<String, ParameterValue> parameterValueMap, BufferedReader reader, int lineNumber, char openBracket, String name, ParameterValue parameterValue, ParameterValueType parameterValueType) throws IOException {
		String line;
		String value;
		String trim;
		int tlen;
		int vlen;
		char cchar;
		
		while((line = reader.readLine()) != null) {
			lineNumber++;
			trim = line.trim();
			tlen = trim.length();
			
			System.out.println("[" + lineNumber + "] " + line);

			if(tlen == 0 || trim.charAt(0) == COMMENT_LINE_START)
				continue;

			if(openBracket == SQUARE_BRACKET_OPEN) {
				value = trim;
				vlen = value.length();
				cchar = (vlen == 1) ? value.charAt(0) : NO_CONTROL_CHAR;
				
				if(SQUARE_BRACKET_CLOSE == cchar)
					return lineNumber;
			} else {
				if(tlen == 1) {
					if(openBracket == CURLY_BRACKET_OPEN && CURLY_BRACKET_CLOSE == trim.charAt(0)) {
						return lineNumber;
					}
				}

				int index = trim.indexOf(NAME_VALUE_SEPARATOR);
				if(index == -1)
					throw new InvalidParameterException(lineNumber, line, trim, "Cannot parse into name-value pair.");
				
				if(index == 0)
					throw new InvalidParameterException(lineNumber, line, trim, "Cannot find parameter name.");
				
				name = trim.substring(0, index).trim();
				value = trim.substring(index + 1).trim();
				vlen = value.length();
				cchar = (vlen == 1) ? value.charAt(0) : NO_CONTROL_CHAR;
				
				parameterValue = parameterValueMap.get(name);

				if(parameterValue != null) {
					parameterValueType = parameterValue.getParameterValueType();
				} else {
					if(!addable)
						throw new InvalidParameterException(lineNumber, line, trim, "Only acceptable pre-defined parameters. Undefined parameter name: " + name);

					parameterValueType = ParameterValueType.valueOfHint(name);
					if(parameterValueType != null) {
						name = ParameterValueType.stripValueTypeHint(name);
						parameterValue = parameterValueMap.get(name);
						if(parameterValue != null)
							parameterValueType = parameterValue.getParameterValueType();
					}
					//System.out.println(lineNumber + " - valueOfHint: " + parameterValueType);
					
				}
				
				if(parameterValueType == ParameterValueType.VARIABLE)
					parameterValueType = null;
				
				if(parameterValueType != null) {
					if(parameterValue != null && !parameterValue.isArray() && SQUARE_BRACKET_OPEN == cchar)
						throw new IncompatibleParameterValueTypeException(lineNumber, line, trim, "Parameter value is not array type.");
					if(parameterValueType != ParameterValueType.PARAMETERS && CURLY_BRACKET_OPEN == cchar)
						throw new IncompatibleParameterValueTypeException(lineNumber, line, trim, parameterValue, parameterValueType);
					if(parameterValueType != ParameterValueType.TEXT && ROUND_BRACKET_OPEN == cchar)
						throw new IncompatibleParameterValueTypeException(lineNumber, line, trim, parameterValue, parameterValueType);
				}
			}
			
			if(parameterValue != null && !parameterValue.isArray()) {
				if(parameterValueType == ParameterValueType.PARAMETERS && CURLY_BRACKET_OPEN != cchar)
					throw new IncompatibleParameterValueTypeException(lineNumber, line, trim, parameterValue, parameterValueType);
				if(parameterValueType == ParameterValueType.TEXT && ROUND_BRACKET_OPEN != cchar)
					throw new IncompatibleParameterValueTypeException(lineNumber, line, trim, parameterValue, parameterValueType);
			}
				
			if(parameterValue == null || (parameterValue != null && parameterValue.isArray())) {
				if(SQUARE_BRACKET_OPEN == cchar) {
					//System.out.println("1**************[ name: " + name);
					//System.out.println("1**************[ parameterValue: " + parameterValue);
					lineNumber = valuelize(parameterValueMap, reader, lineNumber, SQUARE_BRACKET_OPEN, name, parameterValue, parameterValueType);
					continue;
				}
			}

			//System.out.println(lineNumber + " - 01************** parameterValueType: " + parameterValueType);
			
			//if(StringUtils.hasText(value)) {
			if(parameterValueType == null) {
				if(CURLY_BRACKET_OPEN == cchar) {
					parameterValueType = ParameterValueType.PARAMETERS;
				} else if(ROUND_BRACKET_OPEN == cchar) {
					parameterValueType = ParameterValueType.TEXT;
				}
			}
			
			//System.out.println(lineNumber + " - 02************** parameterValueType: " + parameterValueType);

			if(parameterValueType == ParameterValueType.PARAMETERS) {
				//System.out.println("03************** parameterValue: " + parameterValue);
				if(parameterValue == null) {
					parameterValue = new ParameterValue(name, parameterValueType, (openBracket == SQUARE_BRACKET_OPEN));
					parameterValueMap.put(name, parameterValue);
				}
				//System.out.println("04************** parameterValue: " + parameterValue);

				Parameters parameters2 = parameterValue.newParameters();
				lineNumber = valuelize(parameters2.getParameterValueMap(), reader, lineNumber, CURLY_BRACKET_OPEN, null, null, null);
			} else if(parameterValueType == ParameterValueType.TEXT) {
				if(parameterValue == null) {
					parameterValue = new ParameterValue(name, parameterValueType, (openBracket == SQUARE_BRACKET_OPEN));
					parameterValueMap.put(name, parameterValue);
				}

				StringBuilder sb = new StringBuilder();
				lineNumber = valuelizeText(reader, lineNumber, sb);
				parameterValue.putValue(sb.toString());
			} else {
				if(vlen == 0) {
					value = null;
					
					if(parameterValueType == null)
						parameterValueType = ParameterValueType.STRING;
				} else {
					if(value.charAt(0) == QUOTE_CHAR) {
						if(vlen == 1 || value.charAt(vlen - 1) != QUOTE_CHAR)
							throw new InvalidParameterException(lineNumber, line, trim, "Unclosed quotation mark.");						
							
						value = value.substring(1, vlen - 1);
						
						if(parameterValueType == null)
							parameterValueType = ParameterValueType.STRING;
					} else if(parameterValueType == null) {
						if(NULL.equals(value)) {
							value = null;
						} else if(TRUE.equals(value) || FALSE.equals(value)) {
							parameterValueType = ParameterValueType.BOOLEAN;
						} else {
							try {
								Integer.parseInt(value);
								parameterValueType = ParameterValueType.INT;
							} catch(NumberFormatException e1) {
								try {
									Long.parseLong(value);
									parameterValueType = ParameterValueType.LONG;
								} catch(NumberFormatException e2) {
									try {
										Float.parseFloat(value);
										parameterValueType = ParameterValueType.FLOAT;
									} catch(NumberFormatException e3) {
										try {
											Double.parseDouble(value);
											parameterValueType = ParameterValueType.DOUBLE;
										} catch(NumberFormatException e4) {
											throw new InvalidParameterException(lineNumber, line, trim, "Unknown value type. Strings must be enclosed between double quotation marks.");
										}
									}
								}
							}
						}
					}
				}
				
				if(parameterValue == null) {
					parameterValue = new ParameterValue(name, parameterValueType, (openBracket == SQUARE_BRACKET_OPEN));
					parameterValueMap.put(name, parameterValue);
				} else {
					if(parameterValue.getParameterValueType() != parameterValueType)
						throw new IncompatibleParameterValueTypeException(lineNumber, line, trim, parameterValue, parameterValue.getParameterValueType());						
				}
				
				if(parameterValueType == ParameterValueType.STRING) {
					parameterValue.putValue(unescape(value));
				} else if(parameterValueType == ParameterValueType.INT) {
					parameterValue.putValue(new Integer(value));
				} else if(parameterValueType == ParameterValueType.LONG) {
					parameterValue.putValue(new Long(value));
				} else if(parameterValueType == ParameterValueType.FLOAT) {
					parameterValue.putValue(new Float(value));
				} else if(parameterValueType == ParameterValueType.DOUBLE) {
					parameterValue.putValue(new Double(value));
				} else if(parameterValueType == ParameterValueType.BOOLEAN) {
					parameterValue.putValue(Boolean.valueOf(value));
				}
			}
		}
		
		if(openBracket == CURLY_BRACKET_OPEN) {
			throw new MissingClosingBracketException("curly", name, parameterValue);
		} else if(openBracket == SQUARE_BRACKET_OPEN) {
			throw new MissingClosingBracketException("square", name, parameterValue);
		}
		
		return lineNumber;
	}
	
	private int valuelizeText(BufferedReader reader, int lineNumber, StringBuilder sb) throws IOException {
		String line;
		String trim = null;
		int tlen;
		char tchar;
		
		while((line = reader.readLine()) != null) {
			lineNumber++;
			trim = line.trim();
			tlen = trim.length();
			tchar = tlen > 0 ? trim.charAt(0) : NO_CONTROL_CHAR;
			
			if(tlen == 1 && ROUND_BRACKET_CLOSE == tchar)
				return lineNumber;
				
			if(TEXT_LINE_START == tchar) {
				String value = line.substring(line.indexOf(TEXT_LINE_START) + 1);
				sb.append(value);
			} else if(tlen > 0) {
				throw new InvalidParameterException(lineNumber, line, trim, "The closing round bracket was missing or Each text line is must start with a ';' character.");
			}
		}
		
		throw new InvalidParameterException(lineNumber, line, trim, "The end of the text line was reached with no closing round bracket found.");
	}
	
	private String unescape(String value) {
		int vlen = value.length();

		if(value == null || vlen == 0 || value.indexOf(ESCAPE_CHAR) == -1)
			return value;

		char b = value.charAt(0);
		char c = 0;

		StringBuilder sb = new StringBuilder(vlen);

		for(int i = 1; i < vlen; i++) {
			c = value.charAt(i);
			
			if(b == ESCAPE_CHAR) {
				switch(c) {
				case ESCAPE_CHAR:
				case QUOTE_CHAR:
					sb.append(c);
					break;
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'r':
					sb.append('\r');
					break;
				default:
					sb.append(b);
				}
			} else {
				sb.append(b);
			}
			
			b = c;
		}
		
		if(c != 0)
			sb.append(c);
		
		return sb.toString();
	}
	
	public static void main(String argv[]) {
		try {
			Importable importable = new ImportableFile("/c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/config/aspectran/sample/sample-test.apon", ImportFileType.APON);
			AponReader aponReader = new AponReader();
			aponReader.read(importable.getReader());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
}
