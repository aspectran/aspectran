/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionHandlingRule;
import com.aspectran.core.context.rule.ResponseByContentTypeRule;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class AspectAdviceRuleNodeletAdder.
 *
 * @author Juho Jeong
 * @since 2013. 8. 11.
 */
public class AspectExceptionRaisedNodeletAdder implements NodeletAdder {
	
	protected ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new AspectExceptionRaisedNodeletAdder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	public AspectExceptionRaisedNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.util.xml.NodeletAdder#process(java.lang.String, com.aspectran.core.util.xml.NodeletParser)
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				AspectRule aspectRule = assistant.peekObject();
				
				ExceptionHandlingRule exceptionHandlingRule = new ExceptionHandlingRule();
				aspectRule.setExceptionHandlingRule(exceptionHandlingRule);
				
				assistant.pushObject(exceptionHandlingRule);
			}
		});
		parser.addNodelet(xpath, "/description", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					ExceptionHandlingRule exceptionHandlingRule = assistant.peekObject();
					exceptionHandlingRule.setDescription(text);
				}
			}
		});
		parser.addNodelet(xpath, new ActionRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/responseByContentType", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String exceptionType = attributes.get("exceptionType");

				ResponseByContentTypeRule rbctr = ResponseByContentTypeRule.newInstance(exceptionType);
				assistant.pushObject(rbctr);
			}
		});
		parser.addNodelet(xpath, "/responseByContentType", new ResponseInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/responseByContentType/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = assistant.popObject();
				ExceptionHandlingRule exceptionHandlingRule = assistant.peekObject();
				exceptionHandlingRule.putResponseByContentTypeRule(rbctr);
			}
		});
		parser.addNodelet(xpath, "/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ExceptionHandlingRule exceptionHandlingRule = assistant.popObject();
				AspectRule aspectRule = assistant.peekObject();
				aspectRule.setExceptionHandlingRule(exceptionHandlingRule);
			}
		});
	}

}
