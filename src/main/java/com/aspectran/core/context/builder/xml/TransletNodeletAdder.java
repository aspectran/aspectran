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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.rule.ExceptionHandlingRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseByContentTypeRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class TransletNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class TransletNodeletAdder implements NodeletAdder {
	
	protected ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new translet nodelet adder.
	 *
	 * @param assistant the assistant
	 */
	public TransletNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.util.xml.NodeletAdder#process(java.lang.String, com.aspectran.core.util.xml.NodeletParser)
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/translet", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				String restVerb = attributes.get("restVerb");
				
				TransletRule transletRule = TransletRule.newInstance(name, restVerb);
				assistant.pushObject(transletRule);
			}
		});
		parser.addNodelet(xpath, "/translet", new ActionRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet", new ResponseInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/request", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String method = attributes.get("method");
				String characterEncoding = attributes.get("characterEncoding");

				RequestRule requestRule = RequestRule.newInstance(method, characterEncoding);
				assistant.pushObject(requestRule);
			}
		});
		parser.addNodelet(xpath, "/translet/request/attribute", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet(xpath, "/translet/request/attribute", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/request/attribute/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					RequestRule requestRule = assistant.peekObject();
					requestRule.setAttributeItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/translet/request/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				RequestRule requestRule = assistant.popObject();
				TransletRule transletRule = assistant.peekObject();
				transletRule.setRequestRule(requestRule);
			}
		});
		parser.addNodelet(xpath, "/translet/contents", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				Boolean omittable = BooleanUtils.toNullableBooleanObject(attributes.get("omittable"));
				
				ContentList contentList = ContentList.newInstance(name, omittable);
				assistant.pushObject(contentList);
			}
		});
		parser.addNodelet(xpath, "/translet/contents/content", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String name = attributes.get("name");
				Boolean omittable = BooleanUtils.toNullableBooleanObject(attributes.get("omittable"));
				Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

				if(!assistant.isNullableContentId() && StringUtils.isEmpty(id))
					throw new IllegalArgumentException("The <content> element requires a id attribute.");

				ContentList contentList = assistant.peekObject();
				
				ActionList actionList = ActionList.newInstance(id, name, omittable, hidden, contentList);
				assistant.pushObject(actionList);
			}
		});
		parser.addNodelet(xpath, "/translet/contents/content", new ActionRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/contents/content/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = assistant.popObject();
				
				if(!actionList.isEmpty()) {
					ContentList contentList = assistant.peekObject();
					contentList.addActionList(actionList);
				}
			}
		});
		parser.addNodelet(xpath, "/translet/contents/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ContentList contentList = assistant.popObject();
				
				if(!contentList.isEmpty()) {
					TransletRule transletRule = assistant.peekObject();
					transletRule.setContentList(contentList);
				}
			}
		});
		parser.addNodelet(xpath, "/translet/content", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String name = attributes.get("name");
				Boolean omittable = BooleanUtils.toNullableBooleanObject(attributes.get("omittable"));
				Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

				if(!assistant.isNullableContentId() && StringUtils.isEmpty(id))
					throw new IllegalArgumentException("The <content> element requires a id attribute.");

				TransletRule transletRule = assistant.peekObject();

				ContentList contentList = transletRule.touchContentList(true);
				assistant.pushObject(contentList);
				
				ActionList actionList = ActionList.newInstance(id, name, omittable, hidden, contentList);
				assistant.pushObject(actionList);
			}
		});
		parser.addNodelet(xpath, "/translet/content", new ActionRuleNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/content/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = assistant.popObject();
				
				if(!actionList.isEmpty()) {
					ContentList contentList = assistant.popObject();
					contentList.addActionList(actionList);
				}
			}
		});
		parser.addNodelet(xpath, "/translet/response", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				String characterEncoding = attributes.get("characterEncoding");

				ResponseRule responseRule = ResponseRule.newInstance(name, characterEncoding);
				assistant.pushObject(responseRule);
			}
		});
		parser.addNodelet(xpath, "/translet/response", new ResponseInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/response/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ResponseRule responseRule = assistant.popObject();
				TransletRule transletRule = assistant.peekObject();
				transletRule.addResponseRule(responseRule);
			}
		});
		parser.addNodelet(xpath, "/translet/exception/responseByContentType", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String exceptionType = attributes.get("exceptionType");

				ResponseByContentTypeRule rbctr = ResponseByContentTypeRule.newInstance(exceptionType);
				assistant.pushObject(rbctr);
			}
		});
		parser.addNodelet(xpath, "/translet/exception/responseByContentType", new ResponseInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/exception/responseByContentType/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = assistant.popObject();
				TransletRule transletRule = assistant.peekObject();
				
				ExceptionHandlingRule exceptionHandlingRule = transletRule.touchExceptionHandlingRule();
				exceptionHandlingRule.putResponseByContentTypeRule(rbctr);
			}
		});
		parser.addNodelet(xpath, "/translet/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				TransletRule transletRule = assistant.popObject();
				assistant.addTransletRule(transletRule);
			}
		});
	}

}