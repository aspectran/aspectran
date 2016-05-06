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
package com.aspectran.core.context.loader;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.GenericApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;
import com.aspectran.core.context.loader.resource.InvalidResourceException;

public class XmlActivityContextLoader extends AbstractActivityContextLoader {

	private String rootContext;

	public XmlActivityContextLoader() {
		super(new GenericApplicationAdapter());
	}
	
	public XmlActivityContextLoader(ApplicationAdapter applicationAdapter) {
		super(applicationAdapter);
	}

	@Override
	public ActivityContext load(String rootContext) throws ActivityContextBuilderException, InvalidResourceException {
		this.rootContext = rootContext;

		log.info("Build ActivityContext: " + rootContext);

		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new XmlActivityContextBuilder(getApplicationAdapter());
		builder.setActiveProfiles(getActiveProfiles());
		builder.setDefaultProfiles(getDefaultProfiles());
		builder.setHybridLoad(isHybridLoad());
		
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;

		log.info("ActivityContext build completed in " + elapsedTime + " ms.");
		
		return activityContext;
	}

	@Override
	public ActivityContext reload(boolean hardReload) throws ActivityContextBuilderException, InvalidResourceException {
		if(hardReload) {
			newAspectranClassLoader();
		}

		return load(rootContext);
	}

}
