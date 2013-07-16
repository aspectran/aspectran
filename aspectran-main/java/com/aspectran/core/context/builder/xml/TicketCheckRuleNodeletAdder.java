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
package com.aspectran.core.context.builder.xml;

import java.util.Properties;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.AspectranSettingAssistant;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.rule.TicketCheckRule;
import com.aspectran.core.rule.TicketCheckcaseRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.type.TicketCheckpointType;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class ItemRuleNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class TicketCheckRuleNodeletAdder implements NodeletAdder {
	
	protected AspectranSettingAssistant assistant;
	
	/**
	 * Instantiates a new response rule nodelet adder.
	 * 
	 * @param parser the parser
	 * @param assistant the assistant for Context Builder
	 */
	public TicketCheckRuleNodeletAdder(AspectranSettingAssistant assistant) {
		this.assistant = assistant;
	}

	/**
	 * Returns the resolve alias type.
	 * 
	 * @param alias the alias
	 * 
	 * @return the string
	 */
	protected String resolveAliasType(String alias) {
		String type = assistant.getAliasType(alias);

		if(type == null)
			return alias;

		return type;
	}
	
	/**
	 * Process.
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/ticket", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String checkcase = resolveAliasType(attributes.getProperty("checkcase"));
				String rejectInvalidTicket = attributes.getProperty("rejectInvalidTicket");
				String checkpoint = attributes.getProperty("checkpoint");
	
				TicketCheckcaseRule ticketCheckcaseRule = assistant.getTicketCheckcaseRule(checkcase);
				
				if(ticketCheckcaseRule == null)
					throw new IllegalArgumentException("Unkown ticket checkcase rule '" + checkcase + "'");
				
				TicketCheckRule ticketCheckRule = new TicketCheckRule(ticketCheckcaseRule);
				
				if(rejectInvalidTicket != null)
					ticketCheckRule.setRejectInvalidTicket(Boolean.valueOf(rejectInvalidTicket));
				else
					ticketCheckRule.setRejectInvalidTicket(Boolean.TRUE);
	
				if(checkpoint != null) {
					TicketCheckpointType ticketCheckPoint = TicketCheckpointType.valueOf(checkpoint);
	
					if(ticketCheckPoint == null)
						throw new IllegalArgumentException("No ticket-checkpoint registered for type '" + checkpoint + "'");
					
					ticketCheckRule.setTicketCheckpoint(ticketCheckPoint);
				} else {
					ticketCheckRule.setTicketCheckpoint(TicketCheckpointType.REQUEST);
				}
				
				assistant.pushObject(ticketCheckRule);
			}
		});
	
		parser.addNodelet(xpath, "/ticket/arguments", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
	
		parser.addNodelet(xpath, "/ticket/arguments", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/ticket/arguments/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					TicketCheckRule ticketCheckRule = (TicketCheckRule)assistant.peekObject();
					ticketCheckRule.setArgumentItemRuleMap(irm);
				}
			}
		});
	
		parser.addNodelet(xpath, "/ticket/properties", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});

		parser.addNodelet(xpath, "/ticket/properties", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/ticket/properties/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					TicketCheckRule ticketCheckRule = (TicketCheckRule)assistant.peekObject();
					ticketCheckRule.setPropertyItemRuleMap(irm);
				}
			}
		});

		if(xpath.endsWith("/translet/request")) {
			parser.addNodelet(xpath, "/ticket/end()", new Nodelet() {
				public void process(Node node, Properties attributes, String text) throws Exception {
					TicketCheckRule ticketCheckRule = (TicketCheckRule)assistant.popObject();
					ticketCheckRule.setTicketCheckpoint(TicketCheckpointType.REQUEST);
					Object o = assistant.popObject();
					TransletRule transletRule = (TransletRule)assistant.peekObject();
					assistant.pushObject(o);
					transletRule.addTicketCheckAction(ticketCheckRule);
				}
			});
		} else if(xpath.endsWith("/translet/response")) {
			parser.addNodelet(xpath, "/ticket/end()", new Nodelet() {
				public void process(Node node, Properties attributes, String text) throws Exception {
					TicketCheckRule ticketCheckRule = (TicketCheckRule)assistant.popObject();
					ticketCheckRule.setTicketCheckpoint(TicketCheckpointType.RESPONSE);
					Object o = assistant.popObject();
					TransletRule transletRule = (TransletRule)assistant.peekObject();
					assistant.pushObject(o);
					transletRule.addTicketCheckAction(ticketCheckRule);
				}
			});
		} else if(xpath.endsWith("/translet/requestRule")) {
			parser.addNodelet(xpath, "/ticket/end()", new Nodelet() {
				public void process(Node node, Properties attributes, String text) throws Exception {
					TicketCheckRule ticketCheckRule = (TicketCheckRule)assistant.popObject();
					ticketCheckRule.setTicketCheckpoint(TicketCheckpointType.REQUEST);
					assistant.addTicketCheckAction(ticketCheckRule);
				}
			});
		} else if(xpath.endsWith("/translet/responseRule")) {
			parser.addNodelet(xpath, "/ticket/end()", new Nodelet() {
				public void process(Node node, Properties attributes, String text) throws Exception {
					TicketCheckRule ticketCheckRule = (TicketCheckRule)assistant.popObject();
					ticketCheckRule.setTicketCheckpoint(TicketCheckpointType.RESPONSE);
					assistant.addTicketCheckAction(ticketCheckRule);
				}
			});
		} else {
			parser.addNodelet(xpath, "/ticket/end()", new Nodelet() {
				public void process(Node node, Properties attributes, String text) throws Exception {
					TicketCheckRule ticketCheckRule = (TicketCheckRule)assistant.popObject();
					TransletRule transletRule = (TransletRule)assistant.peekObject();
					transletRule.addTicketCheckAction(ticketCheckRule);
				}
			});
		}
	}
}
