/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.rule;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * <p>Created: 2011. 03. 12 오후 5:48:09</p>
 */
public class MultipleTransletRuleMap extends LinkedHashMap<String, MultipleTransletRule> implements Iterable<MultipleTransletRule> {

	/** @serial */
	static final long serialVersionUID = -680839556666107647L;

	/**
	 * Adds the translet rule.
	 * 
	 * @param multipleTransletRule the translet rule
	 * 
	 * @return the translet rule
	 */
	public MultipleTransletRule putMultipleTransletRule(MultipleTransletRule multipleTransletRule) {
		return put(multipleTransletRule.getName(), multipleTransletRule);
	}
	
	public MultipleTransletRule putMultipleTransletRule(String name, String responseId, TransletRule transletRule) {
		MultipleTransletRule matr = new MultipleTransletRule();
		matr.setName(name);
		matr.setResponseId(responseId);
		matr.setTransletRule(transletRule);
		return put(name, matr);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<MultipleTransletRule> iterator() {
		return this.values().iterator();
	}

}
