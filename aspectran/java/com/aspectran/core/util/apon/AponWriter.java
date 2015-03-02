package com.aspectran.core.util.apon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.aspectran.core.context.builder.Importable;
import com.aspectran.core.context.builder.ImportableFile;
import com.aspectran.core.context.rule.type.ImportFileType;

public class AponWriter extends AponFormat {

	private Writer writer;

	private boolean prettyFormat;
	
	private int indentDepth;
	
	public AponWriter(Writer writer) {
		this(writer, true);
	}
	
	public AponWriter(Writer writer, boolean prettyFormat) {
		this.writer = writer;
		this.prettyFormat = prettyFormat;
	}
	
	public void write(Parameters parameters) throws IOException {
		Map<String, ParameterValue> parameterValueMap = parameters.getParameterValueMap();
		
		for(Parameter pv : parameterValueMap.values()) {
			if(pv.isAssigned()) {
				write(pv);
			}
		}
	}

	public void write(Parameter parameter) throws IOException {
		if(parameter.getParameterValueType() == ParameterValueType.PARAMETERS) {
			if(parameter.isArray()) {
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
			} else {
				writeName(parameter.getName());
				openCurlyBracket();
				write(parameter.getValueAsParameters());
				closeCurlyBracket();
			}
		} else if(parameter.getParameterValueType() == ParameterValueType.STRING || parameter.getParameterValueType() == ParameterValueType.VARIABLE) {
			if(parameter.isArray()) {
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
			} else {
				writeName(parameter.getName());
				writeString(parameter.getValueAsString());
			}
		} else if(parameter.getParameterValueType() == ParameterValueType.TEXT) {
			if(parameter.isArray()) {
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
			} else {
				writeName(parameter.getName());
				openRoundBracket();
				writeText(parameter.getValueAsString());
				closeRoundBracket();
			}
		} else {
			if(parameter.isArray()) {
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
			} else {
				writeName(parameter.getName());
				write(parameter.getValue());
			}
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
		indentPlus();
	}

	private void closeCurlyBracket() throws IOException {
		indentMinus();
		indent();
		writer.write(CURLY_BRACKET_CLOSE);
		nextLine();
	}

	private void openSquareBracket() throws IOException {
		writer.write(SQUARE_BRACKET_OPEN);
		nextLine();
		indentPlus();
	}

	private void closeSquareBracket() throws IOException {
		indentMinus();
		indent();
		writer.write(SQUARE_BRACKET_CLOSE);
		nextLine();
	}

	private void openRoundBracket() throws IOException {
		writer.write(ROUND_BRACKET_OPEN);
		nextLine();
		indentPlus();
	}
	
	private void closeRoundBracket() throws IOException {
		indentMinus();
		indent();
		writer.write(ROUND_BRACKET_CLOSE);
		nextLine();
	}
	
	private void nextLine() throws IOException {
		writer.write(NEXT_LINE_CHAR);
	}
	
	private void indent() throws IOException {
		if(prettyFormat) {
			for(int i = 0; i < indentDepth; i++) {
				writer.write(INDENT_CHAR);
			}
		}
	}
	
	private void indentPlus() throws IOException {
		if(prettyFormat) {
			indentDepth++;
		}
	}
	
	private void indentMinus() throws IOException {
		if(prettyFormat) {
			indentDepth--;
		}
	}
	
	public void flush() throws IOException {
		writer.flush();
	}
	
	public static void main(String argv[]) {
		try {
			Importable importable = new ImportableFile("/c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/config/aspectran/sample/sample-test.apon", ImportFileType.APON);
			AponReader aponReader = new AponReader();
			Parameters parameters = aponReader.read(importable.getReader());
			
			StringWriter writer = new StringWriter();
			
			AponWriter aponWriter = new AponWriter(writer);
			aponWriter.write(parameters);
			aponWriter.flush();
			
			System.out.print(writer.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
