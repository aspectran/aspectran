/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.context;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.bean.ContextBeanRegistry;
import com.aspectran.core.context.service.ActivityContextServiceHandler;
import com.aspectran.core.context.translet.TransletRuleRegistry;

/**
 * <p>Created: 2008. 06. 09 오후 2:12:40</p>
 */
public class ActivityContext {
	
	private static ThreadLocal<CoreActivity> activityThreadLocal = new ThreadLocal<CoreActivity>();

	private ApplicationAdapter applicationAdapter;
	
	private AspectRuleRegistry aspectRuleRegistry;
	
	private ContextBeanRegistry contextBeanRegistry;

	private TransletRuleRegistry transletRuleRegistry;
	
	private String activityDefaultHandler;
	
	public ActivityContext() {
	}
	
	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	/**
	 * Sets the application adapter.
	 *
	 * @param applicationAdapter the new application adapter
	 */
	public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
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
	
	public String getActivityDefaultHandler() {
		return activityDefaultHandler;
	}

	public void setActivityDefaultHandler(String activityDefaultHandler) {
		this.activityDefaultHandler = activityDefaultHandler;
	}

	public ActivityContextServiceHandler getActivityContextServiceHandler() {
		return applicationAdapter.getActivityContextServiceHandler(this);
	}
	
	public CoreActivity getLocalCoreActivity() {
		return activityThreadLocal.get();
	}
	
	public void saveLocalCoreActivity(CoreActivity activity) {
		if(activityThreadLocal.get() == null)
			activityThreadLocal.set(activity);
	}
	
	public void clearLocalCoreActivity() {
		activityThreadLocal.set(null);
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
		sb.append(", activityDefaultHandler=").append(activityDefaultHandler);
		sb.append("}");
		
		return sb.toString();
	}
	
}
