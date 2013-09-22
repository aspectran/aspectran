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
import java.util.Properties;

import org.w3c.dom.Node;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.builder.AspectranContextBuildingAssistant;
import com.aspectran.core.rule.DispatchResponseRule;
import com.aspectran.core.rule.ForwardResponseRule;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.rule.RedirectResponseRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.TransformRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.ability.ResponseAddable;
import com.aspectran.core.type.TransformType;
import com.aspectran.core.util.Resources;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class ResponseRuleNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class ResponseRuleNodeletAdder implements NodeletAdder {
	
	protected AspectranContextBuildingAssistant assistant;
	
	/**
	 * Instantiates a new response rule nodelet adder.
	 * 
	 * @param parser the parser
	 * @param assistant the assistant for Context Builder
	 */
	public ResponseRuleNodeletAdder(AspectranContextBuildingAssistant assistant) {
		this.assistant = assistant;
	}

//	private void addResponseRule(Object object, DispatchResponseRule drr) {
//		if(object instanceof ResponseByContentType) {
//			ResponseByContentType responseByContentType = (ResponseByContentType)object;
//			
//			if(defaultResponseAddingMode) { 
//				responseByContentType.setDefaultResponseRule(drr);
//			} else
//				responseByContentType.addResponseRule(drr);
//		} else if(object instanceof ResponseRule) {
//			ResponseRule responseRule = (ResponseRule)object;
//			responseRule.addResponseRule(drr);
//		}
//	}
	
	private ResponseRule retrieveResponseRule(TransletRule transletRule) {
		ResponseRule responseRule = transletRule.getResponseRule();
		
		if(responseRule == null) {
			responseRule = new ResponseRule();
			transletRule.setResponseRule(responseRule);
		}
		
		return responseRule;
	}
	
	/**
	 * Process.
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/transform", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = attributes.getProperty("id");
				String typeString = attributes.getProperty("type");
				String contentType = attributes.getProperty("contentType");
				String characterEncoding = attributes.getProperty("characterEncoding");

				TransformType transformType = TransformType.valueOf(typeString);

				if(transformType == null)
					throw new IllegalArgumentException("Unkown transform-type '" + typeString + "'.");

				TransformRule tr = new TransformRule();
				tr.setId(id);
				tr.setTransformType(transformType);
				tr.setContentType(contentType);
				tr.setCharacterEncoding(characterEncoding);

				assistant.pushObject(tr);
				
				ActionList actionList = new ActionList(id, null);
				assistant.pushObject(actionList);
			}
		});
		
		parser.addNodelet(xpath, "/transform", new ActionRuleNodeletAdder(assistant));
		
		parser.addNodelet(xpath, "/transform/template", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String resource = attributes.getProperty("resource");
				String filePath = attributes.getProperty("file");
				String templateUrl = attributes.getProperty("url");
				String encoding = attributes.getProperty("encoding");
				Boolean noCache = Boolean.valueOf(attributes.getProperty("noCache"));
				String templateContent = text;

				// backup
				ActionList actionList = (ActionList)assistant.popObject();
				
				TransformRule tr = (TransformRule)assistant.peekObject();
				
				// restore
				assistant.pushObject(actionList);
				
				if(tr.getTransformType() == TransformType.XML_TRANSFORM ||
					tr.getTransformType() == TransformType.JSON_TRANSFORM ||
					tr.getTransformType() == TransformType.CUSTOM_TRANSFORM)
					return;
				
				if(!StringUtils.isEmpty(encoding))
					tr.setTemplateEncoding(encoding);
				
				if(tr.getTransformType() == TransformType.XSL_TRANSFORM) {
					File templateFile = null;

					if(!StringUtils.isEmpty(resource))
						templateFile = Resources.getResourceAsFile(resource);
					else if(!StringUtils.isEmpty(filePath))
						templateFile = assistant.toRealPathFile(filePath);

					if(templateFile == null)
						throw new IllegalArgumentException("The <template> element requires either a resource or a file attribute.");
					
					if(!templateFile.isFile())
						throw new IllegalArgumentException("Invalid template file " + templateFile.getAbsolutePath());

					tr.setTemplateFile(templateFile);
					tr.setTemplateNoCache(noCache);
				} else if(tr.getTransformType() == TransformType.TEXT_TRANSFORM) {
					File templateFile = null;

					if(!StringUtils.isEmpty(resource))
						templateFile = Resources.getResourceAsFile(resource);
					else if(!StringUtils.isEmpty(filePath))
						templateFile = assistant.toRealPathFile(filePath);
					
					if(templateFile != null) {
						if(!templateFile.isFile())
							throw new IllegalArgumentException("Invalid template file '" + templateFile.getAbsolutePath() + "'.");
						
						tr.setTemplateFile(templateFile);
					} else if(templateUrl != null) {
						tr.setTemplateUrl(templateUrl);
					} else if(!StringUtils.isEmpty(templateContent)) {
						tr.setTemplateContent(templateContent);
					}
					
					if(tr.getTemplateFile() == null && tr.getTemplateUrl() == null && tr.getTemplateContent() == null)
						throw new IllegalArgumentException("The <template> element requires either a resource or a file or a url attribute.");

					tr.setTemplateNoCache(noCache);
				}
			}
		});
		parser.addNodelet(xpath, "/transform/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				TransformRule tr = (TransformRule)assistant.popObject();
				
				if(!actionList.isEmpty())
					tr.setActionList(actionList);
				
				Object o = assistant.peekObject();
				
				if(o instanceof TransletRule) {
					TransletRule transletRule = (TransletRule)o;
					ResponseRule responseRule = retrieveResponseRule(transletRule);
					responseRule.addResponse(tr);
				} else {
					ResponseAddable responseAddable = (ResponseAddable)o;
					responseAddable.addResponse(tr);
				}
			}
		});
		parser.addNodelet(xpath, "/dispatch", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = attributes.getProperty("id");
				String contentType = attributes.getProperty("contentType");
				String encoding = attributes.getProperty("encoding");
				
				DispatchResponseRule drr = new DispatchResponseRule();
				drr.setId(id);
				drr.setContentType(contentType);
				drr.setCharacterEncoding(encoding);
				
				assistant.pushObject(drr);

				ActionList actionList = new ActionList(id, null);
				assistant.pushObject(actionList);
			}
		});

		parser.addNodelet(xpath, "/dispatch", new ActionRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/dispatch/template", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String templateFile = attributes.getProperty("file");
				String templateEncoding = attributes.getProperty("encoding");
				Boolean noCache = Boolean.valueOf(attributes.getProperty("noCache"));
				String templateContent = text;
				
				// backup
				ActionList actionList = (ActionList)assistant.popObject();
				
				DispatchResponseRule drr = (DispatchResponseRule)assistant.peekObject();
				
				// restore
				assistant.pushObject(actionList);

				drr.setTemplateFile(templateFile);
				drr.setTemplateEncoding(templateEncoding);
				drr.setTemplateContent(templateContent);
				drr.setTemplateNoCache(noCache);
			}
		});
		parser.addNodelet(xpath, "/dispatch/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				DispatchResponseRule drr = (DispatchResponseRule)assistant.popObject();
				
				if(!actionList.isEmpty())
					drr.setActionList(actionList);
				
				Object o = assistant.peekObject();
				
				if(o instanceof TransletRule) {
					TransletRule transletRule = (TransletRule)o;
					ResponseRule responseRule = retrieveResponseRule(transletRule);
					responseRule.addResponse(drr);
				} else {
					ResponseAddable responseAddable = (ResponseAddable)o;
					responseAddable.addResponse(drr);
				}
			}
		});
		parser.addNodelet(xpath, "/redirect", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = attributes.getProperty("id");
				String contentType = attributes.getProperty("contentType"); // ResponseByContentType에서 content type에 따라 분기
				String translet = attributes.getProperty("translet");
				String url = attributes.getProperty("url");
				Boolean excludeNullParameters = Boolean.valueOf(attributes.getProperty("excludeNullParameters"));
				
				RedirectResponseRule rrr = new RedirectResponseRule();
				rrr.setId(id);
				rrr.setContentType(contentType);
				rrr.setTransletName(translet);
				
				if(url != null && url.length() > 0)
					rrr.setUrl(url);
				
				rrr.setExcludeNullParameters(excludeNullParameters);
				
				assistant.pushObject(rrr);
				
				ActionList actionList = new ActionList(id, null);
				assistant.pushObject(actionList);
			}
		});

		parser.addNodelet(xpath, "/redirect", new ActionRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/redirect/url", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String url = text;
				
				if(url != null) {
					url = url.trim();
					
					if(url.length() == 0)
						url = null;
				}

				if(url != null) {
					// backup
					ActionList actionList = (ActionList)assistant.popObject();
					
					RedirectResponseRule rrr = (RedirectResponseRule)assistant.peekObject();
					
					// restore
					assistant.pushObject(actionList);

					rrr.setUrl(url);
				}
			}
		});
		parser.addNodelet(xpath, "/redirect/parameters", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		
		parser.addNodelet(xpath, "/redirect/parameters", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/redirect/parameters/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					// backup
					ActionList actionList = (ActionList)assistant.popObject();
					
					RedirectResponseRule rrr = (RedirectResponseRule)assistant.peekObject();
					
					// restore
					assistant.pushObject(actionList);

					rrr.setParameterItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/redirect/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				RedirectResponseRule rrr = (RedirectResponseRule)assistant.popObject();
				
				if(rrr.getTransletName() == null && rrr.getUrl() == null)
					throw new IllegalArgumentException("The <redirect> element requires either a translet or a url attribute.");

				if(!actionList.isEmpty())
					rrr.setActionList(actionList);
				
				Object o = assistant.peekObject();
				
				if(o instanceof TransletRule) {
					TransletRule transletRule = (TransletRule)o;
					ResponseRule responseRule = retrieveResponseRule(transletRule);
					responseRule.addResponse(rrr);
				} else {
					ResponseAddable responseAddable = (ResponseAddable)o;
					responseAddable.addResponse(rrr);
				}
			}
		});
		parser.addNodelet(xpath, "/forward", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = attributes.getProperty("id");
				String contentType = attributes.getProperty("contentType");
				String path = attributes.getProperty("path");
				
				ForwardResponseRule frr = new ForwardResponseRule();
				frr.setId(id);
				frr.setContentType(contentType);
				frr.setTransletName(path);
				
				assistant.pushObject(frr);

				ActionList actionList = new ActionList(id, null);
				assistant.pushObject(actionList);
			}
		});

		parser.addNodelet(xpath, "/forward", new ActionRuleNodeletAdder(assistant));
		
		parser.addNodelet(xpath, "/forward/parameters", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});

		parser.addNodelet(xpath, "/forward/parameters", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet(xpath, "/forward/parameters/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					// backup
					ActionList actionList = (ActionList)assistant.popObject();
					
					ForwardResponseRule frr = (ForwardResponseRule)assistant.peekObject();
					
					// restore
					assistant.pushObject(actionList);
					
					frr.setParameterItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet(xpath, "/forward/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				ForwardResponseRule frr = (ForwardResponseRule)assistant.popObject();
				
				if(frr.getParameterItemRuleMap() == null)
					throw new IllegalArgumentException("The <forward> element requires a path attribute.");

				if(!actionList.isEmpty())
					frr.setActionList(actionList);
				
				Object o = assistant.peekObject();
				
				if(o instanceof TransletRule) {
					TransletRule transletRule = (TransletRule)o;
					ResponseRule responseRule = retrieveResponseRule(transletRule);
					responseRule.addResponse(frr);
				} else {
					ResponseAddable responseAddable = (ResponseAddable)o;
					responseAddable.addResponse(frr);
				}
			}
		});
	}
}
