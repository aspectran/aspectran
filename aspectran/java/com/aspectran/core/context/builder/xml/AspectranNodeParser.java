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

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.DefaultSettings;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.io.FileImportStream;
import com.aspectran.core.util.io.ImportStream;
import com.aspectran.core.util.io.ResourceImportStream;
import com.aspectran.core.util.io.URLImportStream;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletParser;
import com.aspectran.core.var.rule.AspectJobAdviceRule;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.BeanRule;
import com.aspectran.core.var.rule.FileItemRule;
import com.aspectran.core.var.rule.ItemRule;
import com.aspectran.core.var.rule.ItemRuleMap;
import com.aspectran.core.var.rule.PointcutPatternRule;
import com.aspectran.core.var.rule.PointcutRule;
import com.aspectran.core.var.rule.RequestRule;
import com.aspectran.core.var.rule.ResponseByContentTypeRule;
import com.aspectran.core.var.rule.ResponseRule;
import com.aspectran.core.var.rule.SettingsAdviceRule;
import com.aspectran.core.var.rule.TransletRule;
import com.aspectran.core.var.type.AspectAdviceType;
import com.aspectran.core.var.type.AspectTargetType;
import com.aspectran.core.var.type.DefaultSettingType;

/**
 * Translet Map Parser.
 * 
 * <p>Created: 2008. 06. 14 오전 4:39:24</p>
 */
public class AspectranNodeParser {
	
	private final NodeletParser parser = new NodeletParser();

	private final ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new translet map parser.
	 * 
	 * @param assistant the assistant for Context Builder
	 */
	public AspectranNodeParser(ContextBuilderAssistant assistant) {
		assistant.clearObjectStack();
		assistant.setNamespace(null);
		
		this.assistant = assistant;

		parser.setValidation(true);
		parser.setEntityResolver(new AspectranDtdResolver());

		addRootNodelets();
		addSettingsNodelets();
		addTypeAliasNodelets();
		addAspectRuleNodelets();
		addBeanNodelets();
		addTransletNodelets();
		addImportNodelets();
	}
	
