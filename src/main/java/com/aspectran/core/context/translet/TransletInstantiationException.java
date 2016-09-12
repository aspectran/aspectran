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
package com.aspectran.core.context.translet;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;

/**
 * Exception thrown when instantiation of a translet failed.
 */
public class TransletInstantiationException extends TransletException {

	/** @serial */
	private static final long serialVersionUID = -762767668765160738L;
	
	private Class<? extends Translet> transletInterfaceClass;

	private Class<? extends CoreTranslet> transletInstanceClass;
	
	/**
	 * Create a new TransletInstantiationException.
	 *
	 * @param transletInterfaceClass the translet interface class
	 * @param transletInstanceClass the translet instance class
	 */
	public TransletInstantiationException(Class<? extends Translet> transletInterfaceClass, Class<? extends CoreTranslet> transletInstanceClass) {
		super("Could not instantiate translet class [" + transletInstanceClass.getName() + "] interface [" + transletInterfaceClass.getName() + "]");
		this.transletInterfaceClass = transletInterfaceClass;
		this.transletInstanceClass = transletInstanceClass;
	}

	/**
	 * Create a new TransletInstantiationException.
	 *
	 * @param transletInterfaceClass the translet interface class
	 * @param transletInstanceClass the translet instance class
	 * @param cause the root cause
	 */
	public TransletInstantiationException(Class<? extends Translet> transletInterfaceClass, Class<? extends CoreTranslet> transletInstanceClass, Throwable cause) {
		super("Could not instantiate translet class [" + transletInstanceClass.getName() + "] interface [" + transletInterfaceClass.getName() + "]", cause);
		this.transletInterfaceClass = transletInterfaceClass;
		this.transletInstanceClass = transletInstanceClass;
	}
	
	/**
	 * Create a new TransletInstantiationException.
	 *
	 * @param transletInstanceClass the translet instance class
	 * @param cause the root cause
	 */
	public TransletInstantiationException(Class<? extends CoreTranslet> transletInstanceClass, Throwable cause) {
		super("Could not instantiate translet class [" + transletInstanceClass.getName() + "]", cause);
		this.transletInstanceClass = transletInstanceClass;
	}

	/**
	 * Returns the translet interface class.
	 *
	 * @return the translet interface class
	 */
	public Class<? extends Translet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	/**
	 * Returns the translet instance class.
	 *
	 * @return the translet instance class
	 */
	public Class<?> getTransletInstanceClass() {
		return transletInstanceClass;
	}

}
