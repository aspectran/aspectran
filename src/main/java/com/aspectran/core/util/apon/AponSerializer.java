/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.util.apon;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Converts a Parameters object to an APON formatted string.
 * If pretty-printing is enabled, includes spaces, tabs to make the format more readable.
 * The new-lines character is always present.
 * Pretty-printing is enabled by default.
 * The default indentation string is a tab character.
 */
public class AponSerializer extends AponFormat implements Closeable, Flushable {

	private Writer writer;

	private boolean prettyPrint;
	
	private String indentString;

	private int indentDepth;
	
	/**
	 * Instantiates a new AponSerializer.
	 * Pretty-printing is enabled by default.
	 *
	 * @param writer the character-output stream
	 */
	public AponSerializer(Writer writer) {
		this(writer, true);
	}
	
	/**
	 * Instantiates a new AponSerializer.
	 * The default indentation string is a tab character.
	 *
	 * @param writer the character-output stream
	 * @param prettyPrint enables or disables pretty-printing.
	 */
	public AponSerializer(Writer writer, boolean prettyPrint) {
		this(writer, prettyPrint, AponFormat.INDENT_STRING);
	}
	
	/**
	 * Instantiates a new AponSerializer.
	 *
	 * @param writer the character-output stream
	 * @param prettyPrint enables or disables pretty-printing.
	 * @param indentString the string that should be used for indentation when pretty-printing is enabled.
	 */
	public AponSerializer(Writer writer, boolean prettyPrint, String indentString) {
		this.writer = writer;
		this.prettyPrint = prettyPrint;
		this.indentString = indentString;
	}
	
	/**
	 * Write a Parameters object to the character-output stream.
	 *
	 * @param parameters the parameters object
	 * @throws IOException An I/O error occurs.
	 */
	public void write(Parameters parameters) throws IOException {
		Map<String, ParameterValue> parameterValueMap = parameters.getParameterValueMap();
		
		for(Parameter pv : parameterValueMap.values()) {
			if(pv.isAssigned()) {
				write(pv);
			}
		}
	}

	/**
	 * Write a Parameter object to the character-output stream.
	 *
	 * @param parameter the parameter object
	 * @throws IOException An I/O error occurs.
	 */
	public void write(Parameter parameter) throws IOException {
		if(parameter.getParameterValueType() == ParameterValueType.PARAMETERS) {
			if(parameter.isArray()) {
				if(parameter.getValueAsParametersList() != null) {
					if(parameter.isBracketed()) {
						writeName(parameter.getName());
						openSquareBracket();
						for(Parameters p : parameter.getValueAsParametersList()) {
							indent();
							openCurlyBracket();
							write(p);
							closeCurlyBracket();
						}
						closeSquareBracket();
					} else {
						for(Parameters p : parameter.getValueAsParametersList()) {
							writeName(parameter.getName());
							openCurlyBracket();
							write(p);
							closeCurlyBracket();
						}
					}
				}
			} else {
				if(parameter.getValueAsParameters() != null) {
					writeName(parameter.getName());
					openCurlyBracket();
					write(parameter.getValueAsParameters());
					closeCurlyBracket();
				}
			}
		} else if(parameter.getParameterValueType() == ParameterValueType.STRING || parameter.getParameterValueType() == ParameterValueType.VARIABLE) {
			if(parameter.isArray()) {
				if(parameter.getValueAsStringList() != null) {
					if(parameter.isBracketed()) {
						writeName(parameter.getName());
						openSquareBracket();
						for(String value : parameter.getValueAsStringList()) {
							indent();
							writeString(value);
						}
						closeSquareBracket();
					} else {
						for(String value : parameter.getValueAsStringList()) {
							writeName(parameter.getName());
							writeString(value);
						}
					}
				}
			} else {
				if(parameter.getValueAsString() != null) {
					writeName(parameter.getName());
					writeString(parameter.getValueAsString());
				}
			}
		} else if(parameter.getParameterValueType() == ParameterValueType.TEXT) {
			if(parameter.isArray()) {
				if(parameter.getValueAsStringList() != null) {
					if(parameter.isBracketed()) {
						writeName(parameter.getName());
						openSquareBracket();
						for(String value : parameter.getValueAsStringList()) {
							indent();
							openRoundBracket();
							writeText(value);
							closeRoundBracket();
						}
						closeSquareBracket();
					} else {
						for(String value : parameter.getValueAsStringList()) {
							writeName(parameter.getName());
							openRoundBracket();
							writeText(value);
							closeRoundBracket();
						}
					}
				}
			} else {
				if(parameter.getValueAsString() != null) {
					writeName(parameter.getName());
					openRoundBracket();
					writeText(parameter.getValueAsString());
					closeRoundBracket();
				}
			}
		} else {
			if(parameter.isArray()) {
				if(parameter.getValueList() != null) {
					if(parameter.isBracketed()) {
						writeName(parameter.getName());
						openSquareBracket();
						for(Object value : parameter.getValueList()) {
							indent();
							write(value);
						}
						closeSquareBracket();
					} else {
						for(Object value : parameter.getValueList()) {
							writeName(parameter.getName());
							write(value);
						}
					}
				}
			} else {
				if(parameter.getValue() != null) {
					writeName(parameter.getName());
					write(parameter.getValue());
				}
			}
		}
	}
	
