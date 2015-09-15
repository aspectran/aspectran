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
package com.aspectran.core.util.apon;

import com.aspectran.core.util.StringUtils;


/**
 * This exception will be thrown when a translet request is failed.
 * 
 * <p>Created: 2008. 01. 07 오전 3:35:55</p>
 */
public class InvalidParameterException extends RuntimeException {
	
	/** @serial */
	private static final long serialVersionUID = 3050709527383043944L;

	/**
	 * Simple constructor.
	 */
	public InvalidParameterException() {
	}

	/**
	 * Constructor to create exception with a message.
	 * 
	 * @param msg A message to associate with the exception
	 */
	public InvalidParameterException(String msg) {
		super(msg);
	}

	/**
	 * Constructor to create exception with a message.
	 * 
	 * @param msg A message to associate with the exception
	 */
	public InvalidParameterException(int lineNumber, String line, String trim, String msg) {
		super(makeMessage(lineNumber, line, trim, msg));
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause The real cause of the exception
	 */
	public InvalidParameterException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a
	 * message.
	 * 
	 * @param msg The message
	 * @param cause The real cause of the exception
	 */
	public InvalidParameterException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	protected static String makeMessage(int lineNumber, String line, String trim, String msg) {
		int column = (trim != null) ? line.indexOf(trim) : 0;
		
		StringBuilder sb = new StringBuilder();
		if(msg != null)
			sb.append(msg);
		sb.append(" Line number: ").append(lineNumber);
		if(column != -1) {
			String lspace = line.substring(0, column);
			int tabCnt = StringUtils.search(lspace, "\t");
			
			if(trim != null && trim.length() > 33)
				trim = trim.substring(0, 30) + "...";
			
			sb.append(", Column: ").append(column + 1);
			
			if(tabCnt == 0) {
				sb.append(column);
			} else {
				sb.append(" (");
				sb.append("Tab ").append(tabCnt);
				sb.append(", Space ").append(column - tabCnt);
				sb.append(")");
			}
			
			sb.append(": ").append(trim);
		}
		
		return sb.toString();
	}
}
