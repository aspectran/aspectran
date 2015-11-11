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
package com.aspectran.core.context.bean;

import com.aspectran.core.context.rule.BeanRule;

/**
 * The Class BeanDestroyFailedException.
 */
public class BeanDestroyFailedException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = -2416583532228763870L;
	
	private BeanRule beanRule;

	/**
	 * Create a new BeanInstantiationException.
	 *
	 * @param beanRule the bean rule
	 */
	public BeanDestroyFailedException(BeanRule beanRule) {
		this(beanRule, null);
	}

	/**
	 * Create a new BeanInstantiationException.
	 *
	 * @param beanRule the bean rule
	 * @param cause the root cause
	 */
	public BeanDestroyFailedException(BeanRule beanRule, Throwable cause) {
		super("Cannot destroy a bean " + beanRule, cause);
		this.beanRule = beanRule;
	}

	public BeanRule getBeanRule() {
		return beanRule;
	}
	
}
