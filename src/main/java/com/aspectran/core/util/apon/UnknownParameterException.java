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
 * The Class UnknownParameterException.
 */
public class UnknownParameterException extends InvalidParameterException {
	
	/** @serial */
	private static final long serialVersionUID = 6446576507072773588L;

	/**
	 * Simple constructor.
	 */
	public UnknownParameterException() {
		super();
	}

	/**
	 * Constructor to create exception with a message.
	 *
	 * @param parameterName the parameter name
	 * @param parameters the parameters
	 */
	public UnknownParameterException(String parameterName, Parameters parameters) {
		super("Unknown parameter \"" + parameterName + "\" from " + parameters);
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause The real cause of the exception
	 */
	public UnknownParameterException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a message.
	 * 
	 * @param msg The message
	 * @param cause The real cause of the exception
	 */
	public UnknownParameterException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
