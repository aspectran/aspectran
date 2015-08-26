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
package com.aspectran.core.context.bean.scan;

import com.aspectran.core.context.bean.BeanException;

public class BeanClassScanningFailedException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = -1301450076259511066L;

	/**
	 * Simple constructor.
	 */
	public BeanClassScanningFailedException() {
	}

	/**
	 * Constructor to create exception with a message.
	 * 
	 * @param msg A message to associate with the exception
	 */
	public BeanClassScanningFailedException(String msg) {
		super(msg);
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause The real cause of the exception
	 */
	public BeanClassScanningFailedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a
	 * message.
	 * 
	 * @param msg The message
	 * @param cause The real cause of the exception
	 */
	public BeanClassScanningFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
