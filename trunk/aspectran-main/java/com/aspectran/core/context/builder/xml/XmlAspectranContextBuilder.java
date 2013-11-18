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

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegister;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.ScopedBeanRegistry;
import com.aspectran.core.context.builder.AspectranContextBuilder;
import com.aspectran.core.context.builder.AspectranContextBuilderException;
import com.aspectran.core.context.builder.AspectranContextResource;
import com.aspectran.core.context.builder.BeanReferenceInspector;
import com.aspectran.core.context.builder.xml.parser.AspectranNodeParser;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.BeanRuleMap;
import com.aspectran.core.rule.TransletRuleMap;
import com.aspectran.core.type.AspectranSettingType;

/**
 * TransletsContext builder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public class XmlAspectranContextBuilder implements AspectranContextBuilder {
	
	private final Log log = LogFactory.getLog(XmlAspectranContextBuilder.class);

	private String applicationRootPath;
	
	/**
	 * Instantiates a new translets client loader.
	 * 
	 * @param servicePath the service path
	 */
	public XmlAspectranContextBuilder(String applicationRootPath) {
		this.applicationRootPath = applicationRootPath;
	}

	public AspectranContext build(String contextConfigLocation) throws AspectranContextBuilderException {
		File file = new File(applicationRootPath, contextConfigLocation);
		return build(file);
	}

	public AspectranContext build(File contextConfigFile) throws AspectranContextBuilderException {
		AspectranContextResource resource = null;
		
		try {
			if(!contextConfigFile.isFile()) {
				throw new FileNotFoundException("aspectran context configuration file is not found. " + contextConfigFile.getName());
			}
			
			resource = new AspectranContextResource();
			resource.setFile(contextConfigFile);
		} catch(Exception e) {
			log.error(e);
			throw new AspectranContextBuilderException(e);
		}
		
		return build(resource);
	}
	
	public AspectranContext build(AspectranContextResource resource) throws AspectranContextBuilderException {
		try {
			XmlAspectranContextAssistant assistant = new XmlAspectranContextAssistant(applicationRootPath);
			
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
	
	private AspectranContext makeAspectranContext(XmlAspectranContextAssistant assistant) {
		BeanRegistry beanRegistry = makeBeanRegistry(assistant);
		TransletRuleRegistry transletRuleRegistry = makeTransletRegistry(assistant);
		
		BeanReferenceInspector beanReferenceInspector = assistant.getBeanReferenceInspector();
		beanReferenceInspector.inpect(assistant.getBeanRuleMap());
		
		AspectranContext aspectranContext = new AspectranContext();
		aspectranContext.setAspectRuleMap(assistant.getAspectRuleMap());
		aspectranContext.setBeanRegistry(beanRegistry);
		aspectranContext.setTransletRuleRegistry(transletRuleRegistry);
		aspectranContext.setActivityDefaultHandler((String)assistant.getSetting(AspectranSettingType.ACTIVITY_DEFAULT_HANDLER));
		
		return aspectranContext;
	}
	
	private TransletRuleRegistry makeTransletRegistry(XmlAspectranContextAssistant assistant) {
		AspectRuleMap aspectRuleMap = assistant.getAspectRuleMap();
		TransletRuleMap transletRuleMap = assistant.getTransletRuleMap();
		transletRuleMap.freeze();
		
		AspectAdviceRuleRegister aspectAdviceRuleRegister = new AspectAdviceRuleRegister(aspectRuleMap);
		aspectAdviceRuleRegister.register(transletRuleMap);
		
		return new TransletRuleRegistry(transletRuleMap);
	}

	private BeanRegistry makeBeanRegistry(XmlAspectranContextAssistant assistant) {
		BeanRuleMap beanRuleMap = assistant.getBeanRuleMap();
		beanRuleMap.freeze();
		
		return new ScopedBeanRegistry(beanRuleMap);
	}
	
}
