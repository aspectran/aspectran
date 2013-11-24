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
import java.io.IOException;
import java.io.InputStream;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.builder.xml.parser.AspectranNodeParser;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.ResourceUtils;

/**
 * XmlAspectranContextBuilder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public class XmlAspectranContextBuilder extends AbstractAspectranContextBuilder implements AspectranContextBuilder {
	
	private String contextConfigLocation;
	
	public XmlAspectranContextBuilder(String contextConfigLocation) {
		this(null, contextConfigLocation);
	}

	public XmlAspectranContextBuilder(String applicationBasePath, String contextConfigLocation) {
		super(applicationBasePath);
		
		Assert.notNull(contextConfigLocation, "contextConfigLocation must not be null");
		
		this.contextConfigLocation = contextConfigLocation;
	}

	public AspectranContext build() throws AspectranContextBuilderException {
		InputStream inputStream = null;

		try {
			if(contextConfigLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				String resource = contextConfigLocation.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
				
				inputStream = getClassLoader().getResourceAsStream(resource);
				
				if(inputStream == null)
					throw new IOException("Cannot read aspectran context configuration resource: " + resource);
			} else {
				File file = new File(getApplicationBasePath(), contextConfigLocation);
				
				if(!file.isFile()) {
					throw new FileNotFoundException("aspectran context configuration file is not found: " + file.getAbsolutePath());
				}
				
				inputStream = new FileInputStream(file);
			}
			
			return build(inputStream);
			
		} catch(Exception e) {
			throw new AspectranContextBuilderException("aspectran context build failed: " + e.toString(), e);
		} finally {
			try {
				inputStream.close();
			} catch(IOException e) {
			}
		}
	}
	
	private AspectranContext build(InputStream inputStream) throws Exception {
		AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(this);
		aspectranNodeParser.parse(inputStream);
		
		AspectranContext aspectranContext = makeAspectranContext(this);
		
		return aspectranContext;
	}
	
}
