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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.TemplateRule;

import java.io.Writer;

/**
 * The Interface TemplateProcessor.
 *
 * <p>Created: 2016. 1. 14.</p>
 */
public interface TemplateProcessor {
	
	/**
	 * Initialize TemplateProcessor.
	 *
	 * @param context the activity context
	 */
	public void initialize(ActivityContext context);

	/**
	 * Destroy TemplateProcessor.
	 */
	public void destroy();

	/**
	 * Gets the template rule registry.
	 *
	 * @return the template rule registry
	 */
	public TemplateRuleRegistry getTemplateRuleRegistry();

	/**
	 * Template processing with specified template ID.
	 *
	 * @param templateId the template id
	 * @return the string
	 */
	public String process(String templateId);

	/**
	 * Template processing with specified TemplateRule by its ID.
	 * Writing the generated output to the supplied {@link Writer}.
	 *
	 * @param templateId the template id
	 * @param writer the writer
	 */
	public void process(String templateId, Writer writer);

	/**
	 * Template processing with specified TemplateRule.
	 *
	 * @param templateRule the template rule
	 * @return the string
	 */
	public String process(TemplateRule templateRule);

	/**
	 * Template processing with specified TemplateRule.
	 * Writing the generated output to the supplied {@link Writer}.
	 *
	 * @param templateRule the template rule
	 * @param writer the {@link Writer} where the output of the template will go. {@link Writer#close()} is not called.
	 */
	public void process(TemplateRule templateRule, Writer writer);

	/**
	 * Template processing with specified TemplateRule by its ID.
	 * Writing the generated output to the supplied {@link Writer}.
	 *
	 * @param templateId the template id
	 * @param activity the activity
	 * @param writer the {@link Writer} where the output of the template will go. {@link Writer#close()} is not called.
	 */
	public void process(String templateId, Activity activity, Writer writer);

	/**
	 * Template processing with specified TemplateRule.
	 * Writing the generated output to the supplied {@link Writer}.
	 *
	 * @param templateRule the template rule
	 * @param activity the activity
	 * @param writer the {@link Writer} where the output of the template will go. {@link Writer#close()} is not called.
	 */
	public void process(TemplateRule templateRule, Activity activity, Writer writer);

}
