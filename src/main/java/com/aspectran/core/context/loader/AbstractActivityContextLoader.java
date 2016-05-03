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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.GenericApplicationAdapter;
import com.aspectran.core.context.builder.env.Environment;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

abstract class AbstractActivityContextLoader implements ActivityContextLoader {

	protected final Log log = LogFactory.getLog(getClass());

	private final ApplicationAdapter applicationAdapter;

	private AspectranClassLoader aspectranClassLoader;

	private String[] resourceLocations;

	private boolean hybridLoad;

	private Environment environment;

	AbstractActivityContextLoader() {
		this.applicationAdapter = new GenericApplicationAdapter();
	}

	AbstractActivityContextLoader(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
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
	public AspectranClassLoader newAspectranClassLoader(String[] resourceLocations) throws InvalidResourceException {
		setResourceLocations(resourceLocations);
		return newAspectranClassLoader();
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

		aspectranClassLoader = acl;
		applicationAdapter.setClassLoader(acl);

		return acl;
	}

	@Override
	public String[] getResourceLocations() {
		return resourceLocations;
	}

	@Override
	public String[] setResourceLocations(String[] resourceLocations) throws InvalidResourceException {
		this.resourceLocations = checkResourceLocations(resourceLocations);
		return this.resourceLocations;
	}

	private String[] checkResourceLocations(String[] resourceLocations) throws InvalidResourceException {
		if(resourceLocations == null)
			return null;

		for(int i = 0; i < resourceLocations.length; i++) {
			if(resourceLocations[i].startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				String path = resourceLocations[i].substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
				URL url = AspectranClassLoader.getDefaultClassLoader().getResource(path);
				if(url == null)
					throw new InvalidResourceException("Class path resource [" + resourceLocations[i] + "] cannot be resolved to URL because it does not exist.");
				resourceLocations[i] = url.getFile();
			} else if(resourceLocations[i].startsWith(ResourceUtils.FILE_URL_PREFIX)) {
				try {
					URL url = new URL(resourceLocations[i]);
					resourceLocations[i] = url.getFile();
				} catch (MalformedURLException e) {
					throw new InvalidResourceException("Resource location [" + resourceLocations[i] + "] is neither a URL not a well-formed file path.");
				}
			} else {
				if(applicationAdapter.getApplicationBasePath() != null) {
					resourceLocations[i] = applicationAdapter.getApplicationBasePath() + resourceLocations[i];
				}
			}

			if(resourceLocations[i].indexOf('\\') != -1)
				resourceLocations[i] = resourceLocations[i].replace('\\', '/');

			if(StringUtils.endsWith(resourceLocations[i], ResourceUtils.PATH_SPEPARATOR_CHAR))
				resourceLocations[i] = resourceLocations[i].substring(0, resourceLocations[i].length() - 1);
		}

		String resourceLocation = null;

		try {
			for(int i = 0; i < resourceLocations.length - 1; i++) {
				if(resourceLocations[i] != null) {
					resourceLocation = resourceLocations[i];
					File f1 = new File(resourceLocations[i]);
					String l1 = f1.getCanonicalPath();

					for(int j = i + 1; j < resourceLocations.length; j++) {
						if(resourceLocations[j] != null) {
							resourceLocation = resourceLocations[j];
							File f2 = new File(resourceLocations[j]);
							String l2 = f2.getCanonicalPath();

							if(l1.equals(l2)) {
								resourceLocations[j] = null;
							}
						}
					}
				}
			}
		} catch(IOException e) {
			throw new InvalidResourceException("Invalid resource location: " + resourceLocation, e);
		}

		return resourceLocations;
	}

	@Override
	public boolean isHybridLoad() {
		return hybridLoad;
	}

	@Override
	public void setHybridLoad(boolean hybridLoad) {
		this.hybridLoad = hybridLoad;
	}
	
	@Override
	public Environment getEnvironment() {
		return environment;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

}