	/**
	 * Writes a comment to the character-output stream.
	 * 
	 * @param describe the comment to write to a character-output stream.
	 * 
	 * @throws IOException An I/O error occurs.
	 */
	public void comment(String describe) throws IOException {
		if(describe.indexOf(AponFormat.NEXT_LINE_CHAR) != -1) {
			Reader reader = new StringReader(describe);
			BufferedReader br = new BufferedReader(reader);
			String line;
			while((line = br.readLine()) != null) {
				writer.write(AponFormat.COMMENT_LINE_START);
				writer.write(AponFormat.SPACE_CHAR);
				writer.write(line);
				nextLine();
			}
			reader.close();
		} else {
			writer.write(AponFormat.COMMENT_LINE_START);
			writer.write(AponFormat.SPACE_CHAR);
			writer.write(describe);
			nextLine();
		}
	}
	
	private void writeName(String name) throws IOException {
		indent();
		writer.write(name);
		writer.write(NAME_VALUE_SEPARATOR);
		writer.write(SPACE_CHAR);
	}
	
	private void writeString(String value) throws IOException {
		writer.write(QUOTE_CHAR);
		writer.write(escape(value));
		writer.write(QUOTE_CHAR);
		nextLine();
	}
	
	private void writeText(String value) throws IOException {
		Reader r = new StringReader(value);
		BufferedReader br = new BufferedReader(r);
		String line;
		
		while((line = br.readLine()) != null) {
			indent();
			writer.write(TEXT_LINE_START);
			writer.write(line);
			nextLine();
		}
	}
	
	private void write(Object value) throws IOException {
		if(value == null) {
			writer.write(NULL);
		} else {
			writer.write(value.toString());
		}
		
		nextLine();
	}
	
	private void openCurlyBracket() throws IOException {
		writer.write(CURLY_BRACKET_OPEN);
		nextLine();
		increaseIndent();
	}

	private void closeCurlyBracket() throws IOException {
		decreaseIndent();
		indent();
		writer.write(CURLY_BRACKET_CLOSE);
		nextLine();
	}

	private void openSquareBracket() throws IOException {
		writer.write(SQUARE_BRACKET_OPEN);
		nextLine();
		increaseIndent();
	}

	private void closeSquareBracket() throws IOException {
		decreaseIndent();
		indent();
		writer.write(SQUARE_BRACKET_CLOSE);
		nextLine();
	}

	private void openRoundBracket() throws IOException {
		writer.write(ROUND_BRACKET_OPEN);
		nextLine();
		increaseIndent();
	}
	
	private void closeRoundBracket() throws IOException {
		decreaseIndent();
		indent();
		writer.write(ROUND_BRACKET_CLOSE);
		nextLine();
	}
	
	private void nextLine() throws IOException {
		writer.write(NEXT_LINE_CHAR);
	}
	
	private void indent() throws IOException {
		if(prettyPrint) {
			for(int i = 0; i < indentDepth; i++) {
				writer.write(indentString);
			}
		}
	}
	
	private void increaseIndent() throws IOException {
		if(prettyPrint) {
			indentDepth++;
		}
	}
	
	private void decreaseIndent() throws IOException {
		if(prettyPrint) {
			indentDepth--;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.io.Flushable#flush()
	 */
	public void flush() throws IOException {
		writer.flush();
	}
	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		if(writer != null)
			writer.close();
		
		writer = null;
	}
	
	/**
	 * Converts a Parameters object to an APON formatted string.
	 *
	 * @param parameters the parameters object
	 * @return the APON formatted string
	 */
	public static String serialize(Parameters parameters) {
		if(parameters == null)
			return null;
		
		try {
			Writer writer = new StringWriter();
			AponSerializer serializer = new AponSerializer(writer);
			serializer.write(parameters);
			serializer.close();
			
			return writer.toString();
		} catch(IOException e) {
			throw new AponWriteFailedException("Cannot convert to an APON formatted string.", e);
		}
	}
	
}
