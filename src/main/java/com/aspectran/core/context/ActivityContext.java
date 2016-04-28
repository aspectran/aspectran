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
import com.aspectran.core.context.message.MessageSource;
import com.aspectran.core.context.template.TemplateProcessor;
import com.aspectran.core.context.translet.TransletRuleRegistry;

/**
 * The Class ActivityContext.
 * 
 * <p>Created: 2008. 06. 09 PM 2:12:40</p>
 */
public interface ActivityContext extends MessageSource {

	String TRANSLET_NAME_SEPARATOR = "/";

	char TRANSLET_NAME_SEPARATOR_CHAR = '/';

	String ID_SEPARATOR = ".";

	char ID_SEPARATOR_CHAR = '.';

	String LINE_SEPARATOR = "\n";

	String DEFAULT_ENCODING = "UTF-8";

	String MESSAGE_SOURCE_BEAN_ID = "messageSource";

	/**
	 * Gets class loader.
	 *
	 * @return the class loader
	 */
	ClassLoader getClassLoader();
	
	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	ApplicationAdapter getApplicationAdapter();

	/**
	 * Gets the aspect rule registry.
	 *
	 * @return the aspect rule registry
	 */
	AspectRuleRegistry getAspectRuleRegistry();

	/**
	 * Gets the context bean registry.
	 *
	 * @return the context bean registry
	 */
	ContextBeanRegistry getContextBeanRegistry();

	/**
	 * Gets the translet rule registry.
	 *
	 * @return the translet rule registry
	 */
	TransletRuleRegistry getTransletRuleRegistry();

	/**
	 * Gets the template processor.
	 *
	 * @return the template processor
	 */
	TemplateProcessor getTemplateProcessor();

	/**
	 * Gets the message source.
	 *
	 * @return the message source
	 */
	MessageSource getMessageSource();

	/**
	 * Gets the current activity.
	 *
	 * @return the current activity
	 */
	Activity getCurrentActivity();
	
	/**
	 * Sets the current activity.
	 *
	 * @param activity the new current activity
	 */
	void setCurrentActivity(Activity activity);
	
	/**
	 * Removes the current activity.
	 */
	void removeCurrentActivity();
	
	/**
	 * Gets the active profiles.
	 *
	 * @return the active profiles
	 */
	String[] getActiveProfiles();
	
	/**
	 * Destroy the aspectran context. 
	 */
	public void destroy();

}
