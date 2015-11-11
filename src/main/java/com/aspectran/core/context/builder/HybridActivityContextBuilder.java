/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.builder;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;

/**
 * The Class HybridActivityContextBuilder.
 * 
 * <p>Created: 2015. 01. 27 오후 10:36:29</p>
 */
public class HybridActivityContextBuilder extends AbstractActivityContextBuilder implements ActivityContextBuilder {
	
	private final String encoding;
	
	public HybridActivityContextBuilder(ApplicationAdapter applicationAdapter) {
		this(applicationAdapter, null);
	}
	
	public HybridActivityContextBuilder(ApplicationAdapter applicationAdapter, String encoding) {
		setApplicationAdapter(applicationAdapter);
		this.encoding = encoding;
	}

	public ActivityContext build(String rootContext) throws ActivityContextBuilderException {
		try {
			if(rootContext == null)
				throw new IllegalArgumentException("rootContext must not be null");

			ImportHandler importHandler = new HybridImportHandler(this, encoding, isHybridLoading());
			setImportHandler(importHandler);
			
			Importable importable = makeImportable(rootContext);
			importHandler.handle(importable);
			
			ActivityContext aspectranContext = makeActivityContext(getApplicationAdapter());

			return aspectranContext;
		} catch(Exception e) {
			throw new ActivityContextBuilderException("HybridActivityContext build failed. rootContext: " + rootContext, e);
		}
	}

}
