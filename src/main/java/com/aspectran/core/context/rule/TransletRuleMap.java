/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.rule;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * The Class TransletRuleMap.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class TransletRuleMap extends LinkedHashMap<String, TransletRule> implements Iterable<TransletRule> {

	/** @serial */
	private static final long serialVersionUID = -6355555002181276839L;

	/**
	 * Adds the translet rule.
	 * 
	 * @param transletRule the translet rule
	 * @return the translet rule
	 */
	public TransletRule putTransletRule(TransletRule transletRule) {
		String key;
		
		if(transletRule.getRestVerb() != null) {
			key = TransletRule.makeRestfulTransletName(transletRule.getName(), transletRule.getRestVerb());
		} else {
			key = transletRule.getName();
		}
		
		return super.put(key, transletRule);
	}

	@Override
	public Iterator<TransletRule> iterator() {
		return this.values().iterator();
	}

}
