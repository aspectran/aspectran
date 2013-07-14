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
package com.aspectran.base.context.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.base.context.AspectranContext;
import com.aspectran.base.context.builder.xml.AspectranNodeParser;
import com.aspectran.base.io.Resource;
import com.aspectran.base.rule.BeanRuleMap;
import com.aspectran.base.rule.RequestRule;
import com.aspectran.base.rule.ResponseRule;
import com.aspectran.base.rule.TransletRule;
import com.aspectran.base.rule.TransletRuleMap;
import com.aspectran.base.util.StringUtils;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.bean.registry.BeanRegistry;
import com.aspectran.core.translet.registry.TransletRegistry;

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
		List<Resource> resources = null;

		try {
			resources = new ArrayList<Resource>();
		
			String[] filePathes = StringUtils.tokenize(contextConfigLocation, "\n\t,;:| ");
			
			if(filePathes.length > 0) {
				
				for(String filePath : filePathes) {
					File file = new File(filePath);
				
					if(!file.isFile())
						throw new FileNotFoundException("Translets context configuration file is not found. " + filePath);
					
					Resource r = new Resource();
					r.setFile(file);
					
					resources.add(r);
				}
			}
		} catch(Exception e) {
			log.error(e);
			throw new AspectranContextBuilderException(e);
		}
		
		AspectranContextBuilderAssistant assistant = null;
		
		for(Resource r : resources) {
			assistant = build(r, assistant);
		}

		BeanRuleMap beanRuleMap = assistant.getBeanRuleMap();
		TransletRuleMap transletRuleMap = assistant.getTransletRuleMap();
		
		BeanRegistry beanRegistry = buildBeanRegistry(assistant);
		TransletRegistry transletRegistry = buildTransletRegistry(assistant);
		
		// create ActivityContext
		AspectranContext context = new AspectranContext(assistant.getActivityRule());
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
	private AspectranContextBuilderAssistant build(Resource resource, AspectranContextBuilderAssistant parentAssistant) throws AspectranContextBuilderException {
		try {
			AspectranContextBuilderAssistant assistant;
			
			if(parentAssistant == null)
				assistant = new AspectranContextBuilderAssistant(serviceRootPath);
			else
				assistant = new AspectranContextBuilderAssistant(parentAssistant);

			assistant.setBeanRuleMap(beanRuleMap);

			// Translet loading...
			AspectranNodeParser transletsNodeParser = new AspectranNodeParser(assistant);
			transletsNodeParser.parse(resource.getInputStream());
			
			List<Resource> resources = assistant.getResources();
			
			for(Resource r : resources) {
				log.info("Loading the translets from '" + r.getResource() + "'...");

				build(r, assistant);
			}
			
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
	
	private TransletRegistry buildTransletRegistry(AspectranContextBuilderAssistant sssistant) {
		return null;
	}

	private BeanRegistry buildBeanRegistry(AspectranContextBuilderAssistant sssistant) {
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
}
