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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Node;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.bean.loader.BeanClassLoader;
import com.aspectran.core.context.builder.xml.DefaultSettings;
import com.aspectran.core.context.builder.xml.InputStreamResource;
import com.aspectran.core.context.builder.xml.XmlBuilderAssistant;
import com.aspectran.core.context.rule.AspectJobAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.FileItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseByContentTypeRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.type.AspectAdviceType;
import com.aspectran.core.context.type.DefaultSettingType;
import com.aspectran.core.context.type.JoinpointScopeType;
import com.aspectran.core.context.type.JoinpointTargetType;
import com.aspectran.core.context.type.PointcutType;
import com.aspectran.core.context.type.RequestMethodType;
import com.aspectran.core.context.type.ScopeType;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.wildcard.WildcardPattern;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * Translet Map Parser.
 * 
 * <p>Created: 2008. 06. 14 오전 4:39:24</p>
 */
public class AspectranNodeParser {
	
	private final NodeletParser parser = new NodeletParser();

	private final XmlBuilderAssistant assistant;
	
	private final ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
	
	/**
	 * Instantiates a new translet map parser.
	 * 
	 * @param assistant the assistant for Context Builder
	 */
	public AspectranNodeParser(XmlBuilderAssistant assistant) {
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
	public void parse(InputStream inputStream) throws Exception {
		try {
			parser.parse(inputStream);
		} catch(Exception e) {
			throw new Exception("Error parsing aspectran configuration. Cause: " + e, e);
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
			public void process(Node node, Properties attributes, String text) throws Exception {
				String namespace = attributes.getProperty("namespace");

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
		parser.addNodelet("/aspectran/settings/setting", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				String value = attributes.getProperty("value");

				DefaultSettingType settingType = null;
				
				if(name != null) {
					settingType = DefaultSettingType.valueOf(name);
					
					if(settingType == null)
						throw new IllegalArgumentException("Unknown setting name '" + name + "'");
				}
				
				assistant.putSetting(settingType, value);
			}
		});
		parser.addNodelet("/aspectran/settings/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				assistant.applyInheritedSettings();
			}
		});
	}

	/**
	 * Adds the type alias nodelets.
	 */
	private void addTypeAliasNodelets() {
		parser.addNodelet("/aspectran/typeAlias", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String alias = attributes.getProperty("alias");
				String type = attributes.getProperty("type");

				assistant.addTypeAlias(alias, type);
			}
		});
	}
	
	private void addAspectRuleNodelets() {
		parser.addNodelet("/aspectran/aspect", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = attributes.getProperty("id");
				
				AspectRule aspectRule = new AspectRule();
				aspectRule.setId(id);
				
				assistant.pushObject(aspectRule);
			}
		});
		parser.addNodelet("/aspectran/aspect/settings", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				
				SettingsAdviceRule sar = new SettingsAdviceRule();
				sar.setAspectId(aspectRule.getId());
				sar.setAspectAdviceType(AspectAdviceType.SETTINGS);
				
				assistant.pushObject(sar);
			}
		});
		parser.addNodelet("/aspectran/aspect/settings/setting", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				String value = attributes.getProperty("value");
				
				if(name != null) {
					SettingsAdviceRule sar = (SettingsAdviceRule)assistant.peekObject();
					sar.putSetting(name, value);
				}
			}
		});
		parser.addNodelet("/aspectran/aspect/settings/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				SettingsAdviceRule sar = (SettingsAdviceRule)assistant.popObject();
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				aspectRule.setSettingsAdviceRule(sar);
			}
		});	
		parser.addNodelet("/aspectran/aspect/joinpoint", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String target = attributes.getProperty("target");
				String scope = attributes.getProperty("scope");

				JoinpointTargetType joinpointTarget = null;
				JoinpointScopeType joinpointScope = null;
				
				if(target != null) {
					joinpointTarget = JoinpointTargetType.valueOf(target);
					
					if(joinpointTarget == null)
						throw new IllegalArgumentException("Unknown joinpoint target '" + target + "'");
				} else {
					joinpointTarget = JoinpointTargetType.TRANSLET;
				}

				if(scope != null) {
					joinpointScope = JoinpointScopeType.valueOf(scope);
					
					if(joinpointScope == null)
						throw new IllegalArgumentException("Unknown joinpoint scope '" + scope + "'");
				} else {
					joinpointScope = JoinpointScopeType.TRANSLET;
				}
				
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				aspectRule.setJoinpointTarget(joinpointTarget);
				aspectRule.setJoinpointScope(joinpointScope);
			}
		});
		parser.addNodelet("/aspectran/aspect/joinpoint/pointcut", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				if(text == null)
					throw new IllegalArgumentException("Pointcut pattern can not be null");

				String type = attributes.getProperty("type");
				
				AspectRule aspectRule = (AspectRule)assistant.peekObject();
				PointcutType pointcutType = null;
				
				if(aspectRule.getJoinpointTarget() == JoinpointTargetType.SCHEDULER) {
					pointcutType = PointcutType.valueOf(type);
					
					if(pointcutType != PointcutType.SIMPLE_TRIGGER && pointcutType != PointcutType.CRON_TRIGGER)
						throw new IllegalArgumentException("scheduler-joinpoint must be 'simpleTrigger' or 'cronTrigger'.");
				} else {
					if(type != null) {
						pointcutType = PointcutType.valueOf(type);
						
						if(pointcutType == null)
							throw new IllegalArgumentException("Unknown pointcut type '" + type + "'");
					} else {
						pointcutType = PointcutType.WILDCARD;
					}
				}
				
				PointcutRule pointcutRule = new PointcutRule();
				pointcutRule.setPointcutType(pointcutType);
				pointcutRule.setPatternString(text);
				
				aspectRule.setPointcutRule(pointcutRule);
			}
		});
		parser.addNodelet("/aspectran/aspect/advice", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String beanId = attributes.getProperty("bean");
				
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
			public void process(Node node, Properties attributes, String text) throws Exception {
				String transletName = attributes.getProperty("translet");
				String disabled = attributes.getProperty("disabled");

				transletName = assistant.getFullTransletName(transletName);
				
				AspectRule ar = (AspectRule)assistant.peekObject();
				
				AspectJobAdviceRule ajar = new AspectJobAdviceRule();
				ajar.setAspectId(ar.getId());
				ajar.setAspectAdviceType(AspectAdviceType.JOB);
				ajar.setJobTransletName(transletName);
				ajar.setDisabled(Boolean.parseBoolean(disabled));
				
				ar.addAspectJobAdviceRule(ajar);
			}
		});
		
		parser.addNodelet("/aspectran/aspect/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				AspectRule aspectRule = (AspectRule)assistant.popObject();
				assistant.addAspectRule(aspectRule);
			}
		});
	}

	/**
	 * Adds the translet nodelets.
	 */
	private void addTransletNodelets() {
		parser.addNodelet("/aspectran/translet", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");

				if(name == null)
					throw new IllegalArgumentException("The <translet> element requires a name attribute.");

				TransletRule transletRule = new TransletRule();
				transletRule.setName(name);

				assistant.pushObject(transletRule);
			}
		});
		
		parser.addNodelet("/aspectran/translet", new ActionRuleNodeletAdder(assistant));
		
		parser.addNodelet("/aspectran/translet", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/request", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String method = attributes.getProperty("method");
				String characterEncoding = attributes.getProperty("characterEncoding");

				RequestMethodType methodType = null;
				
				if(method != null) {
					methodType = RequestMethodType.valueOf(method);
					
					if(methodType == null)
						throw new IllegalArgumentException("Unknown request method type '" + method + "'");
				}
				
				if(characterEncoding != null && !Charset.isSupported(characterEncoding))
					throw new IllegalCharsetNameException("Given charset name is illegal. '" + characterEncoding + "'");
				
				RequestRule requestRule = new RequestRule();
				requestRule.setMethod(methodType);
				requestRule.setCharacterEncoding(characterEncoding);
				
				assistant.pushObject(requestRule);
			}
		});

		parser.addNodelet("/aspectran/translet/request/attributes", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});		
		
		parser.addNodelet("/aspectran/translet/request/attributes", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/request/attributes/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				RequestRule requestRule = (RequestRule)assistant.peekObject();
				requestRule.setAttributeItemRuleMap(irm);
			}
		});		
		parser.addNodelet("/aspectran/translet/request/multiparts/fileItem", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				
				FileItemRule fir = new FileItemRule();
				fir.setName(name);

				assistant.pushObject(fir);
			}
		});
		parser.addNodelet("/aspectran/translet/request/multiparts/fileItem/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				FileItemRule fir = (FileItemRule)assistant.popObject();
				RequestRule requestRule = (RequestRule)assistant.peekObject();
				requestRule.addFileItemRule(fir);
			}
		});
		parser.addNodelet("/aspectran/translet/request/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				RequestRule requestRule = (RequestRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.setRequestRule(requestRule);
			}
		});
		parser.addNodelet("/aspectran/translet/contents", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ContentList contentList = new ContentList();
				assistant.pushObject(contentList);
			}
		});
		parser.addNodelet("/aspectran/translet/contents/content", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = attributes.getProperty("id");
				Boolean hidden = Boolean.valueOf(attributes.getProperty("hidden"));

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
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				ContentList contentList = (ContentList)assistant.peekObject();
				contentList.addActionList(actionList);
			}
		});
		parser.addNodelet("/aspectran/translet/contents/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ContentList contentList = (ContentList)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.setContentList(contentList);
			}
		});
		
		parser.addNodelet("/aspectran/translet/response", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				String characterEncoding = attributes.getProperty("characterEncoding");

				if(characterEncoding != null && !Charset.isSupported(characterEncoding))
					throw new IllegalCharsetNameException("Given charset name is illegal. '" + characterEncoding + "'");
				
				ResponseRule responseRule = new ResponseRule();
				responseRule.setName(name);
				responseRule.setCharacterEncoding(characterEncoding);

				assistant.pushObject(responseRule);
			}
		});

		parser.addNodelet("/aspectran/translet/response", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/response/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseRule responseRule = (ResponseRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.addResponseRule(responseRule);
			}
		});

		parser.addNodelet("/aspectran/translet/exception", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
				assistant.pushObject(rbctr);
			}
		});
		parser.addNodelet("/aspectran/translet/exception/responseByContentType", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String exceptionType = attributes.getProperty("exceptionType");

				ResponseByContentTypeRule rbctr = (ResponseByContentTypeRule)assistant.peekObject();
				rbctr.setExceptionType(exceptionType);
			}
		});

		parser.addNodelet("/aspectran/translet/exception/responseByContentType", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/exception/defaultResponse", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
				assistant.pushObject(rbctr);
			}
		});
		
		parser.addNodelet("/aspectran/translet/exception/defaultResponse", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/exception/defaultResponse/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = (ResponseByContentTypeRule)assistant.popObject();
				ResponseMap responseMap = rbctr.getResponseMap();
				
				if(responseMap.size() > 0) {
					ResponseByContentTypeRule rbctr2 = (ResponseByContentTypeRule)assistant.peekObject();
					rbctr2.setDefaultResponse(responseMap.get(0));
				}
			}
		});
		parser.addNodelet("/aspectran/translet/exception/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule rbctr = (ResponseByContentTypeRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();

				transletRule.addExceptionHandlingRule(rbctr);
			}
		});
		parser.addNodelet("/aspectran/translet/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				TransletRule transletRule = (TransletRule)assistant.popObject();
				
				if(transletRule.getRequestRule() == null) {
					RequestRule requestRule = new RequestRule();
					transletRule.setRequestRule(requestRule);
				}
				
				if(transletRule.getResponseRule() == null) {
					ResponseRule responseRule = new ResponseRule();
					transletRule.setResponseRule(responseRule);
				}
				
				List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
				
				if(responseRuleList == null || responseRuleList.size() == 0) {
					transletRule.setName(assistant.applyNamespaceForTranslet(transletRule.getName()));
					
					assistant.addTransletRule(transletRule);
				} else if(responseRuleList.size() == 1) {
					transletRule.setResponseRule(responseRuleList.get(0));
					transletRule.setResponseRuleList(null);
					transletRule.setName(assistant.applyNamespaceForTranslet(transletRule.getName()));
					
					assistant.addTransletRule(transletRule);
				} else if(responseRuleList.size() > 1) {
					ResponseRule defaultResponseRule = null;
					
					for(ResponseRule responseRule : responseRuleList) {
						String responseName = responseRule.getName();
						
						if(responseName == null || responseName.length() == 0) {
							defaultResponseRule = responseRule;
						} else {
							TransletRule subTransletRule = transletRule.newSubTransletRule(responseRule);
							subTransletRule.setName(assistant.applyNamespaceForTranslet(subTransletRule.getName()));
							
							assistant.addTransletRule(subTransletRule);
						}
					}
					
					if(defaultResponseRule != null) {
						transletRule.setResponseRule(defaultResponseRule);
						transletRule.setName(assistant.applyNamespaceForTranslet(transletRule.getName()));
						responseRuleList.remove(defaultResponseRule);
						transletRule.setResponseRuleList(responseRuleList);
						
						assistant.addTransletRule(transletRule);
					}
				}
			}
		});
	}

	/**
	 * Adds the bean nodelets.
	 */
	private void addBeanNodelets() {
		parser.addNodelet("/aspectran/bean", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = attributes.getProperty("id");
				String className = resolveAliasType(attributes.getProperty("class"));
				String singleton = attributes.getProperty("singleton");
				String scope = attributes.getProperty("scope");
				String factoryMethod = attributes.getProperty("factoryMethod");
				String initMethodName = attributes.getProperty("initMethod");
				String destroyMethodName = attributes.getProperty("destroyMethod");
				Boolean lazyInit = Boolean.valueOf(attributes.getProperty("lazyInit"));
				Boolean override = Boolean.valueOf(attributes.getProperty("override"));

				if(id == null) {
//					if(assistant.isNullableBeanId()) {
//						// When the bean id is null, the namespace does not apply.
//						id = classType;
//					} else {
						throw new IllegalArgumentException("The <bean> element requires a id attribute.");
//					}
				} else {
					id = assistant.applyNamespaceForBean(id);
				}

				if(className == null)
					throw new IllegalArgumentException("The <bean> element requires a class attribute.");

				Class<?> beanClass = null;
				Map<String, Class<?>> beanClassMap = null;
				
				if(!WildcardPattern.hasWildcards(className)) {
					beanClass = classLoader.loadClass(className);
				} else {
					BeanClassLoader beanClassLoader = new BeanClassLoader(id);
					beanClassMap = beanClassLoader.loadBeanClassMap(className);
				}
				
				boolean isSingleton = !(singleton != null && Boolean.valueOf(singleton) == Boolean.FALSE);
				ScopeType scopeType = ScopeType.valueOf(scope);
				
				if(scope != null && scopeType == null)
					throw new IllegalArgumentException("No scope-type registered for scope '" + scope + "'");
				
				if(scopeType == null)
					scopeType = isSingleton ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
				
				if(beanClass != null) {
					if(initMethodName == null && beanClass.isAssignableFrom(InitializableBean.class)) {
						initMethodName = InitializableBean.INITIALIZE_METHOD_NAME;
					}

					if(initMethodName != null) {
						if(MethodUtils.getAccessibleMethod(beanClass, initMethodName, null) == null) {
							throw new IllegalArgumentException("No such initialization method '" + initMethodName + "() on bean class: " + className);
						}
					}
					
					if(destroyMethodName == null && beanClass.isAssignableFrom(DisposableBean.class)) {
						destroyMethodName = DisposableBean.DESTROY_METHOD_NAME;
					}

					if(destroyMethodName != null) {
						if(MethodUtils.getAccessibleMethod(beanClass, destroyMethodName, null) == null) {
							throw new IllegalArgumentException("No such destroy method '" + destroyMethodName + "() on bean class: " + className);
						}
					}

					BeanRule beanRule = new BeanRule();
					beanRule.setId(id);
					beanRule.setClassName(className);
					beanRule.setBeanClass(beanClass);
					beanRule.setScopeType(scopeType);
					beanRule.setFactoryMethodName(factoryMethod);
					beanRule.setInitMethodName(initMethodName);
					beanRule.setDestroyMethodName(destroyMethodName);
					beanRule.setLazyInit(lazyInit);
					beanRule.setOverride(override);
	
					assistant.pushObject(beanRule);
				} else {
					BeanRule[] beanRules = new BeanRule[beanClassMap.size()];
					
					int i = 0;
					for(Map.Entry<String, Class<?>> entry : beanClassMap.entrySet()) {
						String beanId = entry.getKey();
						Class<?> beanClass2 = entry.getValue();
						String initMethodName2 = initMethodName;
						String destroyMethodName2 = destroyMethodName;
						
						if(initMethodName2 == null && beanClass2.isAssignableFrom(InitializableBean.class)) {
							initMethodName2 = InitializableBean.INITIALIZE_METHOD_NAME;
						}
						
						if(initMethodName2 != null) {
							if(MethodUtils.getAccessibleMethod(beanClass, initMethodName2, null) == null) {
								throw new IllegalArgumentException("No such initialization method '" + initMethodName2 + "() on bean class: " + beanClass2.getName());
							}
						}

						if(destroyMethodName2 == null && beanClass2.isAssignableFrom(DisposableBean.class)) {
							destroyMethodName2 = DisposableBean.DESTROY_METHOD_NAME;
						}

						if(destroyMethodName2 != null) {
							if(MethodUtils.getAccessibleMethod(beanClass, destroyMethodName2, null) == null) {
								throw new IllegalArgumentException("No such destroy method '" + destroyMethodName2 + "() on bean class: " + beanClass2.getName());
							}
						}

						BeanRule beanRule = new BeanRule();
						beanRule.setId(beanId);
						beanRule.setClassName(beanClass2.getName());
						beanRule.setBeanClass(beanClass2);
						beanRule.setScopeType(scopeType);
						beanRule.setFactoryMethodName(factoryMethod);
						beanRule.setInitMethodName(initMethodName2);
						beanRule.setDestroyMethodName(destroyMethodName2);
						beanRule.setLazyInit(lazyInit);
						beanRule.setOverride(override);
						
						beanRules[i++] = beanRule;
					}
	
					assistant.pushObject(beanRules);					
				}
			}
		});
		parser.addNodelet("/aspectran/bean/constructor/arguments", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});		
		
		parser.addNodelet("/aspectran/bean/constructor/arguments", new ItemRuleNodeletAdder(assistant));
		
		parser.addNodelet("/aspectran/bean/constructor/arguments/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
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
		parser.addNodelet("/aspectran/bean/properties", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});		

		parser.addNodelet("/aspectran/bean/properties", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/bean/properties/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
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
			public void process(Node node, Properties attributes, String text) throws Exception {
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
	 * Adds the translet map nodelets.
	 */
	private void addImportNodelets() {
		parser.addNodelet("/aspectran/import", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String resource = attributes.getProperty("resource");
				String file = attributes.getProperty("file");
				String url = attributes.getProperty("url");
				
				InputStreamResource inputStreamResource = new InputStreamResource();
				
				if(StringUtils.hasText(resource))
					inputStreamResource.setResource(resource);
				else if(StringUtils.hasText(file))
					inputStreamResource.setResource(file);
				else if(StringUtils.hasText(url))
					inputStreamResource.setResource(url);
				else
					throw new IllegalArgumentException("The <import> element requires either a resource or a file or a url attribute.");
				
				DefaultSettings defaultSettings = (DefaultSettings)assistant.getInheritedDefaultSettings().clone();
				
				AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(assistant);
				aspectranNodeParser.parse(inputStreamResource.getInputStream());
				
				assistant.setInheritedDefaultSettings(defaultSettings);
			}
		});
	}

}
