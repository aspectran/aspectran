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
package com.aspectran.core.context.rule;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * The Class TransletRuleMap.
 *
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class TransletRuleMap extends LinkedHashMap<String, TransletRule> implements Iterable<TransletRule> {

	/** @serial */
	static final long serialVersionUID = -6355555002181276839L;

	private boolean freezed;
	
	public TransletRule put(String key, TransletRule value) {
		throw new java.lang.UnsupportedOperationException();
	}
	
	/**
	 * Adds the translet rule.
	 * 
	 * @param transletRule the translet rule
	 * 
	 * @return the translet rule
	 */
	public TransletRule putTransletRule(TransletRule transletRule) {
		if(freezed)
			throw new UnsupportedOperationException("freezed transletRuleMap: " + toString());

		String key;
		
		if(transletRule.getRestVerb() != null) {
			key = TransletRule.makeRestfulTransletName(transletRule.getName(), transletRule.getRestVerb());
		} else {
			key = transletRule.getName();
		}
		
		return super.put(key, transletRule);
	}
	
	public void addShallowTransletRule(TransletRule transletRule) {
		super.put(Integer.toString(size()), transletRule);
	}
	
	public void freeze() {
		freezed = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<TransletRule> iterator() {
		return this.values().iterator();
	}

}
