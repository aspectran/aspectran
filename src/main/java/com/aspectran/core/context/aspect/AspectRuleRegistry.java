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
package com.aspectran.core.context.aspect;

import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;

/**
 * The Class AspectRuleRegistry.
 */
public class AspectRuleRegistry {

	private AspectRuleMap aspectRuleMap;
	
	private AspectAdviceRuleRegistry sessionAspectAdviceRuleRegistry;
	
	public AspectRuleRegistry(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}
	
	/**
	 * Gets the aspect rule map.
	 * 
	 * @return the aspect rule map
	 */
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleMap;
	}

	public boolean contains(String aspectId) {
		return aspectRuleMap.containsKey(aspectId);
	}
	
	public AspectRule getAspectRule(String aspectId) {
		return aspectRuleMap.get(aspectId);
	}

	public AspectAdviceRuleRegistry getSessionAspectAdviceRuleRegistry() {
		return sessionAspectAdviceRuleRegistry;
	}

	public void setSessionAspectAdviceRuleRegistry(AspectAdviceRuleRegistry sessionAspectAdviceRuleRegistry) {
		this.sessionAspectAdviceRuleRegistry = sessionAspectAdviceRuleRegistry;
	}

	public void destroy() {
		if(aspectRuleMap != null) {
			aspectRuleMap.clear();
			aspectRuleMap = null;
		}
	}

}
