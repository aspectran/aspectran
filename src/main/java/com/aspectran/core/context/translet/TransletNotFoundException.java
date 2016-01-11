/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.translet;

/**
 * This exception will be thrown when a translet not found.
 * 
 * <p>Created: 2008. 01. 07 오전 3:35:55</p>
 */
public class TransletNotFoundException extends TransletException {
	
	/** @serial */
	private static final long serialVersionUID = -5619283297296999361L;

	private String transletName;
	
	/**
	 * Simple constructor.
	 */
	public TransletNotFoundException() {
	}

	/**
	 * Constructor to create exception with a message.
	 * 
	 * @param transletName A message to associate with the exception
	 */
	public TransletNotFoundException(String transletName) {
		super("translet is not found: " + transletName);
		this.transletName = transletName;
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause The real cause of the exception
	 */
	public TransletNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a
	 * message.
	 * 
	 * @param transletName The message
	 * @param cause The real cause of the exception
	 */
	public TransletNotFoundException(String transletName, Throwable cause) {
		super("translet is not found: " + transletName, cause);
		this.transletName = transletName;
	}
	
	public String getTransletName() {
		return transletName;
	}
	
}