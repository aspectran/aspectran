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
package com.aspectran.core.context.bean.proxy;

import com.aspectran.core.context.bean.BeanInstantiationException;

/**
 * Exception thrown when instantiation of a proxy bean failed.
 */
public class ProxyBeanInstantiationException extends BeanInstantiationException {

	/** @serial */
	static final long serialVersionUID = -2906926519983962457L;
	
	private Class<?> beanClass;

	/**
	 * Create a new ProxyBeanInstantiationException.
	 *
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public ProxyBeanInstantiationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Create a new ProxyBeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param cause the root cause
	 */
	public ProxyBeanInstantiationException(Class<?> beanClass, Throwable cause) {
		this(beanClass, cause.getMessage(), cause);
	}

	/**
	 * Create a new ProxyBeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 */
	public ProxyBeanInstantiationException(Class<?> beanClass, String msg) {
		this(beanClass, msg, null);
	}

	/**
	 * Create a new ProxyBeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public ProxyBeanInstantiationException(Class<?> beanClass, String msg, Throwable cause) {
		super("Could not instantiate proxy bean class [" + beanClass.getName() + "]: " + msg, cause);
		this.beanClass = beanClass;
	}

	/**
	 * Return the offending bean class.
	 */
	public Class<?> getBeanClass() {
		return beanClass;
	}

}
