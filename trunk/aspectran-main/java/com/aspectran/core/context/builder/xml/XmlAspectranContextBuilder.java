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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.builder.AbstractAspectranContextBuilder;
import com.aspectran.core.context.builder.AspectranContextBuilder;
import com.aspectran.core.context.builder.AspectranContextBuilderException;
import com.aspectran.core.context.builder.xml.parser.AspectranNodeParser;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.type.DefaultSettingType;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.ResourceUtils;

/**
 * XmlAspectranContextBuilder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public class XmlAspectranContextBuilder extends AbstractAspectranContextBuilder implements AspectranContextBuilder {
	
	private final Logger logger = LoggerFactory.getLogger(XmlAspectranContextBuilder.class);

	private String applicationBasePath;
	
	public XmlAspectranContextBuilder() {
		this(new File(".").getAbsoluteFile().toString());
	}

	public XmlAspectranContextBuilder(String applicationBasePath) {
		this.applicationBasePath = applicationBasePath;
		logger.info("application base directory path [" + applicationBasePath + "]");
	}

	public AspectranContext build(String contextConfigLocation) throws AspectranContextBuilderException {
		Assert.notNull(contextConfigLocation, "contextConfigLocation must not be null");
		
		try {
			InputStream inputStream;
			
			if(contextConfigLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				String path = contextConfigLocation.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
				
				try {
					inputStream = ResourceUtils.getResourceAsStream(path);
				} catch(IOException e2) {
					throw new IOException("Cannot read aspectran context configuration resource: " + path);
				}
			} else {
				File file = new File(applicationBasePath, contextConfigLocation);
				
				if(!file.isFile()) {
					throw new FileNotFoundException("aspectran context configuration file is not found: " + file.getAbsolutePath());
				}
				
				inputStream = new FileInputStream(file);
			}
			
			return build(inputStream);
			
		} catch(Exception e) {
			throw new AspectranContextBuilderException("aspectran context configuration error: " + e.getMessage(), e);
		}
	}
	
	private AspectranContext build(InputStream inputStream) throws Exception {
		XmlBuilderAssistant assistant = new XmlBuilderAssistant(applicationBasePath);
		
		AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(assistant);
		aspectranNodeParser.parse(inputStream);
		
		AspectranContext aspectranContext = makeAspectranContext(assistant);
		
		return aspectranContext;
	}
	
	private AspectranContext makeAspectranContext(XmlBuilderAssistant assistant) {
		BeanRegistry beanRegistry = makeBeanRegistry(assistant.getBeanRuleMap());
		TransletRuleRegistry transletRuleRegistry = makeTransletRegistry(assistant.getAspectRuleMap(), assistant.getTransletRuleMap());
		
		BeanReferenceInspector beanReferenceInspector = assistant.getBeanReferenceInspector();
		beanReferenceInspector.inpect(assistant.getBeanRuleMap());
		
		AspectranContext aspectranContext = new AspectranContext();
		aspectranContext.setAspectRuleMap(assistant.getAspectRuleMap());
		aspectranContext.setBeanRegistry(beanRegistry);
		aspectranContext.setTransletRuleRegistry(transletRuleRegistry);
		aspectranContext.setActivityDefaultHandler((String)assistant.getSetting(DefaultSettingType.ACTIVITY_DEFAULT_HANDLER));
		
		return aspectranContext;
	}
	
}
