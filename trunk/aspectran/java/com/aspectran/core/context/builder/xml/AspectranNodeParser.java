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
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.ImportHandler;
import com.aspectran.core.context.builder.Importable;
import com.aspectran.core.context.rule.AspectJobAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseByContentTypeRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletParser;

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
		this(assistant, true);
	}

	public AspectranNodeParser(ContextBuilderAssistant assistant, boolean validating) {
		this.assistant = assistant;
		assistant.clearObjectStack();

		parser.setValidating(validating);
		parser.setEntityResolver(new AspectranDtdResolver(validating));

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
	public void parse(InputStream inputStream) throws Exception {
		try {
			parser.parse(inputStream);
		} catch(Exception e) {
			throw new Exception("Error parsing aspectran configuration.", e);
		} finally {
			if(inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	/**
	 * Adds the settings nodelets.
	 */
	private void addSettingsNodelets() {
		parser.addNodelet("/aspectran/settings", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					Parameters parameters = new GenericParameters(text);
					Iterator<String> iter = parameters.getParameterNameSet().iterator();
					
					while(iter.hasNext()) {
						String name = iter.next();
						
						DefaultSettingType settingType = null;
						
						if(name != null) {
							settingType = DefaultSettingType.valueOf(name);
							
							if(settingType == null)
								throw new IllegalArgumentException("Unknown setting name '" + name + "'");
						}
						
						assistant.putSetting(settingType, parameters.getString(name));
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
		parser.addNodelet("/aspectran/typeAliases", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					Parameters parameters = new GenericParameters(text);
					Iterator<String> iter = parameters.getParameterNameSet().iterator();
					
					while(iter.hasNext()) {
						String alias = iter.next();
						assistant.addTypeAlias(alias, parameters.getString(alias));
					}
				}
			}
		});
		parser.addNodelet("/aspectran/typeAliases/typeAlias", new Nodelet() {
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
		parser.addNodelet("/aspectran/aspect/joinpoint", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String scope = attributes.get("scope");

				AspectRule aspectRule = assistant.peekObject();
				AspectRule.updateJoinpointScope(aspectRule, scope);
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String type = attributes.get("type");
				
				AspectRule aspectRule = assistant.peekObject();
				PointcutRule pointcutRule = PointcutRule.newInstance(aspectRule, type, text);
				aspectRule.setPointcutRule(pointcutRule);
			}
		});
/*
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut/target", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String translet = attributes.get("translet");
				String bean = attributes.get("bean");
				String method = attributes.get("method");
				
				AspectRule aspectRule = assistant.peekObject();
				
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
				
				List<PointcutPatternRule> pointcutPatternRuleList = assistant.peekObject();
				
				if(pointcutPatternRuleList != null) {
					PointcutRule.addExcludePointcutPatternRule(pointcutPatternRuleList, translet, bean, method);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut/target/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				List<PointcutPatternRule> pointcutPatternRuleList = assistant.popObject();
				
				if(pointcutPatternRuleList != null) {
					AspectRule aspectRule = assistant.peekObject();
					aspectRule.getPointcutRule().addPointcutPatternRule(pointcutPatternRuleList);
				}
			}
		});
*/
		parser.addNodelet("/aspectran/aspect/settings", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				AspectRule aspectRule = assistant.peekObject();
				
				SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule, text);
				
				assistant.pushObject(sar);
			}
		});
		parser.addNodelet("/aspectran/aspect/settings/setting", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				String value = attributes.get("value");
				
				if(name != null) {
					SettingsAdviceRule sar = assistant.peekObject();
					sar.putSetting(name, value);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/settings/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				SettingsAdviceRule sar = assistant.popObject();
				AspectRule aspectRule = assistant.peekObject();
				aspectRule.setSettingsAdviceRule(sar);
			}
		});	
		parser.addNodelet("/aspectran/aspect/advice", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String beanId = attributes.get("bean");
				
				if(!StringUtils.isEmpty(beanId)) {
					AspectRule aspectRule = assistant.peekObject();
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

				transletName = assistant.applyTransletNamePattern(transletName);
				AspectRule ar = assistant.peekObject();
				
				AspectJobAdviceRule ajar = AspectJobAdviceRule.newInstance(ar, transletName, disabled);
				ar.addAspectJobAdviceRule(ajar);
			}
		});
		parser.addNodelet("/aspectran/aspect/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				AspectRule aspectRule = assistant.popObject();
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
				String className = assistant.resolveAliasType(attributes.get("class"));
				String scope = attributes.get("scope");
				Boolean singleton = BooleanUtils.toNullableBooleanObject(attributes.get("singleton"));
				String factoryMethod = attributes.get("factoryMethod");
				String initMethodName = attributes.get("initMethod");
				String destroyMethodName = attributes.get("destroyMethod");
				Boolean lazyInit = BooleanUtils.toNullableBooleanObject(attributes.get("lazyInit"));
				Boolean important = BooleanUtils.toNullableBooleanObject(attributes.get("important"));

				BeanRule beanRule = BeanRule.newInstance(id, className, scope, singleton, factoryMethod, initMethodName, destroyMethodName, lazyInit, important);
				assistant.pushObject(beanRule);					
			}
		});
		parser.addNodelet("/aspectran/bean/constructor/argument", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					BeanRule beanRule = assistant.peekObject();
					BeanRule.updateConstructorArgument(beanRule, text);
				}
				
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet("/aspectran/bean/constructor/argument", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/bean/constructor/argument/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setConstructorArgumentItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet("/aspectran/bean/property", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					BeanRule beanRule = assistant.peekObject();
					BeanRule.updateProperty(beanRule, text);
				}
				
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});
		parser.addNodelet("/aspectran/bean/property", new ItemRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/bean/property/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setPropertyItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet("/aspectran/bean/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				BeanRule beanRule = assistant.popObject();
				assistant.addBeanRule(beanRule);
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
				ItemRuleMap irm = assistant.popObject();
				
				if(!irm.isEmpty()) {
					RequestRule requestRule = assistant.peekObject();
					requestRule.setAttributeItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet("/aspectran/translet/request/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				RequestRule requestRule = assistant.popObject();
				TransletRule transletRule = assistant.peekObject();
				transletRule.setRequestRule(requestRule);
			}
		});
		parser.addNodelet("/aspectran/translet/contents", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				Boolean omittable = BooleanUtils.toNullableBooleanObject(attributes.get("omittable"));
				
				ContentList contentList = ContentList.newInstance(name, omittable);
				assistant.pushObject(contentList);
			}
		});
		parser.addNodelet("/aspectran/translet/contents/content", new Nodelet() {
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
		parser.addNodelet("/aspectran/translet/contents/content", new ActionRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/translet/contents/content/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = assistant.popObject();
				
				if(!actionList.isEmpty()) {
					ContentList contentList = assistant.peekObject();
					contentList.addActionList(actionList);
				}
			}
		});
		parser.addNodelet("/aspectran/translet/contents/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ContentList contentList = assistant.popObject();
				
				if(!contentList.isEmpty()) {
					TransletRule transletRule = assistant.peekObject();
					transletRule.setContentList(contentList);
				}
			}
		});
		parser.addNodelet("/aspectran/translet/content", new Nodelet() {
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
		parser.addNodelet("/aspectran/translet/content", new ActionRuleNodeletAdder(assistant));
		parser.addNodelet("/aspectran/translet/content/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ActionList actionList = assistant.popObject();
				
				if(!actionList.isEmpty()) {
					ContentList contentList = assistant.popObject();
					contentList.addActionList(actionList);
				}
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
				ResponseRule responseRule = assistant.popObject();
				TransletRule transletRule = assistant.peekObject();
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
				ResponseByContentTypeRule rbctr = assistant.popObject();
				TransletRule transletRule = assistant.peekObject();
				transletRule.addExceptionHandlingRule(rbctr);
			}
		});
		parser.addNodelet("/aspectran/translet/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				TransletRule transletRule = assistant.popObject();
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
				String fileType = attributes.get("fileType");
				
				Importable importable = Importable.newInstance(assistant, resource, file, url, fileType);
				
				ImportHandler importHandler = assistant.getImportHandler();
				
				if(importHandler != null)
					importHandler.pending(importable);
			}
		});
	}

}
