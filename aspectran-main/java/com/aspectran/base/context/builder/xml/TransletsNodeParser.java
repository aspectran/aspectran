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
package com.aspectran.base.context.builder.xml;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Properties;

import org.w3c.dom.Node;

import com.aspectran.base.context.builder.ActivityContextBuilderAssistant;
import com.aspectran.base.io.Resource;
import com.aspectran.base.rule.ActivityRule;
import com.aspectran.base.rule.BeanRule;
import com.aspectran.base.rule.DefaultRequestRule;
import com.aspectran.base.rule.DefaultResponseRule;
import com.aspectran.base.rule.DispatcherViewTypeRule;
import com.aspectran.base.rule.DispatcherViewsRule;
import com.aspectran.base.rule.ExceptionHandleRule;
import com.aspectran.base.rule.FileItemRule;
import com.aspectran.base.rule.ItemRuleMap;
import com.aspectran.base.rule.MultipartRequestRule;
import com.aspectran.base.rule.RequestRule;
import com.aspectran.base.rule.ResponseByContentTypeRule;
import com.aspectran.base.rule.ResponseRule;
import com.aspectran.base.rule.TransletRule;
import com.aspectran.base.type.RequestMethodType;
import com.aspectran.base.type.ScopeType;
import com.aspectran.base.util.StringUtils;
import com.aspectran.base.util.xml.Nodelet;
import com.aspectran.base.util.xml.NodeletParser;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.ticket.TicketCheckActionList;

/**
 * Translet Map Parser.
 * 
 * <p>Created: 2008. 06. 14 오전 4:39:24</p>
 */
public class TransletsNodeParser {
	
	private final NodeletParser parser = new NodeletParser();

	private final ActivityContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new translet map parser.
	 * 
	 * @param assistant the assistant for Context Builder
	 */
	public TransletsNodeParser(ActivityContextBuilderAssistant assistant) {
		//super(log);
		
		this.assistant = assistant;
		this.assistant.clearObjectStack();
		this.assistant.clearTypeAliases();
		this.assistant.setNamespace(null);

		parser.setValidation(true);
		parser.setEntityResolver(new TransletsDtdResolver());

		addRootNodelets();
		addSettingsNodelets();
		addTypeAliasNodelets();
		addActivityRuleNodelets();
		addTicketCheckcaseRuleNodelets();
		addDefaultRequestRuleNodelets();
		addDefaultResponseRuleNodelets();
		addDefaultExceptionRuleNodelets();
		addTransletNodelets();
		addBeanNodelets();
		addImportNodelets();
	}

	/**
	 * Parses the translet map.
	 * 
	 * @param inputStream the input stream
	 * 
	 * @return the translet rule map
	 * 
	 * @throws Exception the exception
	 */
	public void parse(InputStream inputStream) throws Exception {
		try {
			parser.parse(inputStream);
		} catch(Exception e) {
			throw new Exception("Error parsing translet-map. Cause: " + e, e);
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
	 * Adds the translet map nodelets.
	 */
	private void addRootNodelets() {
		parser.addNodelet("/translets", new Nodelet() {
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
		parser.addNodelet("/translets/setting", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				String value = attributes.getProperty("value");
				
				assistant.putSetting(name, value);
			}
		});
		parser.addNodelet("/translets/setting/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				assistant.applySettings();
			}
		});
	}

	/**
	 * Adds the type alias nodelets.
	 */
	private void addTypeAliasNodelets() {
		parser.addNodelet("/translets/typeAlias", new Nodelet() {
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
		parser.addNodelet("/translets/activityRule", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule ar = new ActivityRule();
				assistant.pushObject(ar);
			}
		});
		parser.addNodelet("/translets/activityRule/transletNamePattern/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule ar = (ActivityRule)assistant.peekObject();
				ar.setTransletPathPattern(text);
			}
		});
		parser.addNodelet("/translets/activityRule/transletNamePattern/prefix/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule ar = (ActivityRule)assistant.peekObject();
				ar.setTransletNamePatternPrefix(text);
			}
		});
		parser.addNodelet("/translets/activityRule/transletNamePattern/suffix/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule sr = (ActivityRule)assistant.peekObject();
				sr.setTransletPathPatternSuffix(text);
			}
		});
		parser.addNodelet("/translets/activityRule/description/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule sr = (ActivityRule)assistant.peekObject();
				sr.setDescription(text);
			}
		});
		parser.addNodelet("/translets/activityRule/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActivityRule ar = (ActivityRule)assistant.popObject();
				assistant.setActivityRule(ar);
			}
		});
	}
	
	private void addTicketCheckcaseRuleNodelets() {
		parser.addNodelet("/translets/ticketRule", new TicketCheckcaseRuleNodeletAdder(assistant));
	}

	/**
	 * Adds the generic request rule nodelets.
	 */
	private void addDefaultRequestRuleNodelets() {
		parser.addNodelet("/translets/requestRule", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				assistant.pushObject(new DefaultRequestRule());
			}
		});
		parser.addNodelet("/translets/requestRule/characterEncoding/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DefaultRequestRule grr = (DefaultRequestRule)assistant.peekObject();
				grr.setCharacterEncoding(text);
			}
		});
		parser.addNodelet("/translets/requestRule/multipart", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				MultipartRequestRule mrr = new MultipartRequestRule();
				assistant.pushObject(mrr);
			}
		});
		parser.addNodelet("/translets/requestRule/multipart/maxRequestSize/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				MultipartRequestRule mrr = (MultipartRequestRule)assistant.peekObject();
				mrr.setMaxRequestSize(text);
			}
		});
		parser.addNodelet("/translets/requestRule/multipart/temporaryFilePath/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				MultipartRequestRule mrr = (MultipartRequestRule)assistant.peekObject();
				mrr.setTemporaryFilePath(text);
			}
		});
		parser.addNodelet("/translets/requestRule/multipart/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				MultipartRequestRule mrr = (MultipartRequestRule)assistant.popObject();
				DefaultRequestRule drr = (DefaultRequestRule)assistant.peekObject();
				drr.setMultipartRequestRule(mrr);
			}
		});

		parser.addNodelet("/translets/requestRule", new TicketCheckRuleNodeletAdder(assistant));
		
		parser.addNodelet("/translets/requestRule/end()", new Nodelet() {
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
		parser.addNodelet("/translets/responseRule", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				assistant.pushObject(new DefaultResponseRule());
			}
		});
		parser.addNodelet("/translets/responseRule/characterEncoding/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DefaultResponseRule grr = (DefaultResponseRule)assistant.peekObject();
				grr.setCharacterEncoding(text);
			}
		});

		parser.addNodelet("/translets/responseRule/defaultContentType/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DefaultResponseRule grr = (DefaultResponseRule)assistant.peekObject();
				grr.setDefaultContentType(text);
			}
		});
		
		parser.addNodelet("/translets/responseRule/dispatcherViews", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String defaultDispatchViewId = attributes.getProperty("default");
				
				DispatcherViewsRule dispatcherViewsRule = new DispatcherViewsRule();
				dispatcherViewsRule.setDefaultDispatcherViewTypeId(defaultDispatchViewId);
				
				assistant.pushObject(dispatcherViewsRule);
			}
		});
		parser.addNodelet("/translets/responseRule/dispatcherViews/viewType", new Nodelet() {
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
		parser.addNodelet("/translets/responseRule/dispatcherViews/viewType/properties", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});

		parser.addNodelet("/translets/responseRule/dispatcherViews/viewType/properties", new ItemRuleNodeletAdder(assistant));
	
		parser.addNodelet("/translets/responseRule/dispatcherViews/viewType/properties/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				
				if(irm.size() > 0) {
					DispatcherViewTypeRule dispatcherViewTypeRule = (DispatcherViewTypeRule)assistant.peekObject();
					dispatcherViewTypeRule.setPropertyItemRuleMap(irm);
				}
			}
		});
		parser.addNodelet("/translets/responseRule/dispatcherViews/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				DispatcherViewsRule dispatcherViewsRule = (DispatcherViewsRule)assistant.popObject();
				DefaultResponseRule drr = (DefaultResponseRule)assistant.peekObject();
				drr.setDispatcherViewsRule(dispatcherViewsRule);
			}
		});
		parser.addNodelet("/translets/responseRule/end()", new Nodelet() {
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
		parser.addNodelet("/translets/exceptionRule", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ExceptionHandleRule exceptionRule = new ExceptionHandleRule();
				assistant.pushObject(exceptionRule);
			}
		});

		parser.addNodelet("/translets/exceptionRule/responseByContentType", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/exceptionRule/defaultResponse", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule responseByContentType = new ResponseByContentTypeRule();
				assistant.pushObject(responseByContentType);
			}
		});

		parser.addNodelet("/translets/exceptionRule/defaultResponse", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/exceptionRule/defaultResponse/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule responseByContentType = (ResponseByContentTypeRule)assistant.popObject();
				ResponseMap responseMap = responseByContentType.getResponseMap();
				
				if(responseMap.size() > 0) {
					ExceptionHandleRule exceptionRule = (ExceptionHandleRule)assistant.peekObject();
					exceptionRule.setDefaultResponse(responseMap.get(0));
				}
			}
		});
		parser.addNodelet("/translets/exceptionRule/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ExceptionHandleRule exceptionRule = (ExceptionHandleRule)assistant.popObject();
				assistant.setDefaultExceptionRule(exceptionRule);
			}
		});
	}

	/**
	 * Adds the translet nodelets.
	 */
	private void addTransletNodelets() {
		parser.addNodelet("/translets/translet", new Nodelet() {
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
		
		parser.addNodelet("/translets/translet", new TicketCheckRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/translet", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/translet/request", new Nodelet() {
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

		parser.addNodelet("/translets/translet/request", new TicketCheckRuleNodeletAdder(assistant));
		
		parser.addNodelet("/translets/translet/request/attributes", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});		
		
		parser.addNodelet("/translets/translet/request/attributes", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/translet/request/attributes/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				RequestRule requestRule = (RequestRule)assistant.peekObject();
				requestRule.setAttributeItemRuleMap(irm);
			}
		});		
		parser.addNodelet("/translets/translet/request/multiparts/fileItem", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				
				FileItemRule fir = new FileItemRule();
				fir.setName(name);

				assistant.pushObject(fir);
			}
		});
		parser.addNodelet("/translets/translet/request/multiparts/fileItem/allowFileExtentions/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				if(text != null) {
					FileItemRule fir = (FileItemRule)assistant.peekObject();
					fir.setAllowFileExtentions(text);
				}
			}
		});
		parser.addNodelet("/translets/translet/request/multiparts/fileItem/denyFileExtentions/text()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				if(text != null) {
					FileItemRule fir = (FileItemRule)assistant.peekObject();
					fir.setDenyFileExtentions(text);
				}
			}
		});
		parser.addNodelet("/translets/translet/request/multiparts/fileItem/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				FileItemRule fir = (FileItemRule)assistant.popObject();
				RequestRule requestRule = (RequestRule)assistant.peekObject();
				requestRule.addFileItemRule(fir);
			}
		});
		parser.addNodelet("/translets/translet/request/end()", new Nodelet() {
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
		parser.addNodelet("/translets/translet/contents", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ContentList contentList = new ContentList();
				assistant.pushObject(contentList);
			}
		});
		parser.addNodelet("/translets/translet/contents/content", new Nodelet() {
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

		parser.addNodelet("/translets/translet/contents/content", new ActionRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/translet/contents/content/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ActionList actionList = (ActionList)assistant.popObject();
				ContentList contentList = (ContentList)assistant.peekObject();
				contentList.addActionList(actionList);
			}
		});
		parser.addNodelet("/translets/translet/contents/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ContentList contentList = (ContentList)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();
				transletRule.setContentList(contentList);
			}
		});
		parser.addNodelet("/translets/translet/response", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String defaultResponseId = attributes.getProperty("default");
				String characterEncoding = attributes.getProperty("characterEncoding");

				ResponseRule responseRule = new ResponseRule(assistant.getDefaultResponseRule());
				responseRule.setDefaultResponseId(defaultResponseId);
				responseRule.setCharacterEncoding(characterEncoding);

				assistant.pushObject(responseRule);
			}
		});

		parser.addNodelet("/translets/translet/response", new TicketCheckRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/translet/response", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/translet/response/end()", new Nodelet() {
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
		parser.addNodelet("/translets/translet/exception", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ExceptionHandleRule exceptionRule = new ExceptionHandleRule();
				assistant.pushObject(exceptionRule);
			}
		});

		parser.addNodelet("/translets/translet/exception/responseByContentType", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/translet/exception/defaultResponse", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule responseByContentType = new ResponseByContentTypeRule();
				assistant.pushObject(responseByContentType);
			}
		});

		parser.addNodelet("/translets/translet/exception/defaultResponse", new ResponseRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/translet/exception/defaultResponse/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ResponseByContentTypeRule responseByContentType = (ResponseByContentTypeRule)assistant.popObject();
				ResponseMap responseMap = responseByContentType.getResponseMap();
				
				if(responseMap.size() > 0) {
					ExceptionHandleRule exceptionRule = (ExceptionHandleRule)assistant.peekObject();
					exceptionRule.setDefaultResponse(responseMap.get(0));
				}
			}
		});
		parser.addNodelet("/translets/translet/exception/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ExceptionHandleRule exceptionRule = (ExceptionHandleRule)assistant.popObject();
				TransletRule transletRule = (TransletRule)assistant.peekObject();

				transletRule.setExceptionHandleRule(exceptionRule);
			}
		});
		parser.addNodelet("/translets/translet/end()", new Nodelet() {
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
		parser.addNodelet("/translets/bean", new Nodelet() {
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
		parser.addNodelet("/translets/bean/constructor/arguments", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});		
		
		parser.addNodelet("/translets/bean/constructor/arguments", new ItemRuleNodeletAdder(assistant));
		
		parser.addNodelet("/translets/bean/constructor/arguments/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				BeanRule beanRule = (BeanRule)assistant.peekObject();
				beanRule.setConstructorArgumentItemRuleMap(irm);
			}
		});		
		parser.addNodelet("/translets/bean/properties", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = new ItemRuleMap();
				assistant.pushObject(irm);
			}
		});		

		parser.addNodelet("/translets/bean/properties", new ItemRuleNodeletAdder(assistant));

		parser.addNodelet("/translets/bean/properties/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRuleMap irm = (ItemRuleMap)assistant.popObject();
				BeanRule beanRule = (BeanRule)assistant.peekObject();
				beanRule.setPropertyItemRuleMap(irm);
			}
		});
		parser.addNodelet("/translets/bean/end()", new Nodelet() {
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
		parser.addNodelet("/translets/import", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String resource = attributes.getProperty("resource");
				String file = attributes.getProperty("file");
				String url = attributes.getProperty("url");
				
				Resource r = new Resource();
				
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
