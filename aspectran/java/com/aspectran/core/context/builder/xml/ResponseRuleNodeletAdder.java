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

import java.io.File;
import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;
import com.aspectran.core.var.rule.DispatchResponseRule;
import com.aspectran.core.var.rule.ForwardResponseRule;
import com.aspectran.core.var.rule.ItemRuleMap;
import com.aspectran.core.var.rule.RedirectResponseRule;
import com.aspectran.core.var.rule.TransformRule;
import com.aspectran.core.var.rule.ability.ResponseAddable;
import com.aspectran.core.var.rule.ability.ResponseSettable;
import com.aspectran.core.var.token.Token;
import com.aspectran.core.var.type.TokenType;

/**
 * The Class ResponseRuleNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class ResponseRuleNodeletAdder implements NodeletAdder {
	
	protected ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new response rule nodelet adder.
	 * 
	 * @param parser the parser
	 * @param assistant the assistant for Context Builder
	 */
	public ResponseRuleNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}
	
	/**
	 * Process.
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/transform", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String typeString = attributes.get("type");
				String contentType = attributes.get("contentType");
				String characterEncoding = attributes.get("characterEncoding");

				TransformRule tr = TransformRule.newInstance(typeString, contentType, characterEncoding);

				assistant.pushObject(tr);
				
				ActionList actionList = new ActionList();
				assistant.pushObject(actionList);
			}
		});
		
		parser.addNodelet(xpath, "/transform", new ActionRuleNodeletAdder(assistant));
		
		parser.addNodelet(xpath, "/transform/template", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String resource = attributes.get("resource");
				String filePath = attributes.get("file");
				String templateUrl = attributes.get("url");
				String templateContent = text;
				String encoding = attributes.get("encoding");
				boolean noCache = Boolean.parseBoolean(attributes.get("noCache"));

				File templateFile = null;

				if(StringUtils.hasText(resource))
					templateFile = ResourceUtils.getResourceAsFile(resource);
				else if(StringUtils.hasText(filePath))
					templateFile = assistant.toRealPathFile(filePath);

				TransformRule tr = (TransformRule)assistant.peekObject(1);
				
				if(StringUtils.hasText(encoding))
					tr.setTemplateEncoding(encoding);
				
				TransformRule.updateTemplate(tr, templateFile, templateUrl, templateContent, encoding, noCache);
				
				if(tr.getContentTokens() != null) {
					for(Token token : tr.getContentTokens()) {
						if(token.getType() == TokenType.REFERENCE_BEAN) {
							assistant.putBeanReference(token.getName(), tr);
						}
					}
				}
			}
		});
		parser.addNodelet(xpath, "/transform/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				TransformRule tr = (TransformRule)assistant.popObject();
				
				if(!actionList.isEmpty())
					tr.setActionList(actionList);
				
				Object o = assistant.peekObject();
				
				if(o instanceof ResponseSettable) {
					ResponseSettable settable = (ResponseSettable)o; //TransletRule, ResponseRule
					settable.setResponse(tr);
				} else if(o instanceof ResponseAddable) {
					ResponseAddable addable = (ResponseAddable)o; //ResponseByContentTypeRule
					addable.addResponse(tr);
				}
			}
		});
		parser.addNodelet(xpath, "/dispatch", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String contentType = attributes.get("contentType");
				String encoding = attributes.get("encoding");
				
				DispatchResponseRule drr = DispatchResponseRule.newInstance(contentType, encoding);
				
				assistant.pushObject(drr);

				ActionList actionList = new ActionList();
				assistant.pushObject(actionList);
			}
		});

		parser.addNodelet(xpath, "/dispatch", new ActionRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/dispatch/template", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String templateFile = attributes.get("file");
				String templateContent = text;
				String templateEncoding = attributes.get("encoding");
				boolean noCache = Boolean.parseBoolean(attributes.get("noCache"));
				
				DispatchResponseRule drr = (DispatchResponseRule)assistant.peekObject(1);
				DispatchResponseRule.updateTemplate(drr, templateFile, templateContent, templateEncoding, noCache);
			}
		});
		parser.addNodelet(xpath, "/dispatch/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				DispatchResponseRule drr = (DispatchResponseRule)assistant.popObject();
				
				if(!actionList.isEmpty())
					drr.setActionList(actionList);
				
				Object o = assistant.peekObject();
				
				if(o instanceof ResponseSettable) {
					ResponseSettable settable = (ResponseSettable)o; //TransletRule, ResponseRule
					settable.setResponse(drr);
				} else if(o instanceof ResponseAddable) {
					ResponseAddable addable = (ResponseAddable)o; //ResponseByContentTypeRule
					addable.addResponse(drr);
				}
			}
		});
		parser.addNodelet(xpath, "/redirect", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String contentType = attributes.get("contentType"); // ResponseByContentType에서 content type에 따라 분기
				String translet = attributes.get("translet");
				String url = attributes.get("url");
				boolean excludeNullParameters = Boolean.parseBoolean(attributes.get("excludeNullParameters"));
				
				RedirectResponseRule rrr = RedirectResponseRule.newInstance(contentType, translet, url, excludeNullParameters);
				
				assistant.pushObject(rrr);
				
				ActionList actionList = new ActionList();
				assistant.pushObject(actionList);
			}
		});

		parser.addNodelet(xpath, "/redirect", new ActionRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/redirect/url", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				RedirectResponseRule rrr = (RedirectResponseRule)assistant.peekObject(1);
				RedirectResponseRule.updateUrl(rrr, text);
			}
		});
		parser.addNodelet(xpath, "/redirect/parameter", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		
		parser.addNodelet(xpath, "/redirect/parameter", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/redirect/parameter/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					RedirectResponseRule rrr = (RedirectResponseRule)assistant.peekObject(1);
					rrr.setParameterItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/redirect/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				RedirectResponseRule rrr = (RedirectResponseRule)assistant.popObject();
				
				if(rrr.getTransletName() == null && rrr.getUrl() == null)
					throw new IllegalArgumentException("The <redirect> element requires either a translet or a url attribute.");

				if(!actionList.isEmpty())
					rrr.setActionList(actionList);
				
				Object o = assistant.peekObject();
				
				if(o instanceof ResponseSettable) {
					ResponseSettable settable = (ResponseSettable)o; //TransletRule, ResponseRule
					settable.setResponse(rrr);
				} else if(o instanceof ResponseAddable) {
					ResponseAddable addable = (ResponseAddable)o; //ResponseByContentTypeRule
					addable.addResponse(rrr);
				}
				
				if(rrr.getUrlTokens() != null) {
					for(Token token : rrr.getUrlTokens()) {
						if(token.getType() == TokenType.REFERENCE_BEAN) {
							assistant.putBeanReference(token.getName(), rrr);
						}
					}
				}
			}
		});
		parser.addNodelet(xpath, "/forward", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String contentType = attributes.get("contentType");
				String transletName = attributes.get("translet");
				
				transletName = assistant.getFullTransletName(transletName);
				
				ForwardResponseRule frr = ForwardResponseRule.newInstance(contentType, transletName);
				
				assistant.pushObject(frr);

				ActionList actionList = new ActionList();
				assistant.pushObject(actionList);
			}
		});

		parser.addNodelet(xpath, "/forward", new ActionRuleNodeletAdder(assistant));
		
		parser.addNodelet(xpath, "/forward/parameter", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});

		parser.addNodelet(xpath, "/forward/parameter", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/forward/parameter/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					ForwardResponseRule frr = (ForwardResponseRule)assistant.peekObject(1);
					frr.setParameterItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/forward/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				ForwardResponseRule frr = (ForwardResponseRule)assistant.popObject();
				
				if(frr.getParameterItemRuleMap() == null)
					throw new IllegalArgumentException("The <forward> element requires a path attribute.");

				if(!actionList.isEmpty())
					frr.setActionList(actionList);
				
				Object o = assistant.peekObject();
				
				if(o instanceof ResponseSettable) {
					ResponseSettable settable = (ResponseSettable)o; //TransletRule, ResponseRule
					settable.setResponse(frr);
				} else if(o instanceof ResponseAddable) { //ResponseByContentTypeRule
					ResponseAddable addable = (ResponseAddable)o;
					addable.addResponse(frr);
				}
			}
		});
	}
}
