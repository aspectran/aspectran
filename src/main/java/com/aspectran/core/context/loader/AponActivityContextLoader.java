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
import com.aspectran.core.adapter.BasicApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.AponActivityContextBuilder;
import com.aspectran.core.context.loader.resource.InvalidResourceException;

public class AponActivityContextLoader extends AbstractActivityContextLoader {

	private final String encoding;

	private String rootContext;

	public AponActivityContextLoader() {
		this(new BasicApplicationAdapter());
	}
	
	public AponActivityContextLoader(ApplicationAdapter applicationAdapter) {
		super(applicationAdapter);
		this.encoding = ActivityContext.DEFAULT_ENCODING;
	}
	
	public AponActivityContextLoader(ApplicationAdapter applicationAdapter, String encoding) {
		super(applicationAdapter);
		this.encoding = (encoding == null) ? ActivityContext.DEFAULT_ENCODING : encoding;
	}

	@Override
	public ActivityContext load(String rootContext) throws ActivityContextBuilderException, InvalidResourceException {
		this.rootContext = rootContext;

		log.info("Build Activity Context using the configuration root file: " + rootContext);

		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new AponActivityContextBuilder(getApplicationAdapter(), encoding);
		builder.setActiveProfiles(getActiveProfiles());
		builder.setDefaultProfiles(getDefaultProfiles());
		
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;

		log.info("Activity Context build completed in " + elapsedTime +" ms.");
		
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
