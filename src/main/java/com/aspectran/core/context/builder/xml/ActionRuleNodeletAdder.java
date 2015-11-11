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
package com.aspectran.core.context.builder.xml;

import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class ActionNodeletAdder.
 *
 * @author Juho Jeong
 * @since 2011. 1. 9.
 */
public class ActionRuleNodeletAdder implements NodeletAdder {
	
	protected ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new content nodelet adder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	public ActionRuleNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.util.xml.NodeletAdder#process(java.lang.String, com.aspectran.core.util.xml.NodeletParser)
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/echo", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

				if(!assistant.isNullableActionId() && StringUtils.isEmpty(id))
					throw new IllegalArgumentException("The <echo> element requires a id attribute.");
				
				EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
				assistant.pushObject(echoActionRule);
				
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet(xpath, "/echo/attribute", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/echo", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/echo/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				EchoActionRule echoActionRule = assistant.popObject();
				
				if(!irm.isEmpty())
					echoActionRule.setAttributeItemRuleMap(irm);

				ActionRuleApplicable applicable = assistant.peekObject();
				applicable.applyActionRule(echoActionRule);
			}
		});
		parser.addNodelet(xpath, "/action", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String beanId = attributes.get("bean");
				String methodName = attributes.get("method");
				Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

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
		parser.addNodelet(xpath, "/action/argument", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/action/argument/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					BeanActionRule beanActionRule = assistant.peekObject();
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
		parser.addNodelet(xpath, "/action/property", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/action/property/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					BeanActionRule beanActionRule = assistant.peekObject();
					beanActionRule.setPropertyItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/action/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				BeanActionRule beanActionRule = assistant.popObject();
				ActionRuleApplicable applicable = assistant.peekObject();
				applicable.applyActionRule(beanActionRule);

				//AspectAdviceRule may not have the bean id.
				if(!StringUtils.isEmpty(beanActionRule.getBeanId()))
					assistant.putBeanReference(beanActionRule.getBeanId(), beanActionRule);
			}
		});
		parser.addNodelet(xpath, "/include", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String transletName = attributes.get("translet");
				Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

				transletName = assistant.applyTransletNamePattern(transletName);
				
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
		parser.addNodelet(xpath, "/include/attribute", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/include/attribute/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					IncludeActionRule includeActionRule = assistant.peekObject();
					includeActionRule.setAttributeItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/include/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				IncludeActionRule includeActionRule = assistant.popObject();
				ActionRuleApplicable applicable = assistant.peekObject();
				applicable.applyActionRule(includeActionRule);
			}
		});
	}
}
