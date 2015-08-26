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
package com.aspectran.core.context.translet;

import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;

public class TransletRuleRegistry {

	private final TransletRuleMap transletRuleMap;
	
	public TransletRuleRegistry(TransletRuleMap transletRuleMap) {
		this.transletRuleMap = transletRuleMap;
	}
	
	/**
	 * Gets the translet rule map.
	 * 
	 * @return the translet rule map
	 */
	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

	public boolean contains(String transletName) {
		return transletRuleMap.containsKey(transletName);
	}
	
	public TransletRule getTransletRule(String transletName) {
		return transletRuleMap.get(transletName);
	}
	
	public void destroy() {
		if(transletRuleMap != null) {
			transletRuleMap.clear();
		}
	}

}
