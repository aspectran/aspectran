/**
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.builder;

import com.aspectran.core.context.ActivityContextException;

/**
 * This exception will be thrown when a ActivityContext build failed.
 * 
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class ActivityContextBuilderException extends ActivityContextException {
	
	/** @serial */
	private static final long serialVersionUID = 5702282474852901391L;

	/**
	 * Simple constructor.
	 */
	public ActivityContextBuilderException() {
		super();
	}

	/**
	 * Constructor to create exception with a message.
	 * 
	 * @param msg the specific message
	 */
	public ActivityContextBuilderException(String msg) {
		super(msg);
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause the real cause of the exception
	 */
	public ActivityContextBuilderException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a message.
	 * 
	 * @param msg the specific message
	 * @param cause the real cause of the exception
	 */
	public ActivityContextBuilderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}