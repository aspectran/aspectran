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
import com.aspectran.core.context.builder.AspectranSettingAssistant;
import com.aspectran.core.context.builder.ImportableResource;
import com.aspectran.core.rule.ActivityRule;
import com.aspectran.core.rule.BeanRule;
import com.aspectran.core.rule.DefaultRequestRule;
import com.aspectran.core.rule.DefaultResponseRule;
import com.aspectran.core.rule.DispatcherViewTypeRule;
import com.aspectran.core.rule.DispatcherViewsRule;
import com.aspectran.core.rule.ExceptionHandleRule;
import com.aspectran.core.rule.FileItemRule;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.rule.MultipartRequestRule;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseByContentTypeRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.ticket.TicketCheckActionList;
import com.aspectran.core.type.ActivitySettingType;
import com.aspectran.core.type.RequestMethodType;
import com.aspectran.core.type.ScopeType;
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

	private final AspectranSettingAssistant assistant;
	
	/**
	 * Instantiates a new translet map parser.
	 * 
	 * @param assistant the assistant for Context Builder
	 */
	public AspectranNodeParser(AspectranSettingAssistant assistant) {
		//super(log);
		
		this.assistant = assistant;
		this.assistant.clearObjectStack();
		this.assistant.clearTypeAliases();
		this.assistant.setNamespace(null);

		parser.setValidation(true);
		parser.setEntityResolver(new AspectranDtdResolver());

		addRootNodelets();
		addSettingsNodelets();
		addTypeAliasNodelets();
		addActivityRuleNodelets();
		addDefaultRequestRuleNodelets();
		addDefaultResponseRuleNodelets();
		addDefaultExceptionRuleNodelets();
		addAspectranNodelets();
		addBeanNodelets();
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

				ActivitySettingType settingType = null;
				
				if(name != null) {
					settingType = ActivitySettingType.valueOf(name);
					
					if(settingType == null)
						throw new IllegalArgumentException("Unkown setting name '" + name + "'");
				}
				
				assistant.putSetting(settingType, value);
			}
		});
		parser.addNodelet("/aspectran/setting/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				assistant.applyActivitySettings();
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
	
	/**
	 * Adds the activity rule nodelets.
	 */
	private void addActivityRuleNodelets() {
		parser.addNodelet("/aspectran/activityRule", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule ar = new ActivityRule();
				assistant.pushObject(ar);
			}
		});
		parser.addNodelet("/aspectran/activityRule/transletNamePattern/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule ar = (ActivityRule)assistant.peekObject();
				ar.setTransletNamePattern(text);
			}
		});
		parser.addNodelet("/aspectran/activityRule/transletNamePattern/prefix/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule ar = (ActivityRule)assistant.peekObject();
				ar.setTransletNamePatternPrefix(text);
			}
		});
		parser.addNodelet("/aspectran/activityRule/transletNamePattern/suffix/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule sr = (ActivityRule)assistant.peekObject();
				sr.setTransletNamePatternSuffix(text);
			}
		});
		parser.addNodelet("/aspectran/activityRule/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule ar = (ActivityRule)assistant.popObject();
				assistant.setActivityRule(ar);
			}
		});
	}
	
	private void addTicketCheckcaseRuleNodelets() {
		parser.addNodelet("/aspectran/ticketRule", new TicketCheckcaseRuleNodeletAdder(assistant));
	}

	/**
	 * Adds the generic request rule nodelets.
	 */
	private void addDefaultRequestRuleNodelets() {
		parser.addNodelet("/aspectran/requestRule", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				assistant.pushObject(new DefaultRequestRule());
			}
		});
		parser.addNodelet("/aspectran/requestRule/characterEncoding/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DefaultRequestRule grr = (DefaultRequestRule)assistant.peekObject();
				grr.setCharacterEncoding(text);
			}
		});
		parser.addNodelet("/aspectran/requestRule/multipart", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				MultipartRequestRule mrr = new MultipartRequestRule();
				assistant.pushObject(mrr);
			}
		});
		parser.addNodelet("/aspectran/requestRule/multipart/maxRequestSize/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				MultipartRequestRule mrr = (MultipartRequestRule)assistant.peekObject();
				mrr.setMaxRequestSize(text);
			}
		});
		parser.addNodelet("/aspectran/requestRule/multipart/temporaryFilePath/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				MultipartRequestRule mrr = (MultipartRequestRule)assistant.peekObject();
				mrr.setTemporaryFilePath(text);
			}
		});
		parser.addNodelet("/aspectran/requestRule/multipart/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				MultipartRequestRule mrr = (MultipartRequestRule)assistant.popObject();
				DefaultRequestRule drr = (DefaultRequestRule)assistant.peekObject();
				drr.setMultipartRequestRule(mrr);
			}
		});

		parser.addNodelet("/aspectran/requestRule", new TicketCheckRuleNodeletAdder(assistant));
		
		parser.addNodelet("/aspectran/requestRule/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DefaultRequestRule drr = (DefaultRequestRule)assistant.popObject();
				
				if(drr.getCharacterEncoding() != null && !Charset.isSupported(drr.getCharacterEncoding()))
					throw new IllegalCharsetNameException("Given charset name is illegal. '" + drr.getCharacterEncoding() + "'");

				assistant.setDefaultRequestRule(drr);
			}
		});
	}

	/**
	 * Adds the generic response rule nodelets.
	 */
	private void addDefaultResponseRuleNodelets() {
		parser.addNodelet("/aspectran/responseRule", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				assistant.pushObject(new DefaultResponseRule());
			}
		});
		parser.addNodelet("/aspectran/responseRule/characterEncoding/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DefaultResponseRule grr = (DefaultResponseRule)assistant.peekObject();
				grr.setCharacterEncoding(text);
			}
		});

		parser.addNodelet("/aspectran/responseRule/defaultContentType/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DefaultResponseRule grr = (DefaultResponseRule)assistant.peekObject();
				grr.setDefaultContentType(text);
			}
		});
		
		parser.addNodelet("/aspectran/responseRule/dispatcherViews", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String defaultDispatchViewId = attributes.getProperty("default");
				
				DispatcherViewsRule dispatcherViewsRule = new DispatcherViewsRule();
				dispatcherViewsRule.setDefaultDispatcherViewTypeId(defaultDispatchViewId);
				
				assistant.pushObject(dispatcherViewsRule);
			}
		});
		parser.addNodelet("/aspectran/responseRule/dispatcherViews/viewType", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String id = attributes.getProperty("id");
				String classType = resolveAliasType(attributes.getProperty("class"));
				String beanId = attributes.getProperty("bean");
				
				DispatcherViewTypeRule dispatchViewTypeRule = (DispatcherViewTypeRule)assistant.peekObject();
				dispatchViewTypeRule.setId(id);
				dispatchViewTypeRule.setClassType(classType);
				dispatchViewTypeRule.setBeanId(beanId);
			}
		});
		parser.addNodelet("/aspectran/responseRule/dispatcherViews/viewType/properties", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});

		parser.addNodelet("/aspectran/responseRule/dispatcherViews/viewType/properties", new ItemRuleNodeletAdder(assistant));
	
		parser.addNodelet("/aspectran/responseRule/dispatcherViews/viewType/properties/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					DispatcherViewTypeRule dispatcherViewTypeRule = (DispatcherViewTypeRule)assistant.peekObject();
					dispatcherViewTypeRule.setPropertyItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet("/aspectran/responseRule/dispatcherViews/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DispatcherViewsRule dispatcherViewsRule = (DispatcherViewsRule)assistant.popObject();
				DefaultResponseRule drr = (DefaultResponseRule)assistant.peekObject();
				drr.setDispatcherViewsRule(dispatcherViewsRule);
			}
		});
		parser.addNodelet("/aspectran/responseRule/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DefaultResponseRule drr = (DefaultResponseRule)assistant.popObject();
				
				if(drr.getCharacterEncoding() != null && !Charset.isSupported(drr.getCharacterEncoding()))
					throw new IllegalCharsetNameException("Given charset name is illegal. '" + drr.getCharacterEncoding() + "'");
				
				assistant.setDefaultResponseRule(drr);
			}
		});
	}

	/**
	 * Adds the generic exception rule nodelets.
	 */
	private void addDefaultExceptionRuleNodelets() {
		parser.addNodelet("/aspectran/exceptionRule", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ExceptionHandleRule exceptionRule = new ExceptionHandleRule();
				assistant.pushObject(exceptionRule);
			}
		});

		parser.addNodelet("/aspectran/exceptionRule/responseByContentType", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/exceptionRule/defaultResponse", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule responseByContentType = new ResponseByContentTypeRule();
				assistant.pushObject(responseByContentType);
			}
		});

		parser.addNodelet("/aspectran/exceptionRule/defaultResponse", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/exceptionRule/defaultResponse/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule responseByContentType = (ResponseByContentTypeRule)assistant.popObject();
				ResponseMap responseMap = responseByContentType.getResponseMap();
				
				if(responseMap.size() > 0) {
					ExceptionHandleRule exceptionRule = (ExceptionHandleRule)assistant.peekObject();
					exceptionRule.setDefaultResponse(responseMap.get(0));
				}
			}
		});
		parser.addNodelet("/aspectran/exceptionRule/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ExceptionHandleRule exceptionRule = (ExceptionHandleRule)assistant.popObject();
				assistant.setDefaultExceptionRule(exceptionRule);
			}
		});
	}

	/**
	 * Adds the translet nodelets.
	 */
	private void addAspectranNodelets() {
		parser.addNodelet("/aspectran/translet", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");

				if(name == null)
					throw new IllegalArgumentException("The <translet> element requires a name attribute.");

				name = assistant.applyNamespaceForTranslet(name);

				TransletRule transletRule = new TransletRule();
				transletRule.setName(name);

				assistant.pushObject(transletRule);
			}
		});
		
		parser.addNodelet("/aspectran/translet", new TicketCheckRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/request", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String method = attributes.getProperty("method");
				String characterEncoding = attributes.getProperty("characterEncoding");

				RequestMethodType methodType = null;
				
				if(method != null) {
					methodType = RequestMethodType.valueOf(method);
					
					if(methodType == null)
						throw new IllegalArgumentException("Unkown request method type '" + method + "'");
				}
				
				RequestRule requestRule;

				if(assistant.getDefaultRequestRule() != null)
					requestRule = new RequestRule(assistant.getDefaultRequestRule());
				else
					requestRule = new RequestRule();
				
				requestRule.setMethod(methodType);
				requestRule.setCharacterEncoding(characterEncoding);
				
				assistant.pushObject(requestRule);
			}
		});

		parser.addNodelet("/aspectran/translet/request", new TicketCheckRuleNodeletAdder(assistant));
		
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
				
				// default request rule mapping...
				DefaultRequestRule drr = assistant.getDefaultRequestRule();
				
				requestRule.setMultipartRequestRule(drr.getMultipartRequestRule());
				
				if(requestRule.getCharacterEncoding() == null)
					requestRule.setCharacterEncoding(drr.getCharacterEncoding());
				// ...............................

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

				ResponseRule responseRule = new ResponseRule(assistant.getDefaultResponseRule());
				responseRule.setDefaultResponseId(defaultResponseId);
				responseRule.setCharacterEncoding(characterEncoding);

				assistant.pushObject(responseRule);
			}
		});

		parser.addNodelet("/aspectran/translet/response", new TicketCheckRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/response", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/response/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseRule responseRule = (ResponseRule)assistant.popObject();

				// default response rule mapping...
				DefaultResponseRule drr = assistant.getDefaultResponseRule();
				
				if(responseRule.getCharacterEncoding() == null)
					responseRule.setCharacterEncoding(drr.getCharacterEncoding());
				
				if(responseRule.getDefaultContentType() == null)
					responseRule.setDefaultContentType(drr.getDefaultContentType());
				// ...............................
				
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.setResponseRule(responseRule);
			}
		});
		parser.addNodelet("/aspectran/translet/exception", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ExceptionHandleRule exceptionRule = new ExceptionHandleRule();
				assistant.pushObject(exceptionRule);
			}
		});

		parser.addNodelet("/aspectran/translet/exception/responseByContentType", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/exception/defaultResponse", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule responseByContentType = new ResponseByContentTypeRule();
				assistant.pushObject(responseByContentType);
			}
		});

		parser.addNodelet("/aspectran/translet/exception/defaultResponse", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/aspectran/translet/exception/defaultResponse/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule responseByContentType = (ResponseByContentTypeRule)assistant.popObject();
				ResponseMap responseMap = responseByContentType.getResponseMap();
				
				if(responseMap.size() > 0) {
					ExceptionHandleRule exceptionRule = (ExceptionHandleRule)assistant.peekObject();
					exceptionRule.setDefaultResponse(responseMap.get(0));
				}
			}
		});
		parser.addNodelet("/aspectran/translet/exception/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ExceptionHandleRule exceptionRule = (ExceptionHandleRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();

				transletRule.setExceptionHandleRule(exceptionRule);
			}
		});
		parser.addNodelet("/aspectran/translet/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				TransletRule transletRule = (TransletRule)assistant.popObject();
				
				if(transletRule.getTicketCheckActionList() == null) {
					transletRule.setTicketCheckActionList(assistant.getTicketCheckActionList());
				} else if(assistant.getTicketCheckActionList() != null) {
					TicketCheckActionList tcal = transletRule.getTicketCheckActionList();
					tcal.addAll(0, assistant.getTicketCheckActionList());
				}
				
				if(transletRule.getRequestRule() == null) {
					RequestRule requestRule;

					if(assistant.getDefaultRequestRule() != null)
						requestRule = new RequestRule(assistant.getDefaultRequestRule());
					else
						requestRule = new RequestRule();
					
					transletRule.setRequestRule(requestRule);
				}
				
				if(transletRule.getResponseRule() == null) {
					ResponseRule responseRule;

					if(assistant.getDefaultRequestRule() != null)
						responseRule = new ResponseRule(assistant.getDefaultResponseRule());
					else
						responseRule = new ResponseRule();

					transletRule.setResponseRule(responseRule);
				}
				
				// default exception rule mapping...
				if(transletRule.getExceptionHandleRule() == null)
					transletRule.setExceptionHandleRule(assistant.getDefaultExceptionRule());

				assistant.addTransletRule(transletRule);

				if(assistant.isMultiActivityEnable()) {
					ResponseMap responseMap = transletRule.getResponseRule().getResponseMap();
					
					for(Responsible response : responseMap) {
						String responseId = response.getId();
						
						if(!ResponseRule.DEFAULT_ID.equals(responseId)) {
							String transletName = assistant.replaceTransletNameSuffix(transletRule.getName(), responseId);
							assistant.addMultiActivityTransletRule(transletName, responseId, transletRule);
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
					if(assistant.isNullableBeanId()) {
						// When the bean id is null, the namespace does not apply.
						id = classType;
					} else {
						throw new IllegalArgumentException("The <bean> element requires a id attribute.");
					}
				} else {
					id = assistant.applyNamespaceForBean(id);
				}

				if(classType == null)
					throw new IllegalArgumentException("The <bean> element requires a class attribute.");
				
				boolean isSingleton = !(singleton != null && Boolean.valueOf(singleton) == Boolean.FALSE);
				ScopeType scopeType = ScopeType.valueOf(scope);
				
				if(scope != null && scopeType == null)
					throw new IllegalArgumentException("No scope-type registered for scope '" + scope + "'");
				
				if(scopeType == null)
					scopeType = isSingleton ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
				
				BeanRule beanRule = new BeanRule();
				beanRule.setId(id);
				beanRule.setClassType(classType);
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
				
				ImportableResource r = new ImportableResource();
				
				if(!StringUtils.isEmpty(resource))
					r.setResource(resource);
				else if(!StringUtils.isEmpty(file))
					r.setResource(file);
				else if(!StringUtils.isEmpty(url))
					r.setResource(url);
				else
					throw new IllegalArgumentException("The <import> element requires either a resource or a file or a url attribute.");
				
				assistant.addResource(r);
			}
		});
	}

}
