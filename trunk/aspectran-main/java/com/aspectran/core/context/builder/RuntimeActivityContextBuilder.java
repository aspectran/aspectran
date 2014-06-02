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

/**
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public class RuntimeActivityContextBuilder extends AbstractActivityContextBuilder implements ActivityContextBuilder {
	
	public RuntimeActivityContextBuilder() {
		this(null);
	}

	public RuntimeActivityContextBuilder(String applicationBasePath) {
		super(applicationBasePath);
	}

	public ActivityContext build(ApplicationAdapter applicationAdapter) throws ActivityContextBuilderException {
		try {
			return makeActivityContext(applicationAdapter);
		} catch(Exception e) {
			throw new ActivityContextBuilderException("ActivityContext build failed: " + e.toString(), e);
		}
	}
	
}
