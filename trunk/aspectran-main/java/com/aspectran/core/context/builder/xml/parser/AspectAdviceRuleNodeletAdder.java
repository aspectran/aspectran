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
package com.aspectran.core.context.builder.xml.parser;

import java.util.Properties;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.xml.XmlBuilderAssistant;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.type.AspectAdviceType;

/**
 * The Class AspectAdviceRuleNodeletAdder.
 *
 * @author Gulendol
 * @since 2013. 8. 11.
 */
public class AspectAdviceRuleNodeletAdder implements NodeletAdder {
	
	protected XmlBuilderAssistant assistant;
	
	private AspectAdviceType aspectAdviceType;
	
	/**
	 * Instantiates a new content nodelet adder.
	 * 
	 * @param parser the parser
	 * @param assistant the assistant for Context Builder
	 */
	public AspectAdviceRuleNodeletAdder(XmlBuilderAssistant assistant, AspectAdviceType aspectAdviceType) {
		this.assistant = assistant;
		this.aspectAdviceType = aspectAdviceType;
	}

	/**
	 * Process.
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				AspectRule ar = (AspectRule)assistant.peekObject();
				
				AspectAdviceRule aar = new AspectAdviceRule();
				aar.setAspectId(ar.getId());
				aar.setAdviceBeanId(ar.getAdviceBeanId());
				aar.setAspectAdviceType(aspectAdviceType);
				
				assistant.pushObject(aar);
			}
		});

		parser.addNodelet(xpath, new ActionRuleNodeletAdder(assistant));
		
		parser.addNodelet(xpath, "/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				AspectAdviceRule aar = (AspectAdviceRule)assistant.popObject();
				AspectRule ar = (AspectRule)assistant.peekObject();
				
				ar.addAspectAdviceRule(aar);
			}
		});
	}
}
