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
public class BeanDestroyFailedException extends BeanRuleException {

	/** @serial */
	private static final long serialVersionUID = -2416583532228763870L;
	
	/**
	 * Create a new BeanDestroyFailedException.
	 *
	 * @param beanRule the bean rule
	 */
	public BeanDestroyFailedException(BeanRule beanRule) {
		this(beanRule, "Cannot destroy a bean ");
	}

	/**
	 * Create a new BeanDestroyFailedException.
	 *
	 * @param beanRule the bean rule
	 * @param msg The detail message
	 */
	public BeanDestroyFailedException(BeanRule beanRule, String msg) {
		super(beanRule, msg);
	}

	/**
	 * Create a new BeanDestroyFailedException.
	 *
	 * @param beanRule the bean rule
	 * @param cause the root cause
	 */
	public BeanDestroyFailedException(BeanRule beanRule, Throwable cause) {
		this(beanRule, "Cannot destroy a bean", cause);
	}

	/**
	 * Create a new BeanDestroyFailedException.
	 *
	 * @param beanRule the bean rule
	 * @param msg The detail message
	 * @param cause the root cause
	 */
	public BeanDestroyFailedException(BeanRule beanRule, String msg, Throwable cause) {
		super(beanRule, msg, cause);
	}

}
