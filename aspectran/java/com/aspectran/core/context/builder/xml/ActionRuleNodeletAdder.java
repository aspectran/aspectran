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

import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.BeanActionRule;
import com.aspectran.core.var.rule.EchoActionRule;
import com.aspectran.core.var.rule.IncludeActionRule;
import com.aspectran.core.var.rule.ItemRuleMap;
import com.aspectran.core.var.rule.ability.ActionAddable;
import com.aspectran.core.var.rule.ability.ActionSettable;

/**
 * The Class ActionNodeletAdder.
 *
 * @author Gulendol
 * @since 2011. 1. 9.
 */
public class ActionRuleNodeletAdder implements NodeletAdder {
	
	protected ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new content nodelet adder.
	 * 
	 * @param parser the parser
	 * @param assistant the assistant for Context Builder
	 */
	public ActionRuleNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	/**
	 * Process.
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/echo", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				boolean hidden = Boolean.parseBoolean(attributes.get("hidden"));

				if(!assistant.isNullableActionId() && StringUtils.isEmpty(id))
					throw new IllegalArgumentException("The <echo> element requires a id attribute.");
				
				EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);

				assistant.pushObject(echoActionRule);
				
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet(xpath, "/echo/attribute", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/echo", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/echo/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				EchoActionRule echoActionRule = (EchoActionRule)assistant.popObject();
				
				if(irm.size() > 0)
					echoActionRule.setAttributeItemRuleMap(irm);

				Object o = assistant.peekObject();
				
				if(o instanceof ActionSettable) {
					ActionSettable settable = (AspectAdviceRule)o;
					settable.setEchoAction(echoActionRule);
				} else if(o instanceof ActionAddable) {
					ActionAddable addable = (ActionAddable)o;
					addable.addEchoAction(echoActionRule);
				}
			}
		});
		parser.addNodelet(xpath, "/action", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String beanId = attributes.get("bean");
				String methodName = attributes.get("method");
				boolean hidden = Boolean.parseBoolean(attributes.get("hidden"));

				if(!assistant.isNullableActionId() && StringUtils.isEmpty(id))
					throw new IllegalArgumentException("The <action> element requires a id attribute.");
				
				BeanActionRule beanActionRule = BeanActionRule.newInstance(id, beanId, methodName, hidden);

				assistant.pushObject(beanActionRule);
			}
		});
		parser.addNodelet(xpath, "/action/argument", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet(xpath, "/action/argument", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/action/argument/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					BeanActionRule beanActionRule = (BeanActionRule)assistant.peekObject();
					beanActionRule.setArgumentItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/action/property", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet(xpath, "/action/property", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/action/property/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					BeanActionRule beanActionRule = (BeanActionRule)assistant.peekObject();
					beanActionRule.setPropertyItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/action/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				BeanActionRule beanActionRule = (BeanActionRule)assistant.popObject();

				Object o = assistant.peekObject();
				
				if(o instanceof ActionSettable) {
					ActionSettable settable = (AspectAdviceRule)o;
					settable.setBeanAction(beanActionRule);
					
					//AspectAdviceRule may not have the bean id.
					if(beanActionRule.getBeanId() != null)
						assistant.putBeanReference(beanActionRule.getBeanId(), beanActionRule);
				} else if(o instanceof ActionAddable) {
					ActionAddable addable = (ActionAddable)o;
					addable.addBeanAction(beanActionRule);
					
					assistant.putBeanReference(beanActionRule.getBeanId(), beanActionRule);
				}
			}
		});
		parser.addNodelet(xpath, "/include", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String transletName = attributes.get("translet");
				boolean hidden = Boolean.parseBoolean(attributes.get("hidden"));

				transletName = assistant.getFullTransletName(transletName);
				
				if(!assistant.isNullableActionId() && StringUtils.isEmpty(id))
					throw new IllegalArgumentException("The <include> element requires a id attribute.");
				
				IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, transletName, hidden);

				assistant.pushObject(includeActionRule);
			}
		});

		parser.addNodelet(xpath, "/include/attribute", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet(xpath, "/include/attribute", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/include/attribute/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					IncludeActionRule includeActionRule = (IncludeActionRule)assistant.peekObject();
					includeActionRule.setAttributeItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/include/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				IncludeActionRule includeActionRule = (IncludeActionRule)assistant.popObject();
				
				Object o = assistant.peekObject();
				
				if(o instanceof ActionSettable) {
					//There is nothing that can be set to IncludeAction.
				} else if(o instanceof ActionAddable) {
					ActionAddable addable = (ActionAddable)o;
					addable.addIncludeAction(includeActionRule);
				}
			}
		});
	}
}
