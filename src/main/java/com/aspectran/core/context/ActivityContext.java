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
package com.aspectran.core.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.bean.ContextBeanRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;

/**
 * <p>Created: 2008. 06. 09 오후 2:12:40</p>
 */
public class ActivityContext {
	
	private static ThreadLocal<Activity> currentActivityHolder = new ThreadLocal<Activity>();

	private ApplicationAdapter applicationAdapter;
	
	private AspectRuleRegistry aspectRuleRegistry;
	
	private ContextBeanRegistry contextBeanRegistry;

	private TransletRuleRegistry transletRuleRegistry;
	
	public ActivityContext(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
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
	 * Gets the bean registry.
	 *
	 * @return the bean registry
	 */
	public ContextBeanRegistry getContextBeanRegistry() {
		return contextBeanRegistry;
	}

	/**
	 * Sets the bean registry.
	 *
	 * @param contextBeanRegistry the new bean registry
	 */
	public void setContextBeanRegistry(ContextBeanRegistry contextBeanRegistry) {
		this.contextBeanRegistry = contextBeanRegistry;
	}

	public TransletRuleRegistry getTransletRuleRegistry() {
		return transletRuleRegistry;
	}

	public void setTransletRuleRegistry(TransletRuleRegistry transletRuleRegistry) {
		this.transletRuleRegistry = transletRuleRegistry;
	}
	
	public Activity getCurrentActivity() {
		return currentActivityHolder.get();
	}
	
	public void setCurrentActivity(Activity activity) {
		if(currentActivityHolder.get() == null)
			currentActivityHolder.set(activity);
	}
	
	public void removeCurrentActivity() {
		currentActivityHolder.remove();
	}
	
	/**
	 * Destroy the translets context. 
	 */
	public void destroy() {
		if(aspectRuleRegistry != null) {
			aspectRuleRegistry.destroy();
			aspectRuleRegistry = null;
		}
		if(contextBeanRegistry != null) {
			contextBeanRegistry.destroy();
			contextBeanRegistry = null;
		}
		if(transletRuleRegistry != null) {
			transletRuleRegistry.destroy();
			transletRuleRegistry = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{applicationAdapter=").append(applicationAdapter);
		sb.append(", aspectRuleRegistry=").append(aspectRuleRegistry);
		sb.append(", beanRegistry=").append(contextBeanRegistry);
		sb.append(", transletRuleRegistry=").append(transletRuleRegistry);
		sb.append("}");
		
		return sb.toString();
	}
	
}
