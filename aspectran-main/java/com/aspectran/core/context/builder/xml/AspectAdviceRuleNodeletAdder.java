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

import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.context.builder.AspectranContextBuildingAssistant;
import com.aspectran.core.rule.AspectAdviceRule;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.ResponseByContentTypeRule;
import com.aspectran.core.rule.ResponseByContentTypeRuleMap;
import com.aspectran.core.type.AspectAdviceType;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class AspectAdviceRuleNodeletAdder.
 *
 * @author Gulendol
 * @since 2013. 8. 11.
 */
public class AspectAdviceRuleNodeletAdder implements NodeletAdder {
	
	protected AspectranContextBuildingAssistant assistant;
	
	private AspectAdviceType aspectAdviceType;
	
	/**
	 * Instantiates a new content nodelet adder.
	 * 
	 * @param parser the parser
	 * @param assistant the assistant for Context Builder
	 */
	public AspectAdviceRuleNodeletAdder(AspectranContextBuildingAssistant assistant, AspectAdviceType aspectAdviceType) {
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
				aar.setAspectAdviceType(aspectAdviceType);
				
				assistant.pushObject(aar);
			}
		});

		parser.addNodelet(xpath, new ActionRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/responseByContentType", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String exceptionType = attributes.getProperty("exceptionType");

				ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
				rbctr.setExceptionType(exceptionType);
				
				assistant.pushObject(rbctr);
			}
		});
		
		parser.addNodelet(xpath, "/responseByContentType", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/responseByContentType/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = (ResponseByContentTypeRule)assistant.popObject();
				AspectAdviceRule aar = (AspectAdviceRule)assistant.peekObject();
				aar.addResponseByContentTypeRule(rbctr);
			}
		});

		parser.addNodelet(xpath, "/defaultResponse", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
				assistant.pushObject(rbctr);
			}
		});
		
		parser.addNodelet(xpath, "/defaultResponse", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/defaultResponse/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = (ResponseByContentTypeRule)assistant.popObject();
				ResponseMap responseMap = rbctr.getResponseMap();
				
				if(responseMap.size() > 0) {
					AspectAdviceRule aar = (AspectAdviceRule)assistant.peekObject();
					ResponseByContentTypeRuleMap rbctrm = aar.getResponseByContentTypeRuleMap();
					
					if(rbctrm == null)
						rbctrm = new ResponseByContentTypeRuleMap();
					
					ResponseByContentTypeRule responseByContentTypeRule = new ResponseByContentTypeRule();
					responseByContentTypeRule.setDefaultResponse(responseMap.get(0));
					
					rbctrm.putResponseByContentTypeRule(responseByContentTypeRule);
					aar.setResponseByContentTypeRuleMap(rbctrm);
				}
			}
		});
		
		parser.addNodelet(xpath, "/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				AspectAdviceRule aar = (AspectAdviceRule)assistant.popObject();
				AspectRule ar = (AspectRule)assistant.peekObject();
				
				ar.addAspectAdviceRule(aar);
			}
		});
	}
}
