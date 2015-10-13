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
package com.aspectran.core.context.rule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class BeanRuleMap.
 * 
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public class BeanRuleMap extends LinkedHashMap<String, BeanRule> implements Iterable<BeanRule> {

	/** @serial */
	static final long serialVersionUID = 6582559285464575704L;
	
	private boolean freezed;
	
	public BeanRule put(String beanId, BeanRule beanRule) {
		if(freezed)
			throw new UnsupportedOperationException("freezed BeanRuleMap: " + toString());

		BeanRule br = get(beanId);
		
		if(br != null) {
			if(br.isImportant()) {
				return null;
			} else {
				beanRule.setOverrided(true);
			}
		}
		
		return super.put(beanId, beanRule);
	}

	/**
	 * Adds a value rule.
	 * 
	 * @param itemRule the value rule
	 * 
	 * @return the value rule
	 */
	public BeanRule putBeanRule(BeanRule beanRule) {
		return put(beanRule.getId(), beanRule);
	}
	
	public void freeze() {
		freezed = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<BeanRule> iterator() {
		return this.values().iterator();
	}

	public Map<Class<?>, BeanRule> getClassBeanRuleMap() {
		Map<Class<?>, BeanRule> classBeanRuleMap = new HashMap<Class<?>, BeanRule>();
		
		for(BeanRule beanRule : values()) {
			if(beanRule.getId().equals(beanRule.getBeanClass().getName())) {
				classBeanRuleMap.put(beanRule.getBeanClass(), beanRule);
			}
		}
		
		return classBeanRuleMap;
	}
	
}
