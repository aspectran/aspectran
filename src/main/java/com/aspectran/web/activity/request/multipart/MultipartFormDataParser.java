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

import com.aspectran.core.adapter.RequestAdapter;

/**
 * Multi-part form data parser.
 */
public interface MultipartFormDataParser {

	/**
	 * Returns the directory path used for temporary files.
	 *
	 * @return the directory path used for temporary files
	 */
	String getTempDirectoryPath();

	/**
	 * Sets the directory path used to temporarily files.
	 *
	 * @param tempDirectoryPath the directory path used for temporary files
	 */
	void setTempDirectoryPath(String tempDirectoryPath);

	/**
	 * Returns the maximum length of HTTP GET Request
	 *
	 * @return the max request size
	 */
	long getMaxRequestSize();

	/**
	 * Sets the maximum length of HTTP GET Request
	 *
	 * @param maxSize the maximum length of HTTP GET Request
	 */
	void setMaxRequestSize(long maxSize);

	/**
	 * Gets the allowed file extensions.
	 *
	 * @return the allowed file extensions
	 */
	String getAllowedFileExtensions();

	/**
	 * Sets the allowed file extensions.
	 *
	 * @param allowedFileExtensions the allowed file extensions
	 */
	void setAllowedFileExtensions(String allowedFileExtensions);

	/**
	 * Gets the denied file extensions.
	 *
	 * @return the denied file extensions
	 */
	String getDeniedFileExtensions();

	/**
	 * Sets the denied file extensions.
	 *
	 * @param deniedFileExtensions the denied file extensions
	 */
	void setDeniedFileExtensions(String deniedFileExtensions);

	/**
	 * Parse the given servlet request, resolving its multipart elements.
	 *
	 * @param requestAdapter the request adapter
	 * @throws MultipartRequestException if multipart resolution failed
	 */
	void parse(RequestAdapter requestAdapter);

}