	/**
	 * Parses the aspectran configuration.
	 *
	 * @param inputStream the input stream
	 * @throws Exception the exception
	 */
	public void parse(ImportStream importStream) throws Exception {
		InputStream inputStream = importStream.getInputStream();
		
		try {
			parser.parse(inputStream);
		} catch(Exception e) {
			throw new Exception("Error parsing aspectran configuration. Cause: " + e, e);
		} finally {
			if(inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	/**
	 * Returns the resolve alias type.
	 * 
	 * @param alias the alias
	 * 
	 * @return the string
	 */
	protected String resolveAliasType(String alias) {
		String type = assistant.getAliasType(alias);

		if(type == null)
			return alias;

		return type;
	}

	/**
	 * Adds the aspectran nodelets.
	 */
	private void addRootNodelets() {
		parser.addNodelet("/aspectran", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String namespace = attributes.get("namespace");

				if(namespace != null) {
					namespace = namespace.trim();

					if(namespace.length() == 0)
						namespace = null;
				}

				assistant.setNamespace(namespace);
			}
		});
	}

	/**
	 * Adds the settings nodelets.
	 */
	private void addSettingsNodelets() {
		parser.addNodelet("/aspectran/settings", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					Parameters params = new GenericParameters(text);
					Iterator<String> iter = params.getParameterNameSet().iterator();
					
					while(iter.hasNext()) {
						String name = iter.next();
						
						DefaultSettingType settingType = null;
						
						if(name != null) {
							settingType = DefaultSettingType.valueOf(name);
							
							if(settingType == null)
								throw new IllegalArgumentException("Unknown setting name '" + name + "'");
						}
						
						assistant.putSetting(settingType, params.getString(name));
					}
				}
			}
		});
		parser.addNodelet("/aspectran/settings/setting", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				String value = attributes.get("value");

				DefaultSettingType settingType = null;
				
				if(name != null) {
					settingType = DefaultSettingType.valueOf(name);
					
					if(settingType == null)
						throw new IllegalArgumentException("Unknown setting name '" + name + "'");
				}

				assistant.putSetting(settingType, (text == null) ? value : text);
			}
		});
		parser.addNodelet("/aspectran/settings/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				assistant.applySettings();
			}
		});
	}

	/**
	 * Adds the type alias nodelets.
	 */
	private void addTypeAliasNodelets() {
		parser.addNodelet("/aspectran/typeAlias", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String alias = attributes.get("alias");
				String type = attributes.get("type");

				assistant.addTypeAlias(alias, type);
			}
		});
	}
	
	private void addAspectRuleNodelets() {
		parser.addNodelet("/aspectran/aspect", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String useFor = attributes.get("for");
				
				AspectRule aspectRule = AspectRule.newInstance(id, useFor);
				
				assistant.pushObject(aspectRule);
			}
		});
		parser.addNodelet("/aspectran/aspect/settings", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				
				SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule, text);
				
				assistant.pushObject(sar);
			}
		});
		parser.addNodelet("/aspectran/aspect/settings/setting", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				String value = attributes.get("value");
				
				if(name != null) {
					SettingsAdviceRule sar = (SettingsAdviceRule)assistant.peekObject();
					sar.putSetting(name, value);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/settings/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				SettingsAdviceRule sar = (SettingsAdviceRule)assistant.popObject();
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				aspectRule.setSettingsAdviceRule(sar);
			}
		});	
		parser.addNodelet("/aspectran/aspect/joinpoint", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String scope = attributes.get("scope");

				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				AspectRule.updateJoinpointScope(aspectRule, scope);
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String type = attributes.get("type");
				
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				
				PointcutRule pointcutRule = PointcutRule.newInstance(aspectRule, type, text);
				aspectRule.setPointcutRule(pointcutRule);
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut/target", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String translet = attributes.get("translet");
				String bean = attributes.get("bean");
				String method = attributes.get("method");
				
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				
				if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET) {
					List<PointcutPatternRule> pointcutPatternRuleList = PointcutRule.newPointcutPatternRuleList();
					PointcutRule.addPointcutPatternRule(pointcutPatternRuleList, translet, bean, method, text);
					
					assistant.pushObject(pointcutPatternRuleList);
				} else {
					assistant.pushObject(null);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut/target/exclude", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String translet = attributes.get("translet");
				String bean = attributes.get("bean");
				String method = attributes.get("method");
				
				@SuppressWarnings("unchecked")
				List<PointcutPatternRule> pointcutPatternRuleList = (List<PointcutPatternRule>)assistant.peekObject();
				
				if(pointcutPatternRuleList != null) {
					PointcutRule.addExcludePointcutPatternRule(pointcutPatternRuleList, translet, bean, method);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut/target/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				@SuppressWarnings("unchecked")
				List<PointcutPatternRule> pointcutPatternRuleList = (List<PointcutPatternRule>)assistant.popObject();
				
				if(pointcutPatternRuleList != null) {
					AspectRule aspectRule = (AspectRule)assistant.peekObject();
					aspectRule.getPointcutRule().addPointcutPatternRule(pointcutPatternRuleList);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/advice", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String beanId = attributes.get("bean");
				
				if(beanId != null && beanId.length() > 0) {
					AspectRule aspectRule = (AspectRule)assistant.peekObject();
					aspectRule.setAdviceBeanId(beanId);
					
					assistant.putBeanReference(beanId, aspectRule);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/advice/before", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.BEFORE));
		parser.addNodelet("/aspectran/aspect/advice/after", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.AFTER));
		parser.addNodelet("/aspectran/aspect/advice/around", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.AROUND));
		parser.addNodelet("/aspectran/aspect/advice/finally", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.FINALLY));
		parser.addNodelet("/aspectran/aspect/advice/exceptionRaized", new AspectExceptionRaisedAdviceRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/aspect/advice/job", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String transletName = attributes.get("translet");
				Boolean disabled = BooleanUtils.toNullableBooleanObject(attributes.get("disabled"));

				transletName = assistant.getFullTransletName(transletName);
				AspectRule ar = (AspectRule)assistant.peekObject();
				
				AspectJobAdviceRule ajar = AspectJobAdviceRule.newInstance(ar, transletName, disabled);
				ar.addAspectJobAdviceRule(ajar);
			}
		});
		parser.addNodelet("/aspectran/aspect/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				AspectRule aspectRule = (AspectRule)assistant.popObject();
				assistant.addAspectRule(aspectRule);
			}
		});
	}


	/**
	 * Adds the bean nodelets.
	 */
	private void addBeanNodelets() {
		parser.addNodelet("/aspectran/bean", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String className = resolveAliasType(attributes.get("class"));
				String scope = attributes.get("scope");
				Boolean singleton = BooleanUtils.toNullableBooleanObject(attributes.get("singleton"));
				String factoryMethod = attributes.get("factoryMethod");
				String initMethodName = attributes.get("initMethod");
				String destroyMethodName = attributes.get("destroyMethod");
				Boolean lazyInit = BooleanUtils.toNullableBooleanObject(attributes.get("lazyInit"));
				Boolean important = BooleanUtils.toNullableBooleanObject(attributes.get("important"));

				if(id != null) {
					id = assistant.applyNamespaceForBean(id);
				}

				BeanRule[] beanRules = BeanRule.newInstance(assistant.getClassLoader(), id, className, scope, singleton, factoryMethod, initMethodName, destroyMethodName, lazyInit, important);
	
				assistant.pushObject(beanRules);					
			}
		});
		parser.addNodelet("/aspectran/bean/constructor/argument", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					List<Parameters> argumentParameterList = ItemRule.toParametersList(text);
					
					if(argumentParameterList != null) {
						BeanRule[] beanRules = (BeanRule[])assistant.peekObject();
						BeanRule.updateConstructorArgument(beanRules, argumentParameterList);
					}
				}
				
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet("/aspectran/bean/constructor/argument", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/bean/constructor/argument/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				BeanRule[] beanRules = (BeanRule[])assistant.peekObject();
				
				for(BeanRule beanRule : beanRules) {
					beanRule.setConstructorArgumentItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet("/aspectran/bean/property", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					List<Parameters> argumentParameterList = ItemRule.toParametersList(text);
					
					if(argumentParameterList != null) {
						BeanRule[] beanRules = (BeanRule[])assistant.peekObject();
						BeanRule.updateProperty(beanRules, argumentParameterList);
					}
				}
				
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet("/aspectran/bean/property", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/bean/property/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				BeanRule[] beanRules = (BeanRule[])assistant.peekObject();
				
				for(BeanRule beanRule : beanRules) {
					beanRule.setPropertyItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet("/aspectran/bean/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				BeanRule[] beanRules = (BeanRule[])assistant.popObject();
				
				for(BeanRule beanRule : beanRules) {
					assistant.addBeanRule(beanRule);
				}
			}
		});
	}
	
	/**
	 * Adds the translet nodelets.
	 */
	private void addTransletNodelets() {
		parser.addNodelet("/aspectran/translet", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");

				TransletRule transletRule = TransletRule.newInstance(name);

				assistant.pushObject(transletRule);
			}
		});
		parser.addNodelet("/aspectran/translet", new ActionRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/translet", new ResponseRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/translet/request", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String method = attributes.get("method");
				String characterEncoding = attributes.get("characterEncoding");

				RequestRule requestRule = RequestRule.newInstance(method, characterEncoding);
				
				assistant.pushObject(requestRule);
			}
		});
		parser.addNodelet("/aspectran/translet/request/attribute", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet("/aspectran/translet/request/attribute", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/translet/request/attribute/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				RequestRule requestRule = (RequestRule)assistant.peekObject();
				requestRule.setAttributeItemRuleMap(irm);
			}
		});
		parser.addNodelet("/aspectran/translet/request/multipart", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String attribute = attributes.get("attribute");

				if(attribute != null) {
					Boolean fileItemCanBeAttribute = BooleanUtils.toNullableBooleanObject(attribute);

					RequestRule requestRule = (RequestRule)assistant.peekObject();
					requestRule.setFileItemCanBeAttribute(fileItemCanBeAttribute);
				}
			}
		});
		parser.addNodelet("/aspectran/translet/request/multipart/fileItem", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				
				FileItemRule fir = FileItemRule.newInstance(name);
				
				assistant.pushObject(fir);
			}
		});
		parser.addNodelet("/aspectran/translet/request/multipart/fileItem/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				FileItemRule fir = (FileItemRule)assistant.popObject();
				RequestRule requestRule = (RequestRule)assistant.peekObject();
				requestRule.addFileItemRule(fir);
			}
		});
		parser.addNodelet("/aspectran/translet/request/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				RequestRule requestRule = (RequestRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.setRequestRule(requestRule);
			}
		});
		parser.addNodelet("/aspectran/translet/contents", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ContentList contentList = new ContentList();
				assistant.pushObject(contentList);
			}
		});
		parser.addNodelet("/aspectran/translet/contents/content", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

				if(!assistant.isNullableContentId() && StringUtils.isEmpty(id))
					throw new IllegalArgumentException("The <content> element requires a id attribute.");
				
				ContentList contentList = (ContentList)assistant.peekObject();
				ActionList actionList = new ActionList(id, contentList);
				actionList.setHidden(hidden);

				assistant.pushObject(actionList);
			}
		});
		parser.addNodelet("/aspectran/translet/contents/content", new ActionRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/translet/contents/content/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				ContentList contentList = (ContentList)assistant.peekObject();
				contentList.addActionList(actionList);
			}
		});
		parser.addNodelet("/aspectran/translet/contents/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ContentList contentList = (ContentList)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.setContentList(contentList);
			}
		});
		parser.addNodelet("/aspectran/translet/response", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				String characterEncoding = attributes.get("characterEncoding");

				ResponseRule responseRule = ResponseRule.newInstance(name, characterEncoding);

				assistant.pushObject(responseRule);
			}
		});
		parser.addNodelet("/aspectran/translet/response", new ResponseRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/translet/response/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ResponseRule responseRule = (ResponseRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.addResponseRule(responseRule);
			}
		});
		parser.addNodelet("/aspectran/translet/exception/responseByContentType", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String exceptionType = attributes.get("exceptionType");

				ResponseByContentTypeRule rbctr = ResponseByContentTypeRule.newInstance(exceptionType);
				assistant.pushObject(rbctr);
			}
		});
		parser.addNodelet("/aspectran/translet/exception/responseByContentType", new ResponseRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/translet/exception/responseByContentType/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = (ResponseByContentTypeRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.addExceptionHandlingRule(rbctr);
			}
		});
		parser.addNodelet("/aspectran/translet/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				TransletRule transletRule = (TransletRule)assistant.popObject();
				assistant.addTransletRule(transletRule);
			}
		});
	}
	
	/**
	 * Adds the translet map nodelets.
	 */
	private void addImportNodelets() {
		parser.addNodelet("/aspectran/import", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String resource = attributes.get("resource");
				String file = attributes.get("file");
				String url = attributes.get("url");
				
				ImportStream importStream = null;
				
				if(StringUtils.hasText(resource))
					importStream = new ResourceImportStream(assistant.getClassLoader(), resource);
				else if(StringUtils.hasText(file))
					importStream = new FileImportStream(assistant.getApplicationBasePath(), file);
				else if(StringUtils.hasText(url))
					importStream = new URLImportStream(url);
				else
					throw new IllegalArgumentException("The <import> element requires either a resource or a file or a url attribute.");
				
				DefaultSettings defaultSettings = (DefaultSettings)assistant.getDefaultSettings().clone();
				
				AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(assistant);
				aspectranNodeParser.parse(importStream);
				
				assistant.setDefaultSettings(defaultSettings);
			}
		});
	}

}
