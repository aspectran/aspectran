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
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class TransletRuleMap extends LinkedHashMap<String, TransletRule> implements Iterable<TransletRule> {

	/** @serial */
	static final long serialVersionUID = -6355555002181276839L;

	private boolean freezed;
	
	public TransletRule put(String key, TransletRule value) {
		if(freezed)
			throw new java.lang.UnsupportedOperationException("freezed transletRuleMap: " + toString());

		return super.put(key, value);
	}
	
//	/** The multi activity translet rule map. */
//	private MultiActivityTransletRuleMap multiActivityTransletRuleMap;;
//
//	/**
//	 * Instantiates a new translet rule map.
//	 */
//	public TransletRuleMap() {
//		multiActivityTransletRuleMap = new MultiActivityTransletRuleMap();
//	}
//	
//	/**
//	 * Gets the translet rule by uri.
//	 * 
//	 * @param requestUri the service uri
//	 * 
//	 * @return the translet rule by uri
//	 */
//	public TransletRule get(Object key) {
//		TransletRule o = super.get(key);
//		
//		if(o != null)
//			return o;
//
//		return multiActivityTransletRuleMap.get(key);
//	}

	/**
	 * Adds the translet rule.
	 * 
	 * @param transletRule the translet rule
	 * 
	 * @return the translet rule
	 */
	public TransletRule putTransletRule(TransletRule transletRule) {
		return put(transletRule.getName(), transletRule);
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
	
//	/**
//	 * Gets the multi activity translet rule.
//	 *
//	 * @param name the path
//	 * @return the multi activity translet rule
//	 */
//	public MultiActivityTransletRule getMultiActivityTransletRule(String name) {
//		return multiActivityTransletRuleMap.get(name);
//	}
//	
//	/**
//	 * Put multi activity translet rule.
//	 *
//	 * @param name the path
//	 * @param responseId the response id
//	 * @param transletRule the translet rule
//	 * @return the translet rule
//	 */
//	public TransletRule putMultiActivityTransletRule(String name, String responseId, TransletRule transletRule) {
//		MultiActivityTransletRule matr = new MultiActivityTransletRule();
//		matr.setPath(name);
//		matr.setResponseId(responseId);
//		matr.setTransletRule(transletRule);
//		
//		multiActivityTransletRuleMap.put(name, matr);
//		transletRule.addMultiActivityTransletRule(matr);
//
//		return transletRule;
//	}

}
