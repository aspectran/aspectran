/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.activity.response.transform.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.util.BeanUtils;

/**
 * <p>Created: 2008. 06. 12 오후 8:20:54</p>
 */
public class ContentsJSONWriter {

	private Writer writer;

	private boolean prettyWrite;

	private int indentDepth;

	private boolean willWriteValue;

	/**
	 * Instantiates a new contents json writer.
	 * 
	 * @param writer the writer
	 */
	public ContentsJSONWriter(Writer writer) {
		this(writer, false);
	}

	/**
	 * Instantiates a new contents json writer.
	 * 
	 * @param writer the writer
	 * @param prettyWrite the pretty write
	 */
	public ContentsJSONWriter(Writer writer, boolean prettyWrite) {
		this.writer = writer;
		this.prettyWrite = prettyWrite;
		this.indentDepth = 0;
	}

	/**
	 * Write.
	 * 
	 * @param processResult the process result
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvocationTargetException the invocation target exception
	 */
	public void write(ProcessResult processResult) throws IOException, InvocationTargetException {
		openBrace();
		write(processResult, null);
		nextLine();
		closeBrace();
	}

	/**
	 * Write.
	 * 
	 * @param processResult the process result
	 * @param parentContentId the parent action path
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void write(ProcessResult processResult, String parentContentId) throws IOException,
			InvocationTargetException {
		if(processResult == null || processResult.size() == 0)
			return;

		Iterator<ContentResult> iter = processResult.iterator();

		while(iter.hasNext()) {
			ContentResult contentResult = iter.next();

			if(contentResult != null && contentResult.size() > 0) {
				Iterator<ActionResult> iter2 = contentResult.iterator();

				while(iter2.hasNext()) {
					ActionResult actionResult = iter2.next();

					Object resultValue = actionResult.getResultValue();

					if(resultValue == null) {
						writeNull();
					} else if(resultValue instanceof ProcessResult) {
						write((ProcessResult)resultValue, actionResult.getActionPath());
					} else {
						writeName(actionResult.getActionPath(parentContentId));
						write(resultValue);
					}

					if(iter2.hasNext()) {
						writeComma();
						nextLine();
					}
				}

				if(iter.hasNext()) {
					writeComma();
					nextLine();
				}
			}
		}
	}

	/**
	 * Write.
	 * 
	 * @param object the object
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void write(Object object) throws IOException, InvocationTargetException {
		if(object instanceof ProcessResult) {
			indentDepth++;
			write((ProcessResult)object);
			indentDepth--;
		} else if(object instanceof String ||
					object instanceof Number ||
					object instanceof Boolean ||
					object instanceof Date) {
			writeValue(object.toString());
		} else if(object instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)object;

			openBrace();

			Iterator<String> iter = map.keySet().iterator();

			while(iter.hasNext()) {
				String name = iter.next();
				Object value = map.get(name);
				writeName(name);

				if(value == null)
					writeNull();
				else
					write(map.get(name));

				if(iter.hasNext())
					writeComma();

				nextLine();
			}

			closeBrace();
		} else if(object instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Iterator<Object> iter = ((Collection<Object>)object).iterator();

			openSquareBracket();

			while(iter.hasNext()) {
				write(iter.next());

				if(iter.hasNext()) {
					writeComma();
					nextLine();
				}
			}

			closeSquareBracket();
		} else if(object.getClass().isArray()) {

			openSquareBracket();

			int len = Array.getLength(object);
			for(int i = 0; i < len; i++) {
				if(i > 0) {
					writeComma();
					nextLine();
				}

				write(Array.get(object, i));
			}

			closeSquareBracket();
		} else {
			String[] readablePropertyNames = BeanUtils.getReadablePropertyNames(object);

			openBrace();

			if(readablePropertyNames != null && readablePropertyNames.length > 0) {

				for(int i = 0; i < readablePropertyNames.length; i++) {
					Object value = BeanUtils.getObject(object, readablePropertyNames[i]);

					if(object.equals(value))
						continue;

					writeName(readablePropertyNames[i]);

					if(value == null)
						writeNull();
					else
						write(value);

					if(i < (readablePropertyNames.length - 1))
						writeComma();

					nextLine();
				}
			}

			closeBrace();
		}
	}

	/**
	 * Indent.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void indent() throws IOException {
		if(prettyWrite) {
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
	private void writeName(String name) throws IOException {
		indent();

		writer.write(quote(name));
		writer.write(":");

		if(prettyWrite)
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
	private void writeValue(String value) throws IOException {
		if(!willWriteValue)
			indent();

		writer.write(quote(value));

		willWriteValue = false;
	}

	/**
	 * Write null.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeNull() throws IOException {
		writer.write("null");
	}

	/**
	 * Write comma.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeComma() throws IOException {
		writer.write(",");
	}
	
	/**
	 * Next line.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void nextLine() throws IOException {
		if(prettyWrite)
			writer.write("\n");
	}

	/**
	 * Open brace.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void openBrace() throws IOException {
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
	private void closeBrace() throws IOException {
		indentDepth--;

		indent();

		writer.write("}");
	}

	/**
	 * Open square bracket.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void openSquareBracket() throws IOException {
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
	private void closeSquareBracket() throws IOException {
		indentDepth--;

		nextLine();
		indent();

		writer.write("]");
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
	public static String quote(String string) {
		if(string == null || string.length() == 0)
			return "\"\"";

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 4);
		String t;

		sb.append('"');

		for(i = 0; i < len; i++) {
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
