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
package com.aspectran.core.context.template.engine;

import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.template.TemplateRuleException;

/**
 * This class is the basic exception that gets thrown from the template engine pacakge.
 * 
 * <p>Created: 2016. 01. 15</p>
 */
public class TemplateEngineException extends TemplateRuleException {

	/** @serial */
	private static final long serialVersionUID = -2280211078256350741L;

	/**
	 * Instantiates a new TemplateEngineException.
	 *
	 * @param templateRule the template rule
	 * @param msg The detail message
	 */
	public TemplateEngineException(TemplateRule templateRule, String msg) {
		super(templateRule, msg);
	}

	/**
	 * Instantiates a new TemplateEngineException.
	 *
	 * @param templateRule the template rule
	 * @param msg The detail message
	 * @param cause the root cause
	 */
	public TemplateEngineException(TemplateRule templateRule, String msg, Throwable cause) {
		super(templateRule, msg, cause);
	}

}
