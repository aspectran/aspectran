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
package com.aspectran.base.context.builder.xml;

import java.util.Properties;

import org.w3c.dom.Node;

import com.aspectran.base.context.builder.AspectranContextBuilderAssistant;
import com.aspectran.base.rule.ItemRuleMap;
import com.aspectran.base.rule.TicketCheckcaseRule;
import com.aspectran.base.type.TicketCheckpointType;
import com.aspectran.base.util.xml.Nodelet;
import com.aspectran.base.util.xml.NodeletAdder;
import com.aspectran.base.util.xml.NodeletParser;

/**
 * The Class ItemRuleNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class TicketCheckcaseRuleNodeletAdder implements NodeletAdder {
	
	protected AspectranContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new response rule nodelet adder.
	 * 
	 * @param parser the parser
	 * @param assistant the assistant for Context Builder
	 */
	public TicketCheckcaseRuleNodeletAdder(AspectranContextBuilderAssistant assistant) {
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
		parser.addNodelet(xpath, "/checkcase", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = resolveAliasType(attributes.getProperty("id"));
				String rejectInvalidTicket = attributes.getProperty("rejectInvalidTicket");
				String checkpoint = attributes.getProperty("checkpoint");
	
				TicketCheckcaseRule ticketCheckcaseRule = new TicketCheckcaseRule();
				ticketCheckcaseRule.setId(id);
				
				if(rejectInvalidTicket != null)
					ticketCheckcaseRule.setRejectInvalidTicket(Boolean.valueOf(rejectInvalidTicket));
				else
					ticketCheckcaseRule.setRejectInvalidTicket(Boolean.TRUE);
	
				if(checkpoint != null) {
					TicketCheckpointType ticketCheckPoint = TicketCheckpointType.valueOf(checkpoint);
	
					if(ticketCheckPoint == null)
						throw new IllegalArgumentException("No ticket-checkpoint registered for type '" + checkpoint + "'");
					
					ticketCheckcaseRule.setTicketCheckpoint(ticketCheckPoint);
				} else {
					ticketCheckcaseRule.setTicketCheckpoint(TicketCheckpointType.REQUEST);
				}
				
				assistant.pushObject(ticketCheckcaseRule);
			}
		});
		parser.addNodelet(xpath, "/checkcase/action", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String bean = resolveAliasType(attributes.getProperty("bean"));
				String method = attributes.getProperty("method");
				
				TicketCheckcaseRule ticketCheckcaseRule = (TicketCheckcaseRule)assistant.peekObject();
				ticketCheckcaseRule.setBeanId(bean);
				ticketCheckcaseRule.setMethodName(method);
				
//				BeanRule br = assistant.getBeanRuleMap().get(bean);
//				
//				if(br == null)
//					throw new IllegalArgumentException("Unkown bean '" + bean + "'. ticketCheckcaseRule " + ticketCheckcaseRule);
			}
		});
		parser.addNodelet(xpath, "/checkcase/action/arguments", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});

		parser.addNodelet(xpath, "/checkcase/action/arguments", new ItemRuleNodeletAdder(assistant));
	
		parser.addNodelet(xpath, "/checkcase/action/arguments/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					TicketCheckcaseRule ticketCheckcaseRule = (TicketCheckcaseRule)assistant.peekObject();
					ticketCheckcaseRule.setArgumentItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/checkcase/action/properties", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});

		parser.addNodelet(xpath, "/checkcase/action/properties", new ItemRuleNodeletAdder(assistant));
		
		parser.addNodelet(xpath, "/checkcase/action/properties/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					TicketCheckcaseRule ticketCheckcaseRule = (TicketCheckcaseRule)assistant.peekObject();
					ticketCheckcaseRule.setPropertyItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/checkcase/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				TicketCheckcaseRule ticketCheckcaseRule = (TicketCheckcaseRule)assistant.popObject();
				assistant.addTicketCheckcaseRule(ticketCheckcaseRule);
			}
		});
	}
}
