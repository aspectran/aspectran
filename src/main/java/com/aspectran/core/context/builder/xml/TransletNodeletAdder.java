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
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class TransletNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class TransletNodeletAdder implements NodeletAdder {
	
	protected final ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new TransletNodeletAdder.
	 *
	 * @param assistant the assistant
	 */
	TransletNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	@Override
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/translet", (node, attributes, text) -> {
            String name = attributes.get("name");
            String scan = attributes.get("scan");
            String mask = attributes.get("mask");
            String method = attributes.get("method");

            if(name == null && scan == null)
                throw new IllegalArgumentException("The <translet> element requires a name attribute.");

            TransletRule transletRule = TransletRule.newInstance(name, scan, mask, method);
            assistant.pushObject(transletRule);
        });
		parser.addNodelet(xpath, "/translet/description", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					TransletRule transletRule = assistant.peekObject();
					transletRule.setDescription(text);
				}
			}
		});
		parser.addNodelet(xpath, "/translet", new ActionNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet", new ResponseInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/request", (node, attributes, text) -> {
            String method = attributes.get("method");
            String characterEncoding = attributes.get("characterEncoding");

            RequestRule requestRule = RequestRule.newInstance(method, characterEncoding);
            assistant.pushObject(requestRule);
        });
		parser.addNodelet(xpath, "/translet/request/attribute", (node, attributes, text) -> {
            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
		parser.addNodelet(xpath, "/translet/request/attribute", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/request/attribute/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if(!irm.isEmpty()) {
                RequestRule requestRule = assistant.peekObject();
                requestRule.setAttributeItemRuleMap(irm);
            }
        });
		parser.addNodelet(xpath, "/translet/request/end()", (node, attributes, text) -> {
            RequestRule requestRule = assistant.popObject();
            TransletRule transletRule = assistant.peekObject();
            transletRule.setRequestRule(requestRule);
        });
		parser.addNodelet(xpath, "/translet/contents", (node, attributes, text) -> {
            String name = attributes.get("name");
            Boolean omittable = BooleanUtils.toNullableBooleanObject(attributes.get("omittable"));

            ContentList contentList = ContentList.newInstance(name, omittable);
            assistant.pushObject(contentList);
        });
		parser.addNodelet(xpath, "/translet/contents/content", (node, attributes, text) -> {
            String name = attributes.get("name");
            Boolean omittable = BooleanUtils.toNullableBooleanObject(attributes.get("omittable"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

            ActionList actionList = ActionList.newInstance(name, omittable, hidden);
            assistant.pushObject(actionList);
        });
		parser.addNodelet(xpath, "/translet/contents/content", new ActionNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/contents/content/end()", (node, attributes, text) -> {
            ActionList actionList = assistant.popObject();

            if(!actionList.isEmpty()) {
                ContentList contentList = assistant.peekObject();
                contentList.addActionList(actionList);
            }
        });
		parser.addNodelet(xpath, "/translet/contents/end()", (node, attributes, text) -> {
            ContentList contentList = assistant.popObject();

            if(!contentList.isEmpty()) {
                TransletRule transletRule = assistant.peekObject();
                transletRule.setContentList(contentList);
            }
        });
		parser.addNodelet(xpath, "/translet/content", (node, attributes, text) -> {
            String name = attributes.get("name");
            Boolean omittable = BooleanUtils.toNullableBooleanObject(attributes.get("omittable"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

            ContentList contentList = ContentList.newInstance(name, Boolean.TRUE);
            ActionList actionList = ActionList.newInstance(name, omittable, hidden);

            assistant.pushObject(contentList);
            assistant.pushObject(actionList);
        });
		parser.addNodelet(xpath, "/translet/content", new ActionNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/content/end()", (node, attributes, text) -> {
            ActionList actionList = assistant.popObject();
            ContentList contentList = assistant.popObject();

            if(!actionList.isEmpty()) {
                TransletRule transletRule = assistant.peekObject();
                contentList.addActionList(actionList);
                transletRule.setContentList(contentList);
            }
        });
		parser.addNodelet(xpath, "/translet/response", (node, attributes, text) -> {
            String name = attributes.get("name");
            String characterEncoding = attributes.get("characterEncoding");

            ResponseRule responseRule = ResponseRule.newInstance(name, characterEncoding);
            assistant.pushObject(responseRule);
        });
		parser.addNodelet(xpath, "/translet/response", new ResponseInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/response/end()", (node, attributes, text) -> {
            ResponseRule responseRule = assistant.popObject();
            TransletRule transletRule = assistant.peekObject();
            transletRule.addResponseRule(responseRule);
        });
		parser.addNodelet(xpath, "/translet/exception/responseByContentType", (node, attributes, text) -> {
            String exceptionType = attributes.get("exceptionType");

            ResponseByContentTypeRule rbctr = ResponseByContentTypeRule.newInstance(exceptionType);
            assistant.pushObject(rbctr);
        });
		parser.addNodelet(xpath, "/translet/exception/responseByContentType", new ResponseInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/translet/exception/responseByContentType/end()", (node, attributes, text) -> {
            ResponseByContentTypeRule rbctr = assistant.popObject();
            TransletRule transletRule = assistant.peekObject();

            ExceptionHandlingRule exceptionHandlingRule = transletRule.touchExceptionHandlingRule();
            exceptionHandlingRule.putResponseByContentTypeRule(rbctr);
        });
		parser.addNodelet(xpath, "/translet/end()", (node, attributes, text) -> {
            TransletRule transletRule = assistant.popObject();
            assistant.addTransletRule(transletRule);
        });
	}

}