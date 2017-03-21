/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.aspect;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AspectRuleRegistry.
 */
public class AspectRuleRegistry {

	private final Log log = LogFactory.getLog(AspectRuleRegistry.class);

	private final Map<String, AspectRule> aspectRuleMap = new LinkedHashMap<>();
	
	private AspectAdviceRuleRegistry sessionAspectAdviceRuleRegistry;
	
	public AspectRuleRegistry() {
	}
	
	public Map<String, AspectRule> getAspectRuleMap() {
		return aspectRuleMap;
	}

	public boolean contains(String aspectId) {
		return aspectRuleMap.containsKey(aspectId);
	}
	
	public AspectRule getAspectRule(String aspectId) {
		return aspectRuleMap.get(aspectId);
	}

	public void addAspectRule(AspectRule aspectRule) {
		aspectRuleMap.put(aspectRule.getId(), aspectRule);
		
		if (log.isTraceEnabled()) {
			log.trace("add AspectRule " + aspectRule);
		}
	}

	public Collection<AspectRule> getAspectRules() {
		return aspectRuleMap.values();
	}

	public void clear() {
		aspectRuleMap.clear();
	}

	public AspectAdviceRuleRegistry getSessionAspectAdviceRuleRegistry() {
		return sessionAspectAdviceRuleRegistry;
	}

	public void setSessionAspectAdviceRuleRegistry(AspectAdviceRuleRegistry sessionAspectAdviceRuleRegistry) {
		this.sessionAspectAdviceRuleRegistry = sessionAspectAdviceRuleRegistry;
	}

}
