/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.console.adapter;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.adapter.AbstractApplicationAdapter;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.service.AspectranService;

/**
 * The Class ConsoleApplicationAdapter.
 * 
 * @author Juho Jeong
 * @since 2016. 1. 18.
 */
public class ConsoleApplicationAdapter extends AbstractApplicationAdapter implements ApplicationAdapter {
	
	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	/**
	 * Instantiates a new ConsoleApplicationAdapter.
	 *
	 * @param aspectranService the aspectran service
	 */
	public ConsoleApplicationAdapter(AspectranService aspectranService) {
		super(aspectranService, null);
		
		String applicationBasePath = System.getProperty("com.aspectran.console.workingDir");
		if(applicationBasePath == null)
			applicationBasePath = new File(".").getAbsolutePath();
		
		super.setApplicationBasePath(applicationBasePath);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#getAttribute(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)attributes.get(name);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
}
