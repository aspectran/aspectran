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
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.builder.xml.AspectranNodeParser;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.rule.AspectranSettingsRule;
import com.aspectran.core.rule.BeanRuleMap;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.TransletRuleMap;

/**
 * TransletsContext builder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public class AspectranContextBuilder {
	
	private final Log log = LogFactory.getLog(AspectranContextBuilder.class);

	private String serviceRootPath;
	
	private BeanRuleMap beanRuleMap;
	
	/**
	 * Instantiates a new translets client loader.
	 * 
	 * @param servicePath the service path
	 */
	public AspectranContextBuilder(String serviceRootPath) {
		this.serviceRootPath = serviceRootPath;
	}
	
	public BeanRuleMap getBeanRuleMap() {
		return beanRuleMap;
	}

	public void setBeanRuleMap(BeanRuleMap beanRuleMap) {
		this.beanRuleMap = beanRuleMap;
	}

	public AspectranContext build(String contextConfigLocation) throws AspectranContextBuilderException {
		ImportableResource resource = null;
		
		try {
			File file = new File(contextConfigLocation);
			
			if(!file.isFile())
				throw new FileNotFoundException("aspectran context configuration file is not found. " + contextConfigLocation);
			
			resource = new ImportableResource();
			resource.setFile(file);
		} catch(Exception e) {
			log.error(e);
			throw new AspectranContextBuilderException(e);
		}
		
		AspectranSettingAssistant assistant = null;
		assistant = build(resource, assistant);

		BeanRuleMap beanRuleMap = assistant.getBeanRuleMap();
		TransletRuleMap transletRuleMap = assistant.getTransletRuleMap();
		
		BeanRegistry beanRegistry = buildBeanRegistry(assistant);
		TransletRuleRegistry transletRegistry = buildTransletRegistry(assistant);
		
		// create ActivityContext
		AspectranContext context = new AspectranContext(assistant.getActivitySettingsRule());
		context.setBeanRegistry(beanRegistry);
		context.setTransletRegistry(transletRegistry);
		//context.setBeanFactory(beanRegistry);
		//context.setTransletRuleMap(assistant.getTransletRuleMap());
		
		
		
		assistant.clearObjectStack();
		assistant.clearTypeAliases();

		return context;
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
	private AspectranSettingAssistant build(ImportableResource resource, AspectranSettingAssistant parentAssistant) throws AspectranContextBuilderException {
		try {
			AspectranSettingAssistant assistant;
			
			if(parentAssistant == null)
				assistant = new AspectranSettingAssistant(serviceRootPath);
			else
				assistant = new AspectranSettingAssistant(parentAssistant);

			assistant.setBeanRuleMap(beanRuleMap);

			AspectranSettingsRule oldActivitySettingsRule = assistant.getActivitySettingsRule();

			// Translet loading...
			AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(assistant);
			aspectranNodeParser.parse(resource.getInputStream());

			assistant.clearTypeAliases();
			
			List<ImportableResource> resources = assistant.getResources();
			
			for(ImportableResource r : resources) {
				log.info("Loading the translets from '" + r.getResource() + "'...");
				build(r, assistant);
			}
			
			assistant.setActivitySettingsRule(oldActivitySettingsRule);
			
			// initializing translets 
			log.info("Initializing translets...");
			initialize(assistant.getTransletRuleMap());

			if(log.isDebugEnabled()) {
				for(TransletRule t : assistant.getTransletRuleMap()) {
					log.debug("Describing translet:" + AspectranContextConstant.LINE_SEPARATOR + t.describe());
				}
			}
			
			return assistant;
			
		} catch(Exception e) {
			log.error("Translets configuration error." + resource);
			throw new AspectranContextBuilderException("Translets configuration error" + resource, e);
		}
	}
	
	private TransletRuleRegistry buildTransletRegistry(AspectranSettingAssistant sssistant) {
		return null;
	}

	private BeanRegistry buildBeanRegistry(AspectranSettingAssistant sssistant) {
		return null;
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
	private TransletRuleMap initialize(TransletRuleMap transletRuleMap) throws Exception {
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
			
			ContentList contentList = transletRule.getContentList();
			
			if(contentList != null) {
				for(ActionList actionList : contentList) {
					for(Executable action : actionList) {
						// TransletRule mapping for ProcessCallAction
						// 맵핑 불필요 정책
//						if(action instanceof ProcessCallAction) {
//							ProcessCallAction processCallAction = (ProcessCallAction)action;
//							ProcessCallActionRule processCallActionRule = processCallAction.getProcessCallActionRule();
//							String path = processCallActionRule.getPath();
//							TransletRule tr;
//							
//							if(path == null || (tr = transletRuleMap.get(path)) == null) {
//								log.error("Unknown translet path '" + path + "'. ProcessCallActionRule " + processCallActionRule);
//								throw new IllegalArgumentException("Unknown translet path '" + path + "'. ProcessCallActionRule " + processCallActionRule);
//							}
//							
//							processCallAction.setTransletRule(tr);
//						}
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
//							if(action instanceof ProcessCallAction) {
//								ProcessCallAction processCallAction = (ProcessCallAction)action;
//								ProcessCallActionRule processCallActionRule = processCallAction.getProcessCallActionRule();
//								String path = processCallActionRule.getPath();
//								TransletRule tr;
//								
//								if(path == null || (tr = transletRuleMap.get(path)) == null) {
//									log.error("Unknown translet path '" + path + "'. ProcessCallActionRule " + processCallActionRule);
//									throw new IllegalArgumentException("Unknown translet path '" + path + "'. ProcessCallActionRule " + processCallActionRule);
//								}
//								
//								processCallAction.setTransletRule(tr);
//							}
						}
					}
				}
			}
		}
		
		return transletRuleMap;
	}
	
//	public AspectranContext build(String contextConfigLocation) throws AspectranContextBuilderException {
//		List<ImportableResource> importableResourceList = null;
//
//		try {
//			importableResourceList = new ArrayList<ImportableResource>();
//		
//			String[] filePathes = StringUtils.tokenize(contextConfigLocation, "\n\t,;:| ");
//			
//			if(filePathes.length > 0) {
//				
//				for(String filePath : filePathes) {
//					File file = new File(filePath);
//				
//					if(!file.isFile())
//						throw new FileNotFoundException("aspectran context configuration file is not found. " + filePath);
//					
//					ImportableResource r = new ImportableResource();
//					r.setFile(file);
//					
//					importableResourceList.add(r);
//				}
//			}
//		} catch(Exception e) {
//			log.error(e);
//			throw new AspectranContextBuilderException(e);
//		}
//		
//		AspectranSettingAssistant assistant = null;
//		
//		for(ImportableResource r : importableResourceList) {
//			assistant = build(r, assistant);
//		}
//
//		BeanRuleMap beanRuleMap = assistant.getBeanRuleMap();
//		TransletRuleMap transletRuleMap = assistant.getTransletRuleMap();
//		
//		BeanRegistry beanRegistry = buildBeanRegistry(assistant);
//		TransletRegistry transletRegistry = buildTransletRegistry(assistant);
//		
//		// create ActivityContext
//		AspectranContext context = new AspectranContext(assistant.getActivitySettingsRule());
//		context.setBeanRegistry(beanRegistry);
//		context.setTransletRegistry(transletRegistry);
//		//context.setBeanFactory(beanRegistry);
//		//context.setTransletRuleMap(assistant.getTransletRuleMap());
//		
//		
//		
//		assistant.clearObjectStack();
//		assistant.clearTypeAliases();
//
//		return context;
//	}
	
	
}
