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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegister;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.ScopedBeanRegistry;
import com.aspectran.core.context.builder.xml.AspectranNodeParser;
import com.aspectran.core.context.translet.TransletRegistry;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.BeanRuleMap;
import com.aspectran.core.rule.TransletRuleMap;

/**
 * TransletsContext builder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public class AspectranContextBuilder {
	
	private final Log log = LogFactory.getLog(AspectranContextBuilder.class);

	private String applicationRootPath;
	
	/**
	 * Instantiates a new translets client loader.
	 * 
	 * @param servicePath the service path
	 */
	public AspectranContextBuilder(String applicationRootPath) {
		this.applicationRootPath = applicationRootPath;
	}

	public AspectranContext build(String contextConfigLocation) throws AspectranContextBuilderException {
		AspectranContextResource resource = null;
		
		try {
			File file = new File(applicationRootPath, contextConfigLocation);
			
			log.debug("absolute pathname: " + file.getAbsolutePath());

			if(!file.isFile()) {
				throw new FileNotFoundException("aspectran context configuration file is not found. " + contextConfigLocation);
			}
			
			resource = new AspectranContextResource();
			resource.setFile(file);
		} catch(Exception e) {
			log.error(e);
			throw new AspectranContextBuilderException(e);
		}
		
		try {
			AspectranContextBuildingAssistant assistant = new AspectranContextBuildingAssistant(applicationRootPath);
	
			AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(assistant);
			aspectranNodeParser.parse(resource.getInputStream());
			
			AspectranContext aspectranContext = makeAspectranContext(assistant);
			
			assistant.clearObjectStack();
			assistant.clearTypeAliases();
	
			return aspectranContext;
		} catch(Exception e) {
			log.error("aspectran configuration error: " + resource);
			throw new AspectranContextBuilderException("aspectran configuration error: " + resource, e);
		}
	}
	
	private AspectranContext makeAspectranContext(AspectranContextBuildingAssistant assistant) {
		BeanRegistry beanRegistry = makeBeanRegistry(assistant);
		TransletRegistry transletRuleRegistry = makeTransletRuleRegistry(assistant);
		
		AspectranContext aspectranContext = new AspectranContext();
		aspectranContext.setBeanRegistry(beanRegistry);
		aspectranContext.setTransletRuleRegistry(transletRuleRegistry);
		
		return aspectranContext;
	}
	
	private TransletRegistry makeTransletRuleRegistry(AspectranContextBuildingAssistant assistant) {
		AspectRuleMap aspectRuleMap = assistant.getAspectRuleMap();
		TransletRuleMap transletRuleMap = assistant.getTransletRuleMap();
		//transletRuleMap.freeze();
		
		AspectAdviceRuleRegister aspectAdviceRuleRegister = new AspectAdviceRuleRegister(aspectRuleMap);
		aspectAdviceRuleRegister.register(transletRuleMap);
		
		return new TransletRegistry(transletRuleMap);
	}

	private BeanRegistry makeBeanRegistry(AspectranContextBuildingAssistant assistant) {
		BeanRuleMap beanRuleMap = assistant.getBeanRuleMap();
		//beanRuleMap.freeze();
		return new ScopedBeanRegistry(beanRuleMap);
	}
	
}
