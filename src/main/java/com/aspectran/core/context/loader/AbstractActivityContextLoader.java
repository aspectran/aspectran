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
import com.aspectran.core.context.loader.resource.AspectranClassLoader;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

abstract class AbstractActivityContextLoader implements ActivityContextLoader {

	protected final Log log = LogFactory.getLog(getClass());

	private ApplicationAdapter applicationAdapter;

	private AspectranClassLoader aspectranClassLoader;

	private String[] resourceLocations;

	private String[] activeProfiles;

	private String[] defaultProfiles;

	private boolean hybridLoad;

	@Override
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}

	@Override
	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}

	@Override
	public AspectranClassLoader newAspectranClassLoader() throws InvalidResourceException {
		String[] excludePackageNames = new String[] {
				"com.aspectran.core",
				"com.aspectran.scheduler",
				"com.aspectran.web",
				"com.aspectran.console"
		};

		AspectranClassLoader acl = new AspectranClassLoader();
		acl.excludePackage(excludePackageNames);

		if(resourceLocations != null && resourceLocations.length > 0) {
			acl.setResourceLocations(resourceLocations);
		}

		this.aspectranClassLoader = acl;
		
		if(applicationAdapter != null) {
			applicationAdapter.setClassLoader(acl);
		}

		return acl;
	}

	@Override
	public String[] getResourceLocations() {
		return resourceLocations;
	}

	@Override
	public void setResourceLocations(String[] resourceLocations) {
		this.resourceLocations = resourceLocations;
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
	
}
