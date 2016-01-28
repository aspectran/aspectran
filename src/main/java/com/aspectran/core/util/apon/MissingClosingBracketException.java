/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 */
public class MissingClosingBracketException extends InvalidParameterException {
	
	/** @serial */
	private static final long serialVersionUID = -6294265620028959255L;

	/**
	 * Simple constructor.
	 */
	public MissingClosingBracketException() {
		super();
	}

	/**
	 * Constructor to create exception with a message.
	 * 
	 * @param msg A message to associate with the exception
	 */
	public MissingClosingBracketException(String msg) {
		super(msg);
	}

	public MissingClosingBracketException(String bracketShape, String name, ParameterValue parameterValue) {
		super("the end of the string was reached with no closing " + bracketShape + " bracket found: " + (parameterValue == null ? name : parameterValue));
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause The real cause of the exception
	 */
	public MissingClosingBracketException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a message.
	 * 
	 * @param msg The message
	 * @param cause The real cause of the exception
	 */
	public MissingClosingBracketException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
