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
package com.aspectran.web.activity.request.multipart;

import javax.servlet.http.HttpServletRequest;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.util.FileUtils;

/**
 * The Class MultipartRequestWrapperResolver.
 *
 * @since 1.3.1
 */
public class MultipartRequestWrapperResolver {
	
	private String temporaryFilePath;
	
	private long maxRequestSize = -1L;

	private String allowedFileExtensions;

	private String deniedFileExtensions;

	/**
	 * Instantiates a new Multipart request wrapper resolver.
	 */
	public MultipartRequestWrapperResolver() {
	}

	/**
	 * Gets temporary file path.
	 *
	 * @return the temporary file path
	 */
	public String getTemporaryFilePath() {
		return temporaryFilePath;
	}

	/**
	 * Sets temporary file path.
	 *
	 * @param temporaryFilePath the temporary file path
	 */
	public void setTemporaryFilePath(String temporaryFilePath) {
		this.temporaryFilePath = temporaryFilePath;
	}

	/**
	 * Gets max request size.
	 *
	 * @return the max request size
	 */
	public long getMaxRequestSize() {
		return maxRequestSize;
	}

	/**
	 * Sets max request size.
	 *
	 * @param maxRequestSize the max request size
	 */
	public void setMaxRequestSize(long maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}

	/**
	 * Sets max request size.
	 *
	 * @param maxRequestSize the max request size
	 */
	public void setMaxRequestSize(String maxRequestSize) {
		this.maxRequestSize = FileUtils.formattedSizeToBytes(maxRequestSize, -1);
	}

	/**
	 * Gets allowed file extensions.
	 *
	 * @return the allowed file extensions
	 */
	public String getAllowedFileExtensions() {
		return allowedFileExtensions;
	}

	/**
	 * Sets allowed file extensions.
	 *
	 * @param allowedFileExtensions the allowed file extensions
	 */
	public void setAllowedFileExtensions(String allowedFileExtensions) {
		this.allowedFileExtensions = allowedFileExtensions;
	}

	/**
	 * Gets denied file extensions.
	 *
	 * @return the denied file extensions
	 */
	public String getDeniedFileExtensions() {
		return deniedFileExtensions;
	}

	/**
	 * Sets denied file extensions.
	 *
	 * @param deniedFileExtensions the denied file extensions
	 */
	public void setDeniedFileExtensions(String deniedFileExtensions) {
		this.deniedFileExtensions = deniedFileExtensions;
	}

	/**
	 * Gets multipart request wrapper.
	 *
	 * @param translet the translet
	 * @return the multipart request wrapper
	 */
	public MultipartRequestWrapper getMultipartRequestWrapper(Translet translet) {
		HttpServletRequest request = translet.getRequestAdaptee();
		
		MultipartFormDataParser parser = new MultipartFormDataParser(request);
		if(maxRequestSize > -1)
			parser.setMaxRequestSize(maxRequestSize);
		parser.setTemporaryFilePath(temporaryFilePath);
		parser.setAllowedFileExtensions(allowedFileExtensions);
		parser.setDeniedFileExtensions(deniedFileExtensions);
		parser.parse();
		
		MultipartRequestWrapper requestWrapper = new MultipartRequestWrapper(parser);
		
		return requestWrapper;
	}
	
}
