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
package com.aspectran.core.activity;

import com.aspectran.core.context.AspectranRuntimeException;

/**
 * This exception will be thrown when a translet process is failed..
 * 
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class ActivityException extends AspectranRuntimeException {
	
	/** @serial */
	static final long serialVersionUID = -4400747654771758521L;

	/**
	 * Instantiates a new ActivityException.
	 */
	public ActivityException() {
		super();
	}

	/**
	 * Instantiates a new ActivityException.
	 * 
	 * @param msg A message to associate with the exception
	 */
	public ActivityException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new ActivityException.
	 * 
	 * @param cause The real cause of the exception
	 */
	public ActivityException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new ActivityException.
	 * 
	 * @param msg The message
	 * @param cause The real cause of the exception
	 */
	public ActivityException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
