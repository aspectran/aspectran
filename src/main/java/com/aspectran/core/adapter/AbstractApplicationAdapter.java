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
package com.aspectran.core.adapter;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.aspectran.core.context.bean.scope.ApplicationScope;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.service.AspectranService;
import com.aspectran.core.service.AspectranServiceController;
import com.aspectran.core.util.ResourceUtils;

/**
 * The Class AbstractApplicationAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {
	
	protected final AspectranService aspectranService;
	
	protected final Object adaptee;

	protected final ApplicationScope scope = new ApplicationScope();
	
	protected String applicationBasePath;

	/**
	 * Instantiates a new AbstractApplicationAdapter.
	 *
	 * @param aspectranService the aspectran service
	 * @param adaptee the adaptee
	 */
	public AbstractApplicationAdapter(AspectranService aspectranService, Object adaptee) {
		this.aspectranService = aspectranService;
		this.adaptee = adaptee;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#getAdaptee()
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAdaptee() {
		return (T)adaptee;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#getApplicationScope()
	 */
	public ApplicationScope getApplicationScope() {
		return scope;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#getAspectranServiceController()
	 */
	public AspectranServiceController getAspectranServiceController() {
		return aspectranService;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#getClassLoader()
	 */
	public ClassLoader getClassLoader() {
		if(aspectranService.getAspectranClassLoader() != null)
			return aspectranService.getAspectranClassLoader();
		
		return AspectranClassLoader.getDefaultClassLoader();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#getApplicationBasePath()
	 */
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

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#toRealPath(java.lang.String)
	 */
	public String toRealPath(String filePath) throws IOException {
		File file = toRealPathAsFile(filePath);
		return file.getCanonicalPath();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.ApplicationAdapter#toRealPathAsFile(java.lang.String)
	 */
	public File toRealPathAsFile(String filePath) {
		File file;
		
		if(filePath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
			URI uri = URI.create(filePath);
			file = new File(uri);
		} else if(filePath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			file = new File(getClassLoader().getResource(filePath).getFile());
		} else {
			if(applicationBasePath != null)
				file = new File(applicationBasePath, filePath);
			else
				file = new File(filePath);
		}
		
		return file;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{applicationBasePath=").append(applicationBasePath);
		sb.append(", classLoader=").append(getClassLoader());
		sb.append(", adaptee=").append(adaptee);
		sb.append("}");
		
		return sb.toString();
	}
	
}
