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
import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.context.bean.scan.BeanClassScanner;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.DefaultSettings;
import com.aspectran.core.context.builder.apon.params.ItemParameters;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.io.FileImportStream;
import com.aspectran.core.util.io.ImportStream;
import com.aspectran.core.util.io.ResourceImportStream;
import com.aspectran.core.util.io.URLImportStream;
import com.aspectran.core.util.wildcard.WildcardPattern;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletParser;
import com.aspectran.core.var.apon.GenericParameters;
import com.aspectran.core.var.apon.ParameterHolder;
import com.aspectran.core.var.apon.Parameters;
import com.aspectran.core.var.rule.AspectJobAdviceRule;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.BeanRule;
import com.aspectran.core.var.rule.FileItemRule;
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
import com.aspectran.core.var.type.JoinpointScopeType;
import com.aspectran.core.var.type.PointcutPatternOperationType;

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
				
				SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule);
				
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

				JoinpointScopeType joinpointScope = null;
				
				if(scope != null) {
					joinpointScope = JoinpointScopeType.valueOf(scope);
					
					if(joinpointScope == null)
						throw new IllegalArgumentException("Unknown joinpoint scope '" + scope + "'");
				} else {
					joinpointScope = JoinpointScopeType.TRANSLET;
				}
				
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				aspectRule.setJoinpointScope(joinpointScope);
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
				
				PointcutPatternRule pointcutPatternRule = PointcutPatternRule.newInstance(translet, bean, method);
				
				assistant.pushObject(pointcutPatternRule);
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut/target/exclude", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String translet = attributes.get("translet");
				String bean = attributes.get("bean");
				String method = attributes.get("method");
				
				PointcutPatternRule pointcutPatternRule = (PointcutPatternRule)assistant.peekObject();
				AspectRule aspectRule = (AspectRule)assistant.peekObject(1);
				
				if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET) {
					PointcutRule.addExcludePointcutPatternRule(pointcutPatternRule, translet, bean, method);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut/target/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				PointcutPatternRule pointcutPatternRule = (PointcutPatternRule)assistant.popObject();
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				
				if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET) {
					aspectRule.getPointcutRule().addPointcutPatternRule(pointcutPatternRule);
				}
			}
		});
//		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut/end()", new Nodelet() {
//			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
//				AspectRule aspectRule = (AspectRule)assistant.peekObject();
//				PointcutRule pointcutRule = aspectRule.getPointcutRule();
//				
//				if(pointcutRule != null) {
//					
//				}
//			}
//		});
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
				boolean disabled = Boolean.parseBoolean(attributes.get("disabled"));

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
				boolean singleton = Boolean.parseBoolean(attributes.get("singleton"));
				String factoryMethod = attributes.get("factoryMethod");
				String initMethodName = attributes.get("initMethod");
				String destroyMethodName = attributes.get("destroyMethod");
				boolean lazyInit = Boolean.parseBoolean(attributes.get("lazyInit"));
				boolean override = Boolean.parseBoolean(attributes.get("override"));
				
				if(id == null) {
					throw new IllegalArgumentException("The <bean> element requires a id attribute.");
				} else {
					id = assistant.applyNamespaceForBean(id);
				}

				if(className == null)
					throw new IllegalArgumentException("The <bean> element requires a class attribute.");

				Class<?> beanClass = null;
				Map<String, Class<?>> beanClassMap = null;
				
				if(!WildcardPattern.hasWildcards(className)) {
					beanClass = assistant.getClassLoader().loadClass(className);
				} else {
					BeanClassScanner scanner = new BeanClassScanner(id, assistant.getClassLoader());
					beanClassMap = scanner.scanClass(className);
				}
								
				if(beanClass != null) {
					BeanRule beanRule = BeanRule.newInstance(id, beanClass, scope, singleton, factoryMethod, initMethodName, destroyMethodName, lazyInit, override);
	
					assistant.pushObject(beanRule);
				} else {
					BeanRule[] beanRules = new BeanRule[beanClassMap.size()];
					
					int i = 0;
					for(Map.Entry<String, Class<?>> entry : beanClassMap.entrySet()) {
						String beanId = entry.getKey();
						Class<?> beanClass2 = entry.getValue();
						
						BeanRule beanRule = BeanRule.newInstance(beanId, beanClass2, scope, singleton, factoryMethod, initMethodName, destroyMethodName, lazyInit, override);
						beanRule.setStealthily(true);
						
						beanRules[i++] = beanRule;
					}
	
					assistant.pushObject(beanRules);					
				}
			}
		});
		parser.addNodelet("/aspectran/bean/constructor/argument", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
				
				if(text != null) {
					ParameterHolder holder = new ParameterHolder(text, new ItemParameters(), true);
					holder.getParametersArray();
					//TODO
				}
			}
		});
		
		parser.addNodelet("/aspectran/bean/constructor/argument", new ItemRuleNodeletAdder(assistant));
		
		parser.addNodelet("/aspectran/bean/constructor/argument/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				Object o = assistant.peekObject();
				
				if(o instanceof BeanRule) {
					BeanRule beanRule = (BeanRule)o;
					beanRule.setConstructorArgumentItemRuleMap(irm);
				} else {
					for(BeanRule beanRule : (BeanRule[])o) {
						beanRule.setConstructorArgumentItemRuleMap(irm);
					}
				}
			}
		});
		parser.addNodelet("/aspectran/bean/property", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});

		parser.addNodelet("/aspectran/bean/property", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/bean/property/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				Object o = assistant.peekObject();
				
				if(o instanceof BeanRule) {
					BeanRule beanRule = (BeanRule)o;
					beanRule.setPropertyItemRuleMap(irm);
				} else {
					for(BeanRule beanRule : (BeanRule[])o) {
						beanRule.setPropertyItemRuleMap(irm);
					}
				}
			}
		});
		parser.addNodelet("/aspectran/bean/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				Object o = assistant.popObject();
				
				if(o instanceof BeanRule) {
					BeanRule beanRule = (BeanRule)o;
					assistant.addBeanRule(beanRule);
				} else {
					for(BeanRule beanRule : (BeanRule[])o) {
						assistant.addBeanRule(beanRule);
					}
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
					Boolean fileItemCanBeAttribute = Boolean.valueOf(attribute);

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
				boolean hidden = Boolean.parseBoolean(attributes.get("hidden"));

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

		parser.addNodelet("/aspectran/translet/exception", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = ResponseByContentTypeRule.newInstance();
				assistant.pushObject(rbctr);
			}
		});
		parser.addNodelet("/aspectran/translet/exception/responseByContentType", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String exceptionType = attributes.get("exceptionType");

				ResponseByContentTypeRule rbctr = (ResponseByContentTypeRule)assistant.peekObject();
				rbctr.setExceptionType(exceptionType);
			}
		});

		parser.addNodelet("/aspectran/translet/exception/responseByContentType", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/exception/defaultResponse", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = ResponseByContentTypeRule.newInstance();
				assistant.pushObject(rbctr);
			}
		});
		
		parser.addNodelet("/aspectran/translet/exception/defaultResponse", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/exception/defaultResponse/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = (ResponseByContentTypeRule)assistant.popObject();
				ResponseMap responseMap = rbctr.getResponseMap();
				
				if(responseMap.size() > 0) {
					ResponseByContentTypeRule rbctr2 = (ResponseByContentTypeRule)assistant.peekObject();
					rbctr2.setDefaultResponse(responseMap.get(0));
				}
			}
		});
		parser.addNodelet("/aspectran/translet/exception/end()", new Nodelet() {
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
