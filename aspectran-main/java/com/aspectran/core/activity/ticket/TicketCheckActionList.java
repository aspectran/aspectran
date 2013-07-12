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
package com.aspectran.core.activity.ticket;

import java.util.ArrayList;
import java.util.Iterator;

import com.aspectran.base.rule.TicketCheckRule;
import com.aspectran.base.type.TicketCheckpointType;
import com.aspectran.core.activity.ticket.action.TicketCheckAction;

/**
 * The action list class.
 * 
 * <p>Created: 2008. 03. 23 오전 1:38:14</p>
 */
public class TicketCheckActionList extends ArrayList<TicketCheckAction> {
	
	/** @serial */
	static final long serialVersionUID = 6557886453403106858L;
	
	/**
	 * Instantiates a new action list.
	 * 
	 * @param parent the action list
	 */
	public TicketCheckActionList() {
		super();
	}

	public TicketCheckActionList(TicketCheckActionList ticketCheckActionList) {
		super(ticketCheckActionList);
	}
	
	/**
	 * Adds the ticket bean action.
	 *
	 * @param ticketCheckRule the ticket bean action rule
	 * @throws ClassNotFoundException the class not found exception
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws NoSuchMethodException the no such method exception
	 */
	public void addTicketCheckAction(TicketCheckRule ticketCheckRule) {
		TicketCheckAction ticketBeanAction = new TicketCheckAction(ticketCheckRule);

		add(ticketBeanAction);
	}

	/**
	 * Gets the request check point count.
	 *
	 * @return the request check point count
	 */
	public int getRequestCheckpointCount() {
		int count = 0;
		
		for(Iterator<TicketCheckAction> iter = super.iterator(); iter.hasNext(); ) {
			if(iter.next().getTicketCheckRule().getTicketCheckpoint() == TicketCheckpointType.REQUEST)
				count++;
		}
		
		return count;
	}

	/**
	 * Gets the response check point count.
	 *
	 * @return the response check point count
	 */
	public int getResponseCheckpointCount() {
		int count = 0;
		
		for(Iterator<TicketCheckAction> iter = super.iterator(); iter.hasNext(); ) {
			if(iter.next().getTicketCheckRule().getTicketCheckpoint() == TicketCheckpointType.RESPONSE)
				count++;
		}
		
		return count;
	}
	
}
