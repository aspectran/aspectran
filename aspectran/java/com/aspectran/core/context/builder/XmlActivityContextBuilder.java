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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.xml.parser.AspectranNodeParser;
import com.aspectran.core.util.ResourceUtils;

/**
 * XmlAspectranContextBuilder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public class XmlActivityContextBuilder extends AbstractActivityContextBuilder implements ActivityContextBuilder {
	
	private final ApplicationAdapter applicationAdapter;
	
	public XmlActivityContextBuilder(ApplicationAdapter applicationAdapter) {
		this(applicationAdapter, null);
	}
	
	public XmlActivityContextBuilder(ApplicationAdapter applicationAdapter, ClassLoader classLoader) {
		super(applicationAdapter.getApplicationBasePath(), classLoader);
		this.applicationAdapter = applicationAdapter;
	}

	public ActivityContext build(String rootContext) throws ActivityContextBuilderException {
		try {
			if(rootContext == null)
				throw new IllegalArgumentException("rootContext must not be null");
			
			ImportResource importResource = new ImportResource(getClassLoader());

			if(rootContext.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				String resource = rootContext.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
				importResource.setResource(resource);
			} else if(rootContext.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
					String file = rootContext.substring(ResourceUtils.FILE_URL_PREFIX.length());
					importResource.setFile(file);
			} else {
				importResource.setFile(getApplicationBasePath(), rootContext);
			}
			
			return build(importResource);
		} catch(Exception e) {
			throw new ActivityContextBuilderException("XmlActivityContext build failed. rootContext: " + rootContext, e);
		}
	}
	
	private ActivityContext build(ImportResource importResource) throws Exception {
		AspectranNodeParser parser = new AspectranNodeParser(this);
		parser.parse(importResource);
		
		ActivityContext aspectranContext = makeActivityContext(applicationAdapter);
		
		return aspectranContext;
	}
	
}
