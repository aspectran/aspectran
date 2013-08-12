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
package com.aspectran.core.context.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.IncludeAction;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.bean.ScopedBeanRegistry;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.builder.xml.AspectranNodeParser;
import com.aspectran.core.context.builder.xml.BeansNodeParser;
import com.aspectran.core.context.builder.xml.ContextNodeParser;
import com.aspectran.core.rule.BeanRule;
import com.aspectran.core.rule.BeanRuleMap;
import com.aspectran.core.rule.IncludeActionRule;
import com.aspectran.core.rule.MultipleTransletRuleMap;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.util.ClassUtils;

/**
 * TransletsContext builder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public final class BakContextBuilder {
	
	private final Log log = LogFactory.getLog(BakContextBuilder.class);

	private String serviceRootPath;
	
	//private ClassLoader classLoader = ContextBuilder.class.getClassLoader(); 
	private ClassLoader classLoader = ClassUtils.getDefaultClassLoader(); 
	
	/**
	 * Instantiates a new translets client loader.
	 * 
	 * @param servicePath the service path
	 */
	public BakContextBuilder(String serviceRootPath) {
		this.serviceRootPath = serviceRootPath;
	}

	/**
	 * Builds an TransletsClient using the specified file path.
	 * 
	 * @param configFilePath the config file path
	 * 
	 * @return the translets client
	 * 
	 * @throws AspectranContextBuilderException the configuration exception
	 */
	public AspectranContext build(String configFilePath) throws AspectranContextBuilderException {
		try {
			log.info("Loading the Translets context '" + configFilePath + "'...");

			File file = new File(configFilePath);
			
			if(!file.isFile())
				throw new FileNotFoundException("Translets context configuration file is not found. " + configFilePath);

			InputStream inputStream = new FileInputStream(file);
			
			// Translets context loading...
			ContextNodeParser contexNodeParser = new ContextNodeParser(serviceRootPath);
			BakContextBuilderAssistant assistant = contexNodeParser.parse(inputStream);

			// Bean loading...
			BeanRuleMap beanRuleMap = new BeanRuleMap();
			List<ContextResourceFactory> beanMaps = assistant.getBeanMapResources();
			
			for(ContextResourceFactory mr : beanMaps) {
				log.info("Loading the bean-map from '" + mr.getResource() + "'...");
				
				BeansNodeParser parser = new BeansNodeParser(assistant);
				BeanRuleMap brm = parser.parse(mr.getInputStream());
				beanRuleMap.putAll(brm);
			}
			
			assistant.setBeanRuleMap(beanRuleMap);
			
			// Translet loading...
			MultipleTransletRuleMap transletRuleMap = new MultipleTransletRuleMap();
			List<ContextResourceFactory> transletMaps = assistant.getTransletMapResources();
			
			for(ContextResourceFactory mr : transletMaps) {
				log.info("Loading the translet-map from '" + mr.getResource() + "'...");

				AspectranNodeParser parser = new AspectranNodeParser(assistant);
				MultipleTransletRuleMap tm = parser.parse(mr.getInputStream());
				transletRuleMap.putAll(tm);
				
				if(log.isDebugEnabled()) {
					for(TransletRule t : tm) {
						log.debug("Describing translet:" + BakContextBuilderAssistant.LINE_SEPARATOR + t.describe());
					}
				}
			}

			// initializing beans
			log.info("Initializing beans...");
			resolveBeanClass(beanRuleMap);
			
			ScopedBeanRegistry beanRegistry = new ScopedBeanRegistry(beanRuleMap);
			
			// initializing translets 
			log.info("Initializing translets...");
			initialize(assistant, transletRuleMap);
			
			// create TransletsContext
			AspectranContext context = new AspectranContext();
			context.setServiceName(assistant.getServiceName());
			context.setTransletNamePattern(assistant.getRequestUriPattern());
			context.setTransletNamePatternPrefix(assistant.getRequestUriPatternPrefix());
			context.setTransletNamePatternSuffix(assistant.getRequestUriPatternSuffix());
			context.setTicketCheckActionList(assistant.getTicketCheckActionList());
			context.setGenericRequestRule(assistant.getGenericRequestRule());
			context.setGenericResponseRule(assistant.getGenericResponseRule());
			context.setGenericExceptionRule(assistant.getGenericExceptionRule());
			
			context.setBeanRegistry(beanRegistry);
			context.setTransletRuleMap(transletRuleMap);
			
			assistant.clearObjectStack();
			assistant.clearTypeAliases();

			return context;
		} catch(Exception e) {
			log.error("Translets configuration error.");
			throw new AspectranContextBuilderException("Translets configuration error", e);
		}
	}

	private void resolveBeanClass(BeanRuleMap beanRuleMap) throws Exception {
		for(BeanRule beanRule : beanRuleMap) {
			log.debug("init Bean " + beanRule);
			
			Class<?> beanClass = ClassUtils.resolveClassName(beanRule.getClassType(), classLoader);
			
			beanRule.setBeanClass(beanClass);
			
			if(beanRule.getInitMethod() == null && beanClass.isAssignableFrom(InitializableBean.class)) {
				beanRule.setInitMethod(InitializableBean.INITIALIZE_METHOD_NAME);
			}

			if(beanRule.getDestroyMethod() == null && beanClass.isAssignableFrom(DisposableBean.class)) {
				beanRule.setDestroyMethod(DisposableBean.DESTROY_METHOD_NAME);
			}
//			
//			
//			
//			ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
//			
//			Class<?>[] parameterTypes = new Class<?>[constructorArgumentItemRuleMap.size()];
//			Object[] args = new Object[parameterTypes.length];
//			
//			Iterator<ItemRule> iter = constructorArgumentItemRuleMap.iterator();
//			int i = 0;
//			
//			while(iter.hasNext()) {
//				ItemRule ir = iter.next();
//				Object o = valueMap.get(ir.getName());
//				
//				parameterTypes[i] = o.getClass();
//				args[i] = o;
//				
//				i++;
//			}

		}
	}

	/**
	 * Initialize all the translets.
	 * 
	 * @param assistant the config
	 * @param allTicketRuleMap the ticket rule map
	 * @param transletRuleMap the translet rule map
	 * @param allPluginRuleMap the plugin rule map
	 * 
	 * @return the translet rule map
	 */
	private MultipleTransletRuleMap initialize(BakContextBuilderAssistant assistant, MultipleTransletRuleMap transletRuleMap) throws Exception {
		for(TransletRule transletRule : transletRuleMap) {
			RequestRule requestRule = transletRule.getRequestRule();
			ResponseRule responseRule = transletRule.getResponseRule();
			
			// log
			StringBuilder sb = new StringBuilder();
			sb.append("init Translet ").append(transletRule.toString());
			if(requestRule != null)
				sb.append(", RequestRule=").append(requestRule.toString());
			if(responseRule != null)
				sb.append(", ResponseRule=").append(responseRule.toString());
			log.debug(sb.toString());
			
//			// Ticket class mapping...
//			if(ticketRuleMap != null) {
//				for(TicketBeanRule ticketRule : ticketRuleMap) {
//					TicketBeanRule tr = allTicketRuleMap.get(ticketRule.getId());
//					
//					if(tr == null) {
//						log.error("Unkown ticket '" + ticketRule.getId() + "'. TicketRule " + ticketRule);
//						throw new IllegalArgumentException("Unkown ticket '" + ticketRule.getId() + "'. TicketRule " + ticketRule);
//					}
//					
//					ticketRule.setTicketClass(tr.getTicketClass());
//				}
//			}
//			
			ContentList contentList = transletRule.getContentList();
			
			if(contentList != null) {
				for(ActionList actionList : contentList) {
					for(Executable action : actionList) {
						// TransletRule mapping for ProcessCallAction
						if(action instanceof IncludeAction) {
							IncludeAction processCallAction = (IncludeAction)action;
							IncludeActionRule processCallActionRule = processCallAction.getIncludeActionRule();
							String path = processCallActionRule.getTransletName();
							TransletRule tr;
							
							if(path == null || (tr = transletRuleMap.get(path)) == null) {
								log.error("Unknown translet path '" + path + "'. ProcessCallActionRule " + processCallActionRule);
								throw new IllegalArgumentException("Unknown translet path '" + path + "'. ProcessCallActionRule " + processCallActionRule);
							}
							
							processCallAction.setTransletRule(tr);
						}
					}
				}
			}
			
			// Response plugin class mapping...
			if(responseRule != null) {
				ResponseMap responsibleMap = responseRule.getResponseMap();
				
				for(Responsible responsible : responsibleMap) {
					ActionList actionList = responsible.getActionList();
					
					if(actionList != null) {
						for(Executable action : actionList) {
							// TransletRule mapping for ProcessCallAction
							if(action instanceof IncludeAction) {
								IncludeAction processCallAction = (IncludeAction)action;
								IncludeActionRule processCallActionRule = processCallAction.getIncludeActionRule();
								String path = processCallActionRule.getTransletName();
								TransletRule tr;
								
								if(path == null || (tr = transletRuleMap.get(path)) == null) {
									log.error("Unknown translet path '" + path + "'. ProcessCallActionRule " + processCallActionRule);
									throw new IllegalArgumentException("Unknown translet path '" + path + "'. ProcessCallActionRule " + processCallActionRule);
								}
								
								processCallAction.setTransletRule(tr);
							}
						}
					}
				}
			}
		}
		
		return transletRuleMap;
	}
	
	private Class<?> checkActionBeanClass(String classType, Map<String, Class<?>> actionBeanCache) throws Exception {
		try {
			Class<?> beanClass = actionBeanCache.get(classType);
			
			if(beanClass != null)
				return beanClass;

			beanClass = ClassUtils.resolveClassName(classType, classLoader);
			actionBeanCache.put(classType, beanClass);
			
			return beanClass;
		} catch(Exception e) {
			log.error("Invalid action-bean '" + classType + "'. Caluse: " + e.getMessage());
			throw e;
		}
	}
}
