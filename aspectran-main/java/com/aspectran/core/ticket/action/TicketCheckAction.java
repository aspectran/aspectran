/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.ticket.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.rule.TicketCheckRule;
import com.aspectran.core.rule.TicketCheckcaseRule;

/**
 * <p>Created: 2008. 03. 22 오후 5:50:35</p>
 */
public class TicketCheckAction implements Executable {

	/** The log. */
	private final Log log = LogFactory.getLog(TicketCheckAction.class);
	
	/** The ticket check rule. */
	private final TicketCheckRule ticketCheckRule;

	/**
	 * Instantiates a new ticket check action.
	 *
	 * @param ticketCheckRule the ticket check rule
	 */
	public TicketCheckAction(TicketCheckRule ticketCheckRule) {
		this.ticketCheckRule = ticketCheckRule;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.process.action.Executable#execute(org.jhlabs.translets.activity.Activity)
	 */
	public Object execute(AspectranActivity activity) throws ActionExecutionException {
		try {
			return invokeMethod(activity, ticketCheckRule);
		} catch(Exception e) {
			log.error("Execute error: TicketCheckRule " + ticketCheckRule.toString());
			throw new ActionExecutionException(this, e);
		}
	}
	
	/**
	 * Gets the ticket check rule.
	 *
	 * @return the ticket check rule
	 */
	public TicketCheckRule getTicketCheckRule() {
		return ticketCheckRule;
	}

	/**
	 * Invoke method.
	 *
	 * @param activity the activity
	 * @param ticketCheckRule the ticket check rule
	 * @return the object
	 * @throws Exception the exception
	 */
	public Object invokeMethod(AspectranActivity activity, TicketCheckRule ticketCheckRule) throws Exception {
		TicketCheckcaseRule ticketCheckcaseRule = ticketCheckRule.getTicketCheckcaseRule();

		String beanId = ticketCheckcaseRule.getBeanId();
		String methodName = ticketCheckcaseRule.getMethodName();
		ItemRuleMap propertyItemRuleMap = ticketCheckRule.getPropertyItemRuleMap();
		ItemRuleMap argumentItemRuleMap = ticketCheckRule.getArgumentItemRuleMap();
		
		if(propertyItemRuleMap == null)
			propertyItemRuleMap = ticketCheckcaseRule.getPropertyItemRuleMap();

		if(argumentItemRuleMap == null)
			argumentItemRuleMap = ticketCheckcaseRule.getArgumentItemRuleMap();
		
		return BeanAction.invokeMethod(activity, beanId, methodName, propertyItemRuleMap, argumentItemRuleMap);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("ticketCheckRule=").append(ticketCheckRule.toString());
		sb.append("}");

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.process.action.Executable#getId()
	 */
	public String getId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.process.action.Executable#isHidden()
	 */
	public boolean isHidden() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.process.action.Executable#getParent()
	 */
	public ActionList getParent() {
		return null;
	}
	
}
