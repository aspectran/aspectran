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
package com.aspectran.core.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.bean.ContextBeanRegistry;
import com.aspectran.core.context.template.TemplateProcessor;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class ActivityContext.
 * 
 * <p>Created: 2008. 06. 09 PM 2:12:40</p>
 */
public class ActivityContext {
	
	private static ThreadLocal<Activity> currentActivityHolder = new ThreadLocal<Activity>();

	private final ApplicationAdapter applicationAdapter;
	
	private AspectRuleRegistry aspectRuleRegistry;
	
	private ContextBeanRegistry contextBeanRegistry;

	private TransletRuleRegistry transletRuleRegistry;
	
	private TemplateProcessor templateProcessor;
	
	/**
	 * Instantiates a new ActivityContext.
	 *
	 * @param applicationAdapter the application adapter
	 */
	public ActivityContext(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}

	/**
	 * Gets class loader.
	 *
	 * @return the class loader
	 */
	public ClassLoader getClassLoader() {
		return applicationAdapter.getClassLoader();
	}
	
	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public AspectRuleRegistry getAspectRuleRegistry() {
		return aspectRuleRegistry;
	}

	public void setAspectRuleRegistry(AspectRuleRegistry aspectRuleRegistry) {
		this.aspectRuleRegistry = aspectRuleRegistry;
	}

	/**
	 * Gets the context bean registry.
	 *
	 * @return the context bean registry
	 */
	public ContextBeanRegistry getContextBeanRegistry() {
		return contextBeanRegistry;
	}

	/**
	 * Sets the context bean registry.
	 *
	 * @param contextBeanRegistry the new context bean registry
	 */
	public void setContextBeanRegistry(ContextBeanRegistry contextBeanRegistry) {
		this.contextBeanRegistry = contextBeanRegistry;
	}

	/**
	 * Gets the translet rule registry.
	 *
	 * @return the translet rule registry
	 */
	public TransletRuleRegistry getTransletRuleRegistry() {
		return transletRuleRegistry;
	}

	/**
	 * Sets the translet rule registry.
	 *
	 * @param transletRuleRegistry the new translet rule registry
	 */
	public void setTransletRuleRegistry(TransletRuleRegistry transletRuleRegistry) {
		this.transletRuleRegistry = transletRuleRegistry;
	}
	
	/**
	 * Gets the template processor.
	 *
	 * @return the template processor
	 */
	public TemplateProcessor getTemplateProcessor() {
		return templateProcessor;
	}

	/**
	 * Sets the template processor.
	 *
	 * @param templateProcessor the new template processor
	 */
	public void setTemplateProcessor(TemplateProcessor templateProcessor) {
		this.templateProcessor = templateProcessor;
	}

	/**
	 * Gets the current activity.
	 *
	 * @return the current activity
	 */
	public Activity getCurrentActivity() {
		return currentActivityHolder.get();
	}
	
	/**
	 * Sets the current activity.
	 *
	 * @param activity the new current activity
	 */
	public void setCurrentActivity(Activity activity) {
		if(currentActivityHolder.get() == null)
			currentActivityHolder.set(activity);
	}
	
	/**
	 * Removes the current activity.
	 */
	public void removeCurrentActivity() {
		currentActivityHolder.remove();
	}
	
	/**
	 * Destroy the aspectran context. 
	 */
	public void destroy() {
		if(aspectRuleRegistry != null) {
			aspectRuleRegistry.clear();
			aspectRuleRegistry = null;
		}
		if(contextBeanRegistry != null) {
			contextBeanRegistry.destroy();
			contextBeanRegistry = null;
		}
		if(transletRuleRegistry != null) {
			transletRuleRegistry.clear();
			transletRuleRegistry = null;
		}
		if(templateProcessor != null) {
			templateProcessor.destroy();
			templateProcessor = null;
		}
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("applicationAdapter", applicationAdapter);
		tsb.append("aspectRuleRegistry", aspectRuleRegistry);
		tsb.append("beanRegistry", contextBeanRegistry);
		tsb.append("transletRuleRegistry", transletRuleRegistry);
		tsb.append("templateProcessor", templateProcessor);
		return tsb.toString();
	}
	
}
