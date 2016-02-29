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
package com.aspectran.core.context.builder;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.importer.AponImportHandler;
import com.aspectran.core.context.builder.importer.ImportHandler;
import com.aspectran.core.context.builder.importer.Importer;
import com.aspectran.core.context.rule.type.ImportFileType;

/**
 * The Class AponAspectranContextBuilder.
 * 
 * <p>Created: 2015. 01. 27 PM 10:36:29</p>
 */
public class AponActivityContextBuilder extends AbstractActivityContextBuilder implements ActivityContextBuilder {
	
	private final String encoding;
	
	public AponActivityContextBuilder(ApplicationAdapter applicationAdapter) {
		this(applicationAdapter, null);
	}
	
	public AponActivityContextBuilder(ApplicationAdapter applicationAdapter, String encoding) {
		super(applicationAdapter);
		this.encoding = encoding;
	}

	@Override
	public ActivityContext build(String rootContext) throws ActivityContextBuilderException {
		try {
			if(rootContext == null)
				throw new IllegalArgumentException("'rootContext' must not be null.");

			ImportHandler importHandler = new AponImportHandler(this, encoding);
			setImportHandler(importHandler);
			
			Importer importer = resolveImporter(rootContext, ImportFileType.APON);
			importHandler.handle(importer);

			return makeActivityContext(getApplicationAdapter());
		} catch(Exception e) {
			throw new ActivityContextBuilderException("Failed to build a APON Activity Context: " + rootContext, e);
		}
	}
	
}
