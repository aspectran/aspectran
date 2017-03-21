/*
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
package com.aspectran.core.context.bean;

/**
 * Exception thrown when instantiation of a bean failed.
 */
public class BeanInstantiationException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = 387409430536237392L;
	
	private Class<?> beanClass;

	/**
	 * Create a new BeanInstantiationException.
	 *
	 * @param beanClass the offending bean class
	 * @param cause the root cause
	 */
	public BeanInstantiationException(Class<?> beanClass, Throwable cause) {
		this(cause.getMessage(), beanClass, cause);
	}

	/**
	 * Create a new BeanInstantiationException.
	 *
	 * @param msg the detail message
	 * @param beanClass the offending bean class
	 * @param cause the root cause
	 */
	public BeanInstantiationException(String msg, Class<?> beanClass, Throwable cause) {
		super("Could not instantiate bean class [" + beanClass.getName() + "]: " + msg, cause);
		this.beanClass = beanClass;
	}

	/**
	 * Create a new BeanInstantiationException.
	 *
	 * @param msg the detail message
	 * @param beanClass the offending bean class
	 */
	public BeanInstantiationException(String msg, Class<?> beanClass) {
		super("Could not instantiate bean class [" + beanClass.getName() + "]: " + msg);
		this.beanClass = beanClass;
	}

	/**
	 * Return the offending bean class.
	 *
	 * @return the bean class
	 */
	public Class<?> getBeanClass() {
		return beanClass;
	}

}
