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


/**
 * This exception will be thrown when a translet request is failed.
 * 
 * <p>Created: 2008. 01. 07 오전 3:35:55</p>
 */
public class IncompatibleParameterValueTypeException extends InvalidParameterException {
	
	/** @serial */
	private static final long serialVersionUID = 1557599183505068164L;

	/**
	 * Simple constructor.
	 */
	public IncompatibleParameterValueTypeException() {
	}

	/**
	 * Constructor to create exception with a message.
	 * 
	 * @param msg A message to associate with the exception
	 */
	public IncompatibleParameterValueTypeException(String msg) {
		super(msg);
	}

	public IncompatibleParameterValueTypeException(ParameterValue parameterValue, ParameterValueType expectedParameterValueType) {
		super("Incompatible value type with expected value type \"" + expectedParameterValueType + "\" for the specified parameter " + parameterValue);
	}
	
	public IncompatibleParameterValueTypeException(int lineNumber, String line, String trim, String msg) {
		super(lineNumber, line, trim, msg);
	}
	
	public IncompatibleParameterValueTypeException(int lineNumber, String line, String trim, ParameterValue parameterValue, ParameterValueType expectedParameterValueType) {
		super(makeMessage(lineNumber, line, trim, parameterValue, expectedParameterValueType));
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause The real cause of the exception
	 */
	public IncompatibleParameterValueTypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a
	 * message.
	 * 
	 * @param msg The message
	 * @param cause The real cause of the exception
	 */
	public IncompatibleParameterValueTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	protected static String makeMessage(int lineNumber, String line, String trim, ParameterValue parameterValue, ParameterValueType expectedParameterValueType) {
		StringBuilder sb = new StringBuilder();
		sb.append("Incompatible value type with expected value type \"");
		sb.append(expectedParameterValueType).append("\"");
		if(parameterValue != null)
			sb.append(" for the specified parameter ").append(parameterValue);
		sb.append(".");
		
		return InvalidParameterException.makeMessage(lineNumber, line, trim, sb.toString());
	}
	
}
