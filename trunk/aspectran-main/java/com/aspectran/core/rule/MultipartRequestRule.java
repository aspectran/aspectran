/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.rule;

import com.aspectran.core.util.FileUtils;

/**
 * <p>Created: 2008. 05. 24 오후 11:05:09</p>
 */
public class MultipartRequestRule {

	private long maxRequestSize;

	private String temporaryFilePath;

	/**
	 * Gets the max request size.
	 * 
	 * @return the max request size
	 */
	public long getMaxRequestSize() {
		return maxRequestSize;
	}

	/**
	 * Sets the max request size.
	 * 
	 * @param maxRequestSize the new max request size
	 */
	public void setMaxRequestSize(long maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}
	
	/**
	 * Sets the max request size.
	 * 
	 * @param maxRequestSize the new max request size
	 */
	public void setMaxRequestSize(String maxRequestSize) {
		this.maxRequestSize = FileUtils.formatSizeToBytes(maxRequestSize, 0);
	}

	/**
	 * Gets the temporary file path.
	 * 
	 * @return the temporary file path
	 */
	public String getTemporaryFilePath() {
		return temporaryFilePath;
	}

	/**
	 * Sets the temporary file path.
	 * 
	 * @param temporaryFilePath the new temporary file path
	 */
	public void setTemporaryFilePath(String temporaryFilePath) {
		this.temporaryFilePath = temporaryFilePath;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{maxRequestSize=").append(maxRequestSize);
		sb.append(", temporaryFilePath=").append(temporaryFilePath);
		sb.append("}");
		
		return sb.toString();
	}
}
