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

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class HybridActivityContextLoader extends AbstractActivityContextLoader {

	private final Log log = LogFactory.getLog(HybridActivityContextLoader.class);
	
	private static final String DEFAULT_ENCODING = "utf-8";
	
	private String encoding;

	public HybridActivityContextLoader() {
		this(null);
	}
	
	public HybridActivityContextLoader(String encoding) {
		this.encoding = (encoding == null) ? DEFAULT_ENCODING : encoding;
	}
	
	public ActivityContext load(String rootContext) throws ActivityContextBuilderException {
		log.info("Build ActivityContext: " + rootContext);
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new HybridActivityContextBuilder(applicationAdapter, encoding);
		builder.setHybridLoading(isHybridLoading());
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		log.info("ActivityContext build completed in " + elapsedTime + " ms.");
		
		return activityContext;
	}
	
}
