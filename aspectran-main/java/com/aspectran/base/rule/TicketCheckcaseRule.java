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
package com.aspectran.base.rule;

import com.aspectran.base.type.TicketCheckpointType;

/**
 * <p>Created: 2008. 03. 27 오후 5:24:20</p>
 */
public class TicketCheckcaseRule extends BeanActionRule {

	private Boolean rejectInvalidTicket;
	
	private TicketCheckpointType ticketCheckpoint;
	
	private ResponseByContentTypeRule responseByContentTypeRule;
	
	/**
	 * Instantiates a new ticket rule.
	 */
	public TicketCheckcaseRule() {
		super();
	}

	/**
	 * Gets the reject invalid ticket.
	 * 
	 * @return the reject invalid ticket
	 */
	public Boolean getRejectInvalidTicket() {
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
	 * Gets the response by content type.
	 * 
	 * @return the responseByContentType
	 */
	public ResponseByContentTypeRule getResponseByContentTypeRule() {
		return responseByContentTypeRule;
	}

	/**
	 * Sets the response by content type.
	 * 
	 * @param responseByContentTypeRule the responseByContentType to set
	 */
	public void setResponseByContentTypeRule(ResponseByContentTypeRule responseByContentTypeRule) {
		this.responseByContentTypeRule = responseByContentTypeRule;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{bean=").append(beanId);
		sb.append(", method=").append(methodName);
		sb.append(", rejectInvalidTicket=").append(rejectInvalidTicket);
		sb.append(", checkpoint=").append(ticketCheckpoint);

		if(propertyItemRuleMap != null) {
			sb.append(", properties=[");
			int sbLength = sb.length();

			for(String name : propertyItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}

			sb.append("]");
		}
		
		if(argumentItemRuleMap != null) {
			sb.append(", arguments=[");
			int sbLength = sb.length();
			
			for(String name : argumentItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}
			
			sb.append("]");
		}
		
		sb.append("}");
		
		return sb.toString();
	}
}
