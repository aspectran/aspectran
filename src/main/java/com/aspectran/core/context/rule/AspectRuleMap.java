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
 * The Class AspectRuleMap.
 * 
 * <p>Created: 2009. 03. 09 오후 23:48:09</p>
 */
public class AspectRuleMap extends LinkedHashMap<String, AspectRule> implements Iterable<AspectRule> {

	/** @serial */
	static final long serialVersionUID = 3857952055410456475L;

	/**
	 * Adds a aspect rule.
	 *
	 * @param aspectRule the aspect rule
	 * @return the value rule
	 */
	public AspectRule putAspectRule(AspectRule aspectRule) {
		return put(aspectRule.getId(), aspectRule);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<AspectRule> iterator() {
		return this.values().iterator();
	}

}
