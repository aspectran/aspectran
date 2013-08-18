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
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Properties;

import org.w3c.dom.Node;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.builder.AspectranContextBuildingAssistant;
import com.aspectran.core.context.builder.ContextResourceFactory;
import com.aspectran.core.context.builder.InheritedAspectranSettings;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.BeanRule;
import com.aspectran.core.rule.FileItemRule;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseByContentTypeRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.type.AspectAdviceType;
import com.aspectran.core.type.AspectranSettingType;
import com.aspectran.core.type.JoinpointScopeType;
import com.aspectran.core.type.JoinpointTargetType;
import com.aspectran.core.type.PointcutType;
import com.aspectran.core.type.RequestMethodType;
import com.aspectran.core.type.ScopeType;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * Translet Map Parser.
 * 
 * <p>Created: 2008. 06. 14 오전 4:39:24</p>
 */
public class AspectranNodeParser {
	
	private final NodeletParser parser = new NodeletParser();

	private final AspectranContextBuildingAssistant assistant;
	
	private final ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
	
	/**
	 * Instantiates a new translet map parser.
	 * 
	 * @param assistant the assistant for Context Builder
	 */
	public AspectranNodeParser(AspectranContextBuildingAssistant assistant) {
		this.assistant = assistant;
		//this.assistant.clearObjectStack();
		//this.assistant.clearTypeAliases();
		this.assistant.setNamespace(null);

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
		parser.addNodelet("/aspectran/setting", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				String value = attributes.getProperty("value");

				AspectranSettingType settingType = null;
				
				if(name != null) {
					settingType = AspectranSettingType.valueOf(name);
					
					if(settingType == null)
						throw new IllegalArgumentException("Unknown setting name '" + name + "'");
				}
				
				assistant.putSetting(settingType, value);
			}
		});
		parser.addNodelet("/aspectran/setting/end()", new Nodelet() {
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
				
				if(type != null) {
					pointcutType = PointcutType.valueOf(type);
					
					if(pointcutType == null)
						throw new IllegalArgumentException("Unknown pointcut type '" + type + "'");
				} else {
					if(aspectRule.getJoinpointTarget() == JoinpointTargetType.SCHEDULER)
						pointcutType = PointcutType.INTERVAL;
					else
						pointcutType = PointcutType.WILDCARD;
				}
				
				PointcutRule pointcutRule = new PointcutRule();
				pointcutRule.setPointcutType(pointcutType);
				pointcutRule.setPatternString(text);
				
				aspectRule.setPointcutRule(pointcutRule);
			}
		});
		
		parser.addNodelet("/aspectran/aspect/before", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.BEFORE));
		parser.addNodelet("/aspectran/aspect/after", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.AFTER));
		parser.addNodelet("/aspectran/aspect/around", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.AROUND));
		parser.addNodelet("/aspectran/aspect/finally", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.FINALLY));
		parser.addNodelet("/aspectran/aspect/exceptionRaized", new AspectAdviceRuleNodeletAdder(assistant, AspectAdviceType.EXCPETION_RAIZED));
		
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
				String parentTransletName = attributes.getProperty("extends");

				if(name == null)
					throw new IllegalArgumentException("The <translet> element requires a name attribute.");

				name = assistant.applyNamespaceForTranslet(name);
				
				if(parentTransletName != null && parentTransletName.startsWith("."))
					parentTransletName = assistant.applyNamespaceForTranslet(parentTransletName.substring(1));

				TransletRule transletRule = new TransletRule();
				transletRule.setName(name);
				transletRule.setParentTransletName(parentTransletName);

				assistant.pushObject(transletRule);
			}
		});
		
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
		parser.addNodelet("/aspectran/translet/request/multiparts/fileItem/allowFileExtentions/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				if(text != null) {
					FileItemRule fir = (FileItemRule)assistant.peekObject();
					fir.setAllowFileExtentions(text);
				}
			}
		});
		parser.addNodelet("/aspectran/translet/request/multiparts/fileItem/denyFileExtentions/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				if(text != null) {
					FileItemRule fir = (FileItemRule)assistant.peekObject();
					fir.setDenyFileExtentions(text);
				}
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
				String defaultResponseId = attributes.getProperty("default");
				String characterEncoding = attributes.getProperty("characterEncoding");

				if(characterEncoding != null && !Charset.isSupported(characterEncoding))
					throw new IllegalCharsetNameException("Given charset name is illegal. '" + characterEncoding + "'");
				
				ResponseRule responseRule = new ResponseRule();
				responseRule.setDefaultResponseId(defaultResponseId);
				responseRule.setCharacterEncoding(characterEncoding);

				assistant.pushObject(responseRule);
			}
		});

		parser.addNodelet("/aspectran/translet/response", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/response/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseRule responseRule = (ResponseRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.setResponseRule(responseRule);
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
				
				assistant.addTransletRule(transletRule);

				if(assistant.isMultipleTransletEnable()) {
					ResponseMap responseMap = transletRule.getResponseRule().getResponseMap();
					
					for(Responsible response : responseMap) {
						String responseId = response.getId();
						
						if(!ResponseRule.DEFAULT_ID.equals(responseId)) {
							String transletName = assistant.replaceTransletNameSuffix(transletRule.getName(), responseId);
							
							TransletRule transletRule2 = (TransletRule)transletRule.clone();
							transletRule2.setName(transletName);
							transletRule2.setMultipleTransletResponseId(responseId);
							
							assistant.addTransletRule(transletRule2);
						}
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
				String classType = resolveAliasType(attributes.getProperty("class"));
				String singleton = attributes.getProperty("singleton");
				String scope = attributes.getProperty("scope");
				String factoryMethod = attributes.getProperty("factoryMethod");
				String initMethod = attributes.getProperty("initMethod");
				String destroyMethod = attributes.getProperty("destroyMethod");
				Boolean lazyInit = Boolean.valueOf(attributes.getProperty("lazyInit"));

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

				if(classType == null)
					throw new IllegalArgumentException("The <bean> element requires a class attribute.");

				Class<?> beanClass = classLoader.loadClass(classType);
				
				if(initMethod == null && beanClass.isAssignableFrom(InitializableBean.class)) {
					initMethod = InitializableBean.INITIALIZE_METHOD_NAME;
				}

				if(destroyMethod == null && beanClass.isAssignableFrom(DisposableBean.class)) {
					destroyMethod = DisposableBean.DESTROY_METHOD_NAME;
				}
				
				boolean isSingleton = !(singleton != null && Boolean.valueOf(singleton) == Boolean.FALSE);
				ScopeType scopeType = ScopeType.valueOf(scope);
				
				if(scope != null && scopeType == null)
					throw new IllegalArgumentException("No scope-type registered for scope '" + scope + "'");
				
				if(scopeType == null)
					scopeType = isSingleton ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
				
				BeanRule beanRule = new BeanRule();
				beanRule.setId(id);
				beanRule.setClassType(classType);
				beanRule.setBeanClass(beanClass);
				beanRule.setScopeType(scopeType);
				beanRule.setFactoryMethod(factoryMethod);
				beanRule.setInitMethod(initMethod);
				beanRule.setDestroyMethod(destroyMethod);
				beanRule.setLazyInit(lazyInit);

				assistant.pushObject(beanRule);
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
				BeanRule beanRule = (BeanRule)assistant.peekObject();
				beanRule.setConstructorArgumentItemRuleMap(irm);
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
				BeanRule beanRule = (BeanRule)assistant.peekObject();
				beanRule.setPropertyItemRuleMap(irm);
			}
		});
		parser.addNodelet("/aspectran/bean/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				BeanRule beanRule = (BeanRule)assistant.popObject();
				assistant.addBeanRule(beanRule);
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
				
				ContextResourceFactory r = new ContextResourceFactory();
				
				if(!StringUtils.isEmpty(resource))
					r.setResource(resource);
				else if(!StringUtils.isEmpty(file))
					r.setResource(file);
				else if(!StringUtils.isEmpty(url))
					r.setResource(url);
				else
					throw new IllegalArgumentException("The <import> element requires either a resource or a file or a url attribute.");
				
				InheritedAspectranSettings inheritedSettings = assistant.getInheritedAspectranSettings();
				
				AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(assistant);
				aspectranNodeParser.parse(r.getInputStream());
				
				assistant.setActivitySettingsRule(inheritedSettings);
			}
		});
	}

}
