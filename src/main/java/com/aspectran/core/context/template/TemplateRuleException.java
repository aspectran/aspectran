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
package com.aspectran.core.context.template;

import com.aspectran.core.context.rule.TemplateRule;

/**
 * The Class TemplateRuleException.
 */
public class TemplateRuleException extends TemplateException {

	/** @serial */
	private static final long serialVersionUID = -3101097393726872156L;

	private final TemplateRule templateRule;

	/**
	 * Instantiates a new TemplateRuleException.
	 *
	 * @param templateRule the template rule
	 * @param msg The detail message
	 */
	public TemplateRuleException(TemplateRule templateRule, String msg) {
		super(msg + " " + templateRule);
		this.templateRule = templateRule;
	}

	/**
	 * Instantiates a new TemplateRuleException.
	 *
	 * @param templateRule the template rule
	 * @param msg The detail message
	 * @param cause the root cause
	 */
	public TemplateRuleException(TemplateRule templateRule, String msg, Throwable cause) {
		super(msg + " " + templateRule, cause);
		this.templateRule = templateRule;
	}

	/**
	 * Gets bean rule.
	 *
	 * @return the template rule
	 */
	public TemplateRule getTemplateRule() {
		return templateRule;
	}

}
