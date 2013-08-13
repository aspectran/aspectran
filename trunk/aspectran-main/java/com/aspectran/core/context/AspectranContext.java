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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;

/**
 * <p>Created: 2008. 06. 09 오후 2:12:40</p>
 */
public class AspectranContext {

	private ApplicationAdapter applicationAdapter;
	
	private BeanRegistry beanRegistry;

	private TransletRuleRegistry transletRuleRegistry;
	
	public AspectranContext() {
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

	/**
	 * Gets the bean registry.
	 *
	 * @return the bean registry
	 */
	public BeanRegistry getBeanRegistry() {
		return beanRegistry;
	}

	/**
	 * Sets the bean registry.
	 *
	 * @param beanRegistry the new bean registry
	 */
	public void setBeanRegistry(BeanRegistry beanRegistry) {
		this.beanRegistry = beanRegistry;
	}

	public TransletRuleRegistry getTransletRuleRegistry() {
		return transletRuleRegistry;
	}

	public void setTransletRuleRegistry(TransletRuleRegistry transletRuleRegistry) {
		this.transletRuleRegistry = transletRuleRegistry;
	}
	
//	/**
//	 * Gets the translet rule map.
//	 * 
//	 * @return the translet rule map
//	 */
//	public TransletRuleMap getTransletRuleMap() {
//		return transletRuleMap;
//	}
//
//	/**
//	 * Sets the translet rule map.
//	 * 
//	 * @param transletRuleMap the new translet rule map
//	 */
//	public final void setTransletRuleMap(TransletRuleMap transletRuleMap) {
//		this.transletRuleMap = transletRuleMap;
//	}
//	
//	/**
//	 * Gets the translet rule.
//	 * 
//	 * @param path the path
//	 * 
//	 * @return the translet rule
//	 */
//	public TransletRule getTransletRule(String path) {
//		if(transletRuleMap == null)
//			return null;
//		
//		return transletRuleMap.get(path);
//	}
//	
//	/**
//	 * Gets the translet rule by request URI.
//	 *
//	 * @param requestUri the request uri
//	 * @return the translet rule by request uri
//	 */
//	public MultiActivityTransletRule getMultiActivityTransletRule(String path) {
//		if(transletRuleMap == null)
//			return null;
//		
//		return transletRuleMap.getMultiActivityTransletRule(path);
//	}
	
	/**
	 * Destroy the translets context. 
	 */
	public void destroy() {
		if(beanRegistry != null) {
			//TODO
			//beanRegistry.destroy();
			beanRegistry = null;
		}
		if(transletRuleRegistry != null) {
			//TODO
			transletRuleRegistry.destroy();
			transletRuleRegistry = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
//		sb.append("{activityRule=").append(activityRule);
//		sb.append("}");
		
		return sb.toString();
	}
	
}
