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
package com.aspectran.web.activity.request.multipart;

import javax.servlet.http.HttpServletRequest;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.util.FileUtils;
import com.aspectran.web.activity.request.multipart.MultipartFormDataParser;
import com.aspectran.web.activity.request.multipart.MultipartRequestWrapper;

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

	public MultipartRequestWrapperResolver() {
	}

	public String getTemporaryFilePath() {
		return temporaryFilePath;
	}

	public void setTemporaryFilePath(String temporaryFilePath) {
		this.temporaryFilePath = temporaryFilePath;
	}

	public long getMaxRequestSize() {
		return maxRequestSize;
	}

	public void setMaxRequestSize(long maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}

	public void setMaxRequestSize(String maxRequestSize) {
		this.maxRequestSize = FileUtils.formattedSizeToBytes(maxRequestSize, -1);
	}
	
	public String getAllowedFileExtensions() {
		return allowedFileExtensions;
	}

	public void setAllowedFileExtensions(String allowedFileExtensions) {
		this.allowedFileExtensions = allowedFileExtensions;
	}

	public String getDeniedFileExtensions() {
		return deniedFileExtensions;
	}

	public void setDeniedFileExtensions(String deniedFileExtensions) {
		this.deniedFileExtensions = deniedFileExtensions;
	}

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
