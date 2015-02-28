package com.aspectran.core.util.apon;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.aspectran.core.context.builder.Importable;
import com.aspectran.core.context.builder.ImportableFile;
import com.aspectran.core.context.rule.type.ImportFileType;

public class AponWriter extends AponFormat {

	private Writer writer;

	private boolean prettyWrite;
	
	private int indentDepth;
	
	public AponWriter(Writer writer) {
		this(writer, true);
	}
	
	public AponWriter(Writer writer, boolean prettyWrite) {
		this.writer = writer;
		this.prettyWrite = prettyWrite;
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
		writeName(parameter.getName());
		
		if(parameter.getParameterValueType() == ParameterValueType.PARAMETERS) {
			if(parameter.isArray()) {
				openSquareBracket();
				for(Parameters p : parameter.getValueAsParametersList()) {
					indent();
					openCurlyBracket();
					write(p);
					closeCurlyBracket();
				}
				closeSquareBracket();
			} else {
				openCurlyBracket();
				write(parameter.getValueAsParameters());
				closeCurlyBracket();
			}
		} else if(parameter.getParameterValueType() == ParameterValueType.STRING || parameter.getParameterValueType() == ParameterValueType.VARIABLE) {
			if(parameter.isArray()) {
				openSquareBracket();
				for(String value : parameter.getValueAsStringList()) {
					indent();
					writeString(value);
				}
				closeSquareBracket();
			} else {
				writeString(parameter.getValueAsString());
			}
		} else if(parameter.getParameterValueType() == ParameterValueType.TEXT) {
			writeText(parameter.getValueAsString());
		} else {
			write(parameter.getValue());
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
		nextLine();
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

	private void nextLine() throws IOException {
		writer.write("\n");
	}
	
	private void indent() throws IOException {
		if(prettyWrite) {
			for(int i = 0; i < indentDepth; i++) {
				writer.write('\t');
			}
		}
	}
	
	private void indentPlus() throws IOException {
		if(prettyWrite) {
			indentDepth++;
		}
	}
	
	private void indentMinus() throws IOException {
		if(prettyWrite) {
			indentDepth--;
		}
	}
	
	public void flush() throws IOException {
		writer.flush();
	}
	
	private String escape(String value) {
		return value;
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
