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

import com.aspectran.core.rule.ability.ArgumentPossessable;
import com.aspectran.core.rule.ability.PropertyPossessable;
import com.aspectran.core.type.TicketCheckpointType;


/**
 * <p>Created: 2008. 03. 27 오후 5:24:20</p>
 * @deprecated
 */
public class TicketCheckRule implements ArgumentPossessable, PropertyPossessable {

	/** The ticket checkcase rule. */
	private TicketCheckcaseRule ticketCheckcaseRule;
	
	/** The reject invalid ticket. */
	private Boolean rejectInvalidTicket;

	/** The ticket checkpoint. */
	private TicketCheckpointType ticketCheckpoint;
	
	protected ItemRuleMap propertyItemRuleMap;
	
	protected ItemRuleMap argumentItemRuleMap;

	/**
	 * Instantiates a new ticket check rule.
	 *
	 * @param ticketCheckcaseRule the ticket checkcase rule
	 */
	public TicketCheckRule(TicketCheckcaseRule ticketCheckcaseRule) {
		this.ticketCheckcaseRule = ticketCheckcaseRule;
	}
	
	/**
	 * Gets the ticket checkcase rule.
	 *
	 * @return the ticket checkcase rule
	 */
	public TicketCheckcaseRule getTicketCheckcaseRule() {
		return ticketCheckcaseRule;
	}
	
	/**
	 * Gets the checkcase id.
	 *
	 * @return the checkcase id
	 */
	public String getCheckcaseId() {
		return ticketCheckcaseRule.getId();
	}

	/**
	 * Gets the reject invalid ticket.
	 * 
	 * @return the reject invalid ticket
	 */
	public Boolean getRejectInvalidTicket() {
		if(rejectInvalidTicket == null)
			return ticketCheckcaseRule.getRejectInvalidTicket();
		
		return rejectInvalidTicket;
	}

	/**
	 * Sets the reject invalid ticket.
	 * 
	 * @param rejectInvalidTicket the new reject invalid ticket
	 */
	public void setRejectInvalidTicket(Boolean rejectInvalidTicket) {
		this.rejectInvalidTicket = rejectInvalidTicket;
	}

	/**
	 * Gets the ticket checkpoint.
	 *
	 * @return the ticket checkpoint
	 */
	public TicketCheckpointType getTicketCheckpoint() {
		if(ticketCheckpoint == null)
			return ticketCheckcaseRule.getTicketCheckpoint();

		return ticketCheckpoint;
	}

	/**
	 * Sets the ticket checkpoint.
	 *
	 * @param ticketCheckpoint the new ticket checkpoint
	 */
	public void setTicketCheckpoint(TicketCheckpointType ticketCheckpoint) {
		this.ticketCheckpoint = ticketCheckpoint;
	}
	
	/**
	 * Gets the parameter rule map for properties.
	 * 
	 * @return the parameter rule map
	 */
	public ItemRuleMap getPropertyItemRuleMap() {
		return propertyItemRuleMap;
	}
	
	/**
	 * Sets the parameter rule map for properties.
	 * 
	 * @param parameterRuleMap the new parameter rule map
	 */
	public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
		this.propertyItemRuleMap = propertyItemRuleMap;
	}

	/**
	 * Adds the parameter rule for property.
	 * 
	 * @param parameterRule the item rule for property
	 */
	public void addPropertyItemRule(ItemRule propertyItemRule) {
		if(propertyItemRuleMap == null) 
			propertyItemRuleMap = new ItemRuleMap();
		
		propertyItemRuleMap.putItemRule(propertyItemRule);
	}
	
	/**
	 * Gets the argument item rule map.
	 *
	 * @return the argument item rule map
	 */
	public ItemRuleMap getArgumentItemRuleMap() {
		return argumentItemRuleMap;
	}

	/**
	 * Sets the argument item rule map.
	 *
	 * @param argumentItemRuleMap the new argument item rule map
	 */
	public void setArgumentItemRuleMap(ItemRuleMap argumentItemRuleMap) {
		this.argumentItemRuleMap = argumentItemRuleMap;
	}

	/**
	 * Adds the item rule for argument.
	 * 
	 * @param parameterRule the item rule for argument
	 */
	public void addArgumentItemRule(ItemRule argumentItemRule) {
		if(argumentItemRuleMap == null) 
			argumentItemRuleMap = new ItemRuleMap();
		
		argumentItemRuleMap.putItemRule(argumentItemRule);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{checkcase=").append(ticketCheckcaseRule);
		sb.append(", rejectInvalidTicket=").append(getRejectInvalidTicket());
		sb.append(", ticketCheckpoint=").append(getTicketCheckpoint());
		sb.append("}");
		
		return sb.toString();
	}
	
}
