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


public class BeanNotFoundException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = 1866105813455720749L;

	/**
	 * Instantiates a new bean not found exception.
	 *
	 * @param beanId the bean id
	 */
	public BeanNotFoundException(String beanId) {
		this(beanId, null);
	}

	/**
	 * Instantiates a new bean not found exception.
	 *
	 * @param beanId the bean id
	 * @param cause the root cause
	 */
	public BeanNotFoundException(String beanId, Throwable cause) {
		super("No bean named '" + beanId + "' is defined");
	}
}
