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

import java.util.Locale;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.bean.ContextBeanRegistry;
import com.aspectran.core.context.builder.env.BuildEnvironment;
import com.aspectran.core.context.builder.env.Environment;
import com.aspectran.core.context.message.DelegatingMessageSource;
import com.aspectran.core.context.message.MessageSource;
import com.aspectran.core.context.message.NoSuchMessageException;
import com.aspectran.core.context.template.TemplateProcessor;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AspectranActivityContext.
 * 
 * <p>Created: 2008. 06. 09 PM 2:12:40</p>
 */
public class AspectranActivityContext implements ActivityContext {

	private final Log log = LogFactory.getLog(AspectranActivityContext.class);

	private final ThreadLocal<Activity> currentActivityHolder = new ThreadLocal<>();

	private final Environment environment;
	
	private final ApplicationAdapter applicationAdapter;
	
	private AspectRuleRegistry aspectRuleRegistry;

	private ContextBeanRegistry contextBeanRegistry;

	private TransletRuleRegistry transletRuleRegistry;

	private TemplateProcessor templateProcessor;

	private MessageSource messageSource;
	
	/**
	 * Instantiates a new AspectranActivityContext.
	 *
	 * @param environment the environment
	 */
	public AspectranActivityContext(BuildEnvironment environment) {
		this.environment = environment;
		this.applicationAdapter = environment.getApplicationAdapter();
	}

	@Override
	public Environment getEnvironment() {
		return environment;
	}

	@Override
	public ClassLoader getClassLoader() {
		return applicationAdapter.getClassLoader();
	}
	
	@Override
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	@Override
	public AspectRuleRegistry getAspectRuleRegistry() {
		return aspectRuleRegistry;
	}

	public void setAspectRuleRegistry(AspectRuleRegistry aspectRuleRegistry) {
		this.aspectRuleRegistry = aspectRuleRegistry;
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public MessageSource getMessageSource() {
		if(this.messageSource == null) {
			throw new IllegalStateException("MessageSource not initialized - " +
					"call 'refresh' before accessing messages via the context: " + this);
		}
		return messageSource;
	}

	@Override
	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		return getMessageSource().getMessage(code, args, defaultMessage, locale);
	}

	@Override
	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(code, args, locale);
	}

	@Override
	public Activity getCurrentActivity() {
		return currentActivityHolder.get();
	}

	@Override
	public void setCurrentActivity(Activity activity) {
		currentActivityHolder.set(activity);
	}

	@Override
	public void removeCurrentActivity() {
		currentActivityHolder.remove();
	}

	public void initialize() {
		if(contextBeanRegistry != null)
			contextBeanRegistry.initialize(this);

		if(templateProcessor != null)
			templateProcessor.initialize(this);

		if(contextBeanRegistry != null)
			initMessageSource();
	}

	@Override
	public void destroy() {
		if(templateProcessor != null) {
			templateProcessor.destroy();
			templateProcessor = null;
		}
		if(transletRuleRegistry != null) {
			transletRuleRegistry.clear();
			transletRuleRegistry = null;
		}
		if(aspectRuleRegistry != null) {
			aspectRuleRegistry.clear();
			aspectRuleRegistry = null;
		}
		if(contextBeanRegistry != null) {
			contextBeanRegistry.destroy();
			contextBeanRegistry = null;
		}
	}

	/**
	 * Initialize the MessageSource.
	 * Use parent's if none defined in this context.
	 */
	private void initMessageSource() {
		if(contextBeanRegistry.containsBean(MESSAGE_SOURCE_BEAN_ID)) {
			messageSource = contextBeanRegistry.getBean(MESSAGE_SOURCE_BEAN_ID, MessageSource.class);
			if(log.isDebugEnabled()) {
				log.debug("Using MessageSource [" + messageSource + "]");
			}
		} else {
			// Use empty MessageSource to be able to accept getMessage calls.
			messageSource = new DelegatingMessageSource();
			//contextBeanRegistry.registerSingleton(MESSAGE_SOURCE_BEAN_ID, messageSource);
			if(log.isDebugEnabled()) {
				log.debug("Unable to locate MessageSource with name '" + MESSAGE_SOURCE_BEAN_ID +
						"': using default [" + messageSource + "]");
			}
		}
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("activeProfiles", environment.getActiveProfiles());
		tsb.append("defaultProfiles", environment.getDefaultProfiles());
		tsb.append("applicationAdapter", applicationAdapter);
		tsb.append("aspectRuleRegistry", aspectRuleRegistry);
		tsb.append("beanRegistry", contextBeanRegistry);
		tsb.append("transletRuleRegistry", transletRuleRegistry);
		tsb.append("templateProcessor", templateProcessor);
		tsb.append("messageSource", messageSource);
		return tsb.toString();
	}
	
}
