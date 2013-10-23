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
import com.aspectran.core.context.translet.TransletRegistry;

/**
 * <p>Created: 2008. 06. 09 오후 2:12:40</p>
 */
public class AspectranContext {

	private ApplicationAdapter applicationAdapter;
	
	private BeanRegistry beanRegistry;

	private TransletRegistry transletRegistry;
	
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

	public TransletRegistry getTransletegistry() {
		return transletRegistry;
	}

	public void setTransletRuleRegistry(TransletRegistry transletRegistry) {
		this.transletRegistry = transletRegistry;
	}
	
	/**
	 * Destroy the translets context. 
	 */
	public void destroy() {
		if(beanRegistry != null) {
			beanRegistry.destroy();
			beanRegistry = null;
		}
		if(transletRegistry != null) {
			transletRegistry.destroy();
			transletRegistry = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{applicationAdapter=").append(applicationAdapter);
		sb.append(", beanRegistry=").append(beanRegistry);
		sb.append(", transletRegistry=").append(transletRegistry);
		sb.append("}");
		
		return sb.toString();
	}
	
}
