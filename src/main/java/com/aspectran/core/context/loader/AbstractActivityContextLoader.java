/**
 * Copyright 2008-2017 Juho Jeong
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
import com.aspectran.core.context.loader.resource.AspectranClassLoader;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

abstract class AbstractActivityContextLoader implements ActivityContextLoader {

	protected final Log log = LogFactory.getLog(getClass());
	
	private final ApplicationAdapter applicationAdapter;

	private AspectranClassLoader aspectranClassLoader;

	private String[] activeProfiles;

	private String[] defaultProfiles;

	private boolean hybridLoad;

	AbstractActivityContextLoader(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
		
		newAspectranClassLoader();
	}
	
	@Override
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	@Override
	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}

	@Override
	public void setResourceLocations(String[] resourceLocations) throws InvalidResourceException {
		if (resourceLocations != null) {
			aspectranClassLoader.setResourceLocations(resourceLocations);
		}
	}

	@Override
	public String[] getActiveProfiles() {
		return activeProfiles;
	}

	@Override
	public void setActiveProfiles(String... activeProfiles) {
		this.activeProfiles = activeProfiles;
	}

	@Override
	public String[] getDefaultProfiles() {
		return defaultProfiles;
	}

	@Override
	public void setDefaultProfiles(String... defaultProfiles) {
		this.defaultProfiles = defaultProfiles;
	}

	@Override
	public boolean isHybridLoad() {
		return hybridLoad;
	}

	@Override
	public void setHybridLoad(boolean hybridLoad) {
		this.hybridLoad = hybridLoad;
	}

	protected AspectranClassLoader newAspectranClassLoader() {
		String[] excludePackageNames = new String[] {
				"com.aspectran.core",
				"com.aspectran.scheduler",
				"com.aspectran.web",
				"com.aspectran.console",
				"com.aspectran.embedded"
		};

		AspectranClassLoader acl = new AspectranClassLoader();
		acl.excludePackage(excludePackageNames);

		this.aspectranClassLoader = acl;
		this.applicationAdapter.setClassLoader(acl);
		
		return acl;
	}
	
}
