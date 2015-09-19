/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.builder.xml;

import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class BeanInnerNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class BeanInnerNodeletAdder implements NodeletAdder {
	
	protected ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new response rule nodelet adder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	public BeanInnerNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	/**
	 * Process.
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/features/class", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setClassName(text);
				}
			}
		});
		parser.addNodelet(xpath, "/features/scope", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					ScopeType scopeType = ScopeType.valueOf(text);
					
					if(scopeType == null)
						throw new IllegalArgumentException("No scope-type registered for scope '" + text + "'.");
					
					BeanRule beanRule = assistant.peekObject();
					beanRule.setScopeType(scopeType);
					
					if(scopeType == ScopeType.SINGLETON)
						beanRule.setSingleton(Boolean.TRUE);
				}
			}
		});
		parser.addNodelet(xpath, "/features/factoryMethod", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setFactoryMethodName(text);
				}
			}
		});
		parser.addNodelet(xpath, "/features/initMethod", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setInitMethodName(text);
				}
			}
		});
		parser.addNodelet(xpath, "/features/destroyMethod", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setDestroyMethodName(text);
				}
			}
		});
		parser.addNodelet(xpath, "/features/lazyInit", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					Boolean lazyInit = BooleanUtils.toBooleanObject(text);
					BeanRule beanRule = assistant.peekObject();
					beanRule.setLazyInit(lazyInit);
				}
			}
		});
		parser.addNodelet(xpath, "/features/important", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					Boolean important = BooleanUtils.toBooleanObject(text);
					BeanRule beanRule = assistant.peekObject();
					beanRule.setImportant(important);
				}
			}
		});
		parser.addNodelet(xpath, "/constructor/argument", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					BeanRule beanRule = assistant.peekObject();
					BeanRule.updateConstructorArgument(beanRule, text);
				}
				
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet(xpath, "/constructor/argument", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/constructor/argument/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setConstructorArgumentItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/property", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					BeanRule beanRule = assistant.peekObject();
					BeanRule.updateProperty(beanRule, text);
				}
				
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet(xpath, "/property", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/property/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setPropertyItemRuleMap(irm);
				}
			}
		});
	}

}