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
package com.aspectran.core.adapter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import com.aspectran.core.context.bean.scope.ApplicationScope;
import com.aspectran.core.context.loader.resource.AspectranClassLoader;
import com.aspectran.core.service.AspectranServiceController;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class AbstractApplicationAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {
	
	protected final Object adaptee;

	protected final ApplicationScope scope = new ApplicationScope();

	protected ClassLoader classLoader;

	protected AspectranServiceController aspectranServiceController;

	protected String applicationBasePath;

	/**
	 * Instantiates a new AbstractApplicationAdapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractApplicationAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdaptee() {
		return (T)adaptee;
	}

	@Override
	public ApplicationScope getApplicationScope() {
		return scope;
	}

	@Override
	public AspectranServiceController getAspectranServiceController() {
		return aspectranServiceController;
	}

	@Override
	public void setAspectranServiceController(AspectranServiceController aspectranServiceController) {
		this.aspectranServiceController = aspectranServiceController;
	}

	@Override
	public ClassLoader getClassLoader() {
		if(classLoader == null) {
			return AspectranClassLoader.getDefaultClassLoader();
		}

		return classLoader;
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public String getApplicationBasePath() {
		return applicationBasePath;
	}

	/**
	 * Sets the application base path.
	 *
	 * @param applicationBasePath the new application base path
	 */
	public void setApplicationBasePath(String applicationBasePath) {
		this.applicationBasePath = applicationBasePath;
	}

	@Override
	public String toRealPath(String filePath) throws IOException {
		File file = toRealPathAsFile(filePath);
		return file.getCanonicalPath();
	}

	@Override
	public File toRealPathAsFile(String filePath) throws IOException {
		File file;
		
		if(filePath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
			URI uri = URI.create(filePath);
			file = new File(uri);
		} else if(filePath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			URL url = getClassLoader().getResource(filePath);
			if(url == null)
				throw new IOException("Could not find the resource with the given name: " + filePath);
			file = new File(url.getFile());
		} else {
			if(applicationBasePath != null)
				file = new File(applicationBasePath, filePath);
			else
				file = new File(filePath);
		}
		
		return file;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("applicationBasePath", applicationBasePath);
		tsb.append("classLoader", getClassLoader());
		tsb.append("adaptee", adaptee);
		return tsb.toString();
	}
	
}
