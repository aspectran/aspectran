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
import com.aspectran.core.context.bean.registry.BeanRegistry;
import com.aspectran.core.context.translet.registry.TransletRegistry;
import com.aspectran.core.rule.ActivityRule;
import com.aspectran.core.rule.MultiActivityTransletRule;
import com.aspectran.core.rule.TransletRule;

/**
 * Translets client.
 * 
 * <p>Created: 2008. 06. 09 오후 2:12:40</p>
 */
public class AspectranContext {

	private ActivityRule activityRule;
	
	private ApplicationAdapter applicationAdapter;
	
	private BeanRegistry beanRegistry;

	private TransletRegistry transletRegistry;
	
	//private TransletRuleMap transletRuleMap;
	
	public AspectranContext(ActivityRule activityRule) {
		this.activityRule = activityRule;
	}
	
	/**
	 * Gets the activity description.
	 *
	 * @return the activity description
	 */
	public String getActivityDescription() {
		return activityRule.getDescription();
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

	public TransletRegistry getTransletRegistry() {
		return transletRegistry;
	}

	public void setTransletRegistry(TransletRegistry transletRegistry) {
		this.transletRegistry = transletRegistry;
	}
	
	public TransletRule getTransletRule(String transletName) {
		return transletRegistry.getTransletRule(transletName);
	}

	public MultiActivityTransletRule getMultiActivityTransletRule(String transletName) {
		return transletRegistry.getMultiActivityTransletRule(transletName);
	}
	
	public boolean isMultiActivityEnable() {
		return transletRegistry.isMultiActivityEnable();
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
		}
		if(transletRegistry != null) {
			//TODO
			transletRegistry.destroy();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{activityRule=").append(activityRule);
		sb.append("}");
		
		return sb.toString();
	}
}
