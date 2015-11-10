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

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * The Class ItemRuleMap.
 * 
 * <p>Created: 2008. 03. 29 오후 5:00:20</p>
 */
public class ItemRuleMap extends LinkedHashMap<String, ItemRule> implements Iterable<ItemRule> {

	/** @serial */
	static final long serialVersionUID = 192817512158305803L;

	/**
	 * Adds a value rule.
	 * 
	 * @param itemRule the value rule
	 * 
	 * @return the value rule
	 */
	public ItemRule putItemRule(ItemRule itemRule) {
		return put(itemRule.getName(), itemRule);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ItemRule> iterator() {
		return this.values().iterator();
	}
}
