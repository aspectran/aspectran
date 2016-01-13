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
 * The Class TemplateRuleMap.
 *
 * <p>Created: 2016. 01. 09</p>
 */
public class TemplateRuleMap extends LinkedHashMap<String, TemplateRule> implements Iterable<TemplateRule> {

	/** @serial */
	private static final long serialVersionUID = 4921723518146075919L;

	private boolean freezed;

	/**
	 * Adds the template rule.
	 * 
	 * @param templateRule the template rule
	 * 
	 * @return the template rule
	 */
	public TemplateRule putTemplateRule(TemplateRule templateRule) {
		if(freezed)
			throw new UnsupportedOperationException("freezed templateRuleMap: " + toString());

		return super.put(templateRule.getId(), templateRule);
	}

	public void freeze() {
		freezed = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<TemplateRule> iterator() {
		return this.values().iterator();
	}

}
