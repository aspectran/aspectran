package com.aspectran.core.util.apon;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class AponWriter extends AponDocument {

	private boolean addable;
	
	private Writer writer;
	
	private int indentDepth;
	
	public AponWriter(Writer writer) {
		this.writer = writer;
	}
	
	public void write(Parameters parameters) {
		Map<String, ParameterValue> parameterValueMap = parameters.getParameterValueMap();
		
		for(ParameterValue pv : parameterValueMap.values()) {
			if(pv.isAssigned()) {
				if(pv.isArray()) {
					if(pv.getParameterValueType() == ParameterValueType.PARAMETERS) {
						for(Parameters p : pv.getValueAsParametersList()) {
							write(p);
						}
					}
				} else {
					if(pv.getParameterValueType() == ParameterValueType.PARAMETERS) {
						write(pv.getValueAsParameters());
					}
				}
			}
		}
	}
	
	public void write(Parameter parameter) throws IOException {
		if(parameter.getParameterValueType() == ParameterValueType.STRING || parameter.getParameterValueType() == ParameterValueType.VARIABLE) {
			writeString(parameter.getValueAsString());
		} else if(parameter.getParameterValueType() == ParameterValueType.TEXT) {
			writeText(parameter.getValueAsString());
		} else {
			write(parameter.getValue());
		}
	}
	
	public void writeString(String value) throws IOException {
		writer.append(QUOTE_CHAR);
		writer.append(escape(value));
		writer.append(QUOTE_CHAR);
	}
	
	public void writeText(String value) {
		
	}
	
	public void write(Object value) throws IOException {
		if(value == null) {
			writer.write(NULL);
			return;
		}
		
		writer.write(value.toString());
	}
	
	public String escape(String value) {
		return value;
	}
	
	public static void main(String argv[]) {
		try {
			//Importable importable = new ImportableFile("/c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/config/aspectran/sample/sample-test.apon", ImportFileType.APON);
			//AponWriter aponReader = new AponWriter();
			//aponReader.read(importable.getReader());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
}
