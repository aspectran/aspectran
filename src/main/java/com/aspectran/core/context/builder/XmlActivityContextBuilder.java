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
import com.aspectran.core.context.rule.type.ImportFileType;

/**
 * The Class XmlActivityContextBuilder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public class XmlActivityContextBuilder extends AbstractActivityContextBuilder implements ActivityContextBuilder {
	
	public XmlActivityContextBuilder(ApplicationAdapter applicationAdapter) {
		super(applicationAdapter);
	}

	public ActivityContext build(String rootContext) throws ActivityContextBuilderException {
		try {
			if(rootContext == null)
				throw new IllegalArgumentException("rootContext must not be null");
			
			ImportHandler importHandler = new XmlImportHandler(this);
			setImportHandler(importHandler);
			
			Importable importable = makeImportable(rootContext, ImportFileType.XML);
			importHandler.handle(importable);
			
			ActivityContext aspectranContext = makeActivityContext(getApplicationAdapter());
			
			return aspectranContext;
		} catch(Exception e) {
			throw new ActivityContextBuilderException("Failed to build a XML Activity Context: " + rootContext, e);
		}
	}
	
}
