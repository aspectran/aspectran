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
package com.aspectran.core.context.aspect;

import com.aspectran.core.context.rule.AspectRule;

/**
 * The Class InvalidPointcutPatternException.
 */
public class InvalidPointcutPatternException extends AspectException {
	
	/** @serial */
	private static final long serialVersionUID = 3736262494374232352L;

	/**
	 * Creates a new InvalidPointcutPatternException without detail message.
	 */
	public InvalidPointcutPatternException() {
	}

	/**
	 * Constructs a InvalidPointcutPatternException with the specified detail message.
	 * 
	 * @param msg A message to associate with the exception
	 */
	public InvalidPointcutPatternException(String msg) {
		super(msg);
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause The real cause of the exception
	 */
	public InvalidPointcutPatternException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a InvalidPointcutPatternException with the specified error message and also the specified root cause exception.
	 * The root cause exception is generally for TypeConversionException's root cause or something that might have caused a InvalidPointcutPatternException.
	 * 
	 * @param msg The detail message
	 * @param cause The real cause of the exception
	 */
	public InvalidPointcutPatternException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * Instantiates a new invalid pointcut pattern exception.
	 *
	 * @param aspectRule the aspect rule
	 * @param msg the msg
	 */
	public InvalidPointcutPatternException(AspectRule aspectRule, String msg) {
		super(msg + " : aspectRule " + aspectRule);
	}
	
}