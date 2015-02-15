/*
 * Copyright 2002-2006 the original author or authors.
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

package com.aspectran.core.context.translet;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;


/**
 * Exception thrown when instantiation of a bean failed.
 * Carries the offending bean class.
 *
 * @author Juergen Hoeller
 * @since 1.2.8
 */
public class TransletInstantiationException extends RuntimeException {

	/** @serial */
	static final long serialVersionUID = -2906926519983962457L;
	
	private Class<? extends Translet> transletInterfaceClass;

	private Class<? extends CoreTranslet> transletInstanceClass;
	
	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 */
	public TransletInstantiationException(Class<? extends Translet> transletInterfaceClass, Class<? extends CoreTranslet> transletInstanceClass) {
		this(transletInterfaceClass, transletInstanceClass, null);
	}

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public TransletInstantiationException(Class<? extends Translet> transletInterfaceClass, Class<? extends CoreTranslet> transletInstanceClass, Throwable cause) {
		super("Could not instantiate translet class [" + transletInstanceClass.getName() + "] interface [" + transletInterfaceClass.getName() + "]", cause);
		this.transletInstanceClass = transletInstanceClass;
	}

	public Class<? extends Translet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	/**
	 * Return the offending bean class.
	 */
	public Class<?> getTransletInstanceClass() {
		return transletInstanceClass;
	}

}
