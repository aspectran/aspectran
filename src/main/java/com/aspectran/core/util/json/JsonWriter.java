/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.util.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.aspectran.core.util.BeanUtils;

/**
 * The Class JsonWriter.
 * 
 * <p>Created: 2008. 06. 12 오후 8:20:54</p>
 */
public class JsonWriter implements Closeable {

	private Writer writer;

	private boolean prettyFormat;

	private int indentDepth;

	private boolean willWriteValue;

	/**
	 * Instantiates a new json writer.
	 * 
	 * @param writer the writer
	 */
	public JsonWriter(Writer writer) {
		this(writer, false);
	}

	/**
	 * Instantiates a new json writer.
	 * 
	 * @param writer the writer
	 * @param prettyFormat the pretty write
	 */
	public JsonWriter(Writer writer, boolean prettyFormat) {
		this.writer = writer;
		this.prettyFormat = prettyFormat;
		this.indentDepth = 0;
	}
	
	/**
	 * Write.
	 * 
	 * @param object the object
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvocationTargetException the invocation target exception
	 */
	public void write(Object object) throws IOException, InvocationTargetException {
		if(object instanceof String ||
					object instanceof Boolean ||
					object instanceof Date) {
			writeString(object.toString());
		} else if(object instanceof Number) {
			writeNumber(object.toString());
		} else if(object instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)object;
			Iterator<String> iter = map.keySet().iterator();

			openCurlyBracket();

			while(iter.hasNext()) {
				String name = iter.next();
				Object value = map.get(name);
				writeName(name);

				if(value == null)
					writeNull();
				else
					write(map.get(name));

				if(iter.hasNext()) {
					writeComma();
				}
			}

			closeCurlyBracket();
		} else if(object instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Iterator<Object> iter = ((Collection<Object>)object).iterator();

			openSquareBracket();

			while(iter.hasNext()) {
				write(iter.next());

				if(iter.hasNext()) {
					writeComma();
				}
			}

			closeSquareBracket();
		} else if(object.getClass().isArray()) {
			openSquareBracket();

			int len = Array.getLength(object);
			for(int i = 0; i < len; i++) {
				if(i > 0) {
					writeComma();
				}

				write(Array.get(object, i));
			}

			closeSquareBracket();
		} else {
			String[] readablePropertyNames = BeanUtils.getReadablePropertyNames(object);

			if(readablePropertyNames != null && readablePropertyNames.length > 0) {
				openCurlyBracket();

				for(int i = 0; i < readablePropertyNames.length; i++) {
					Object value = BeanUtils.getObject(object, readablePropertyNames[i]);

					if(object == value || object.equals(value))
						continue;

					writeName(readablePropertyNames[i]);

					if(value == null) {
						writeNull();
					} else {
						write(value);
					}

					if(i < (readablePropertyNames.length - 1)) {
						writeComma();
					}
				}

				closeCurlyBracket();
			} else {
				writeString(object.toString());
			}
		}
	}

	/**
	 * Indent.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void indent() throws IOException {
		if(prettyFormat) {
			for(int i = 0; i < indentDepth; i++) {
				writer.write('\t');
			}
		}
	}

	/**
	 * Write name.
	 * 
	 * @param name the name
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void writeName(String name) throws IOException {
		indent();

		writer.write(escape(name));
		writer.write(":");

		if(prettyFormat)
			writer.write(" ");

		willWriteValue = true;
	}

	/**
	 * Write value.
	 * 
	 * @param value the value
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void writeString(String value) throws IOException {
		if(!willWriteValue)
			indent();

		writer.write(escape(value));

		willWriteValue = false;
	}

	protected void writeNumber(String value) throws IOException {
		if(!willWriteValue)
			indent();
		
		writer.write(value);
		
		willWriteValue = false;
	}

	/**
	 * Write null.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void writeNull() throws IOException {
		writer.write("null");
	}

	/**
	 * Write comma.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void writeComma() throws IOException {
		writer.write(",");

		if(prettyFormat)
			writer.write(" ");
		
		nextLine();
	}
	
	/**
	 * Next line.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void nextLine() throws IOException {
		if(prettyFormat)
			writer.write("\n");
	}

	/**
	 * Open brace.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void openCurlyBracket() throws IOException {
		if(!willWriteValue)
			indent();

		writer.write("{");
		nextLine();

		indentDepth++;
	}

	/**
	 * Close brace.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void closeCurlyBracket() throws IOException {
		indentDepth--;

		nextLine();
		indent();

		writer.write("}");
	}

	/**
	 * Open square bracket.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void openSquareBracket() throws IOException {
		if(!willWriteValue)
			indent();

		writer.write("[");
		nextLine();
		
		indentDepth++;
		willWriteValue = false;
	}

	/**
	 * Close square bracket.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void closeSquareBracket() throws IOException {
		indentDepth--;

		nextLine();
		indent();

		writer.write("]");
	}

	/**
	 * Flush.
	 * @throws IOException 
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
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, allowing JSON
	 * text to be delivered in HTML. In JSON text, a string cannot contain a
	 * control character or an unescaped quote or backslash.
	 * 
	 * @param string A String
	 * 
	 * @return  A String correctly formatted for insertion in a JSON text.
	 */
	public static String escape(String string) {
		if(string == null || string.length() == 0)
			return "\"\"";

		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 4);
		char b;
		char c = 0;
		String t;

		sb.append('"');

		for(int i = 0; i < len; i++) {
			b = c;
			c = string.charAt(i);

			switch(c) {
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '/':
				if(b == '<') {
					sb.append('\\');
				}
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if(c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				} else {
					sb.append(c);
				}
			}
		}

		sb.append('"');

		return sb.toString();
	}
}
