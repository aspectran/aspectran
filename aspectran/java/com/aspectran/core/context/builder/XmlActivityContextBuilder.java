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
import com.aspectran.core.context.builder.xml.AspectranNodeParser;

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
			Importable importStream = makeImportStream(rootContext);
			
			AspectranNodeParser parser = new AspectranNodeParser(this);
			parser.parse(importStream);
			
			ActivityContext aspectranContext = makeActivityContext(applicationAdapter);
			
			return aspectranContext;
		} catch(Exception e) {
			throw new ActivityContextBuilderException("XmlActivityContext build failed. rootContext: " + rootContext, e);
		}
	}
	
}
