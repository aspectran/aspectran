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
package com.aspectran.core.context.translet;

import com.aspectran.core.context.rule.TransletRule;

/**
 * The Class TransletRuleException.
 */
public class TransletRuleException extends TransletException {

	/** @serial */
	private static final long serialVersionUID = -2570196533755194540L;

	private final TransletRule transletRule;

	/**
	 * Instantiates a new TransletRuleException.
	 *
	 * @param transletRule the translet rule
	 * @param msg The detail message
	 */
	public TransletRuleException(TransletRule transletRule, String msg) {
		super(msg + " " + transletRule);
		this.transletRule = transletRule;
	}

	/**
	 * Instantiates a new TransletRuleException.
	 *
	 * @param transletRule the translet rule
	 * @param msg The detail message
	 * @param cause the root cause
	 */
	public TransletRuleException(TransletRule transletRule, String msg, Throwable cause) {
		super(msg + " " + transletRule, cause);
		this.transletRule = transletRule;
	}

	/**
	 * Gets translet rule.
	 *
	 * @return the bean rule
	 */
	public TransletRule getTransletRule() {
		return transletRule;
	}

}
