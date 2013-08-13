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
 * <p>Created: 2011. 02. 20 오후 12:56:20</p>
 * @deprecated
 */
public class TicketCheckcaseRuleMap extends LinkedHashMap<String, TicketCheckcaseRule> implements Iterable<TicketCheckcaseRule> {

	/** @serial */
	static final long serialVersionUID = 5538909817384251421L;

	/**
	 * Adds a value rule.
	 * 
	 * @param itemRule the value rule
	 * 
	 * @return the value rule
	 */
	public TicketCheckcaseRule putTicketCheckcaseRule(TicketCheckcaseRule ticketCheckcaseRule) {
		return put(ticketCheckcaseRule.getId(), ticketCheckcaseRule);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<TicketCheckcaseRule> iterator() {
		return this.values().iterator();
	}
	
}
