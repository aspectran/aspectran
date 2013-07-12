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
package com.aspectran.base.rule;

import java.io.File;

import com.aspectran.base.context.ActivityContextConstant;

/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class ActivityRule {

	/** The service root path. */
	private String activityRootPath;
	
	/** The service name. */
	private String description;
	
	/** The request uri pattern. */
	private String transletPathPattern;
	
	/** The reqeust uri pattern prefix. */
	private String transletPathPatternPrefix;

	/** The reqeust uri pattern suffix. */
	private String transletPathPatternSuffix;
	
	/**
	 * Gets the service root path.
	 * 
	 * @return the service root path
	 */
	public String getActivityRootPath() {
		return activityRootPath;
	}

	/**
	 * Sets the service root path.
	 * 
	 * @param activityRootPath the new service root path
	 */
	public void setActivityRootPath(String activityRootPath) {
		this.activityRootPath = activityRootPath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the uri pattern.
	 * 
	 * @return the uri pattern
	 */
	public String getTransletPathPattern() {
		return transletPathPattern;
	}

	/**
	 * Sets the uri pattern.
	 * 
	 * @param transletPathPattern the new uri pattern
	 */
	public void setTransletPathPattern(String transletPathPattern) {
		this.transletPathPattern = transletPathPattern;
		
		if(transletPathPattern != null) {
			int index = transletPathPattern.indexOf(ActivityContextConstant.WILDCARD_CHAR);
			
			if(index != -1) {
				if(index == 0) {
					transletPathPatternSuffix = transletPathPattern.substring(ActivityContextConstant.WILDCARD_CHAR.length());
				} else if(index == (transletPathPattern.length() - 1)) {
					transletPathPatternPrefix = transletPathPattern.substring(0, transletPathPattern.length() - ActivityContextConstant.WILDCARD_CHAR.length());
				} else {
					transletPathPatternPrefix = transletPathPattern.substring(0, index);
					transletPathPatternSuffix = transletPathPattern.substring(index + 1);
				}
			}
		}
	}
	
	/**
	 * Sets the uri pattern.
	 * 
	 * @param transletPathPatternPrefix the uri pattern prefix
	 * @param transletPathPatternSuffix the uri pattern suffix
	 */
	public void setTransletPathPattern(String transletPathPatternPrefix, String transletPathPatternSuffix) {
		transletPathPattern = transletPathPatternPrefix + ActivityContextConstant.WILDCARD_CHAR + transletPathPatternSuffix;
	}
	
	/**
	 * Sets the uri prefix pattern.
	 * 
	 * @param transletPathPatternPrefix the new uri pattern prefix
	 */
	public void setTransletNamePatternPrefix(String transletPathPatternPrefix) {
		this.transletPathPatternPrefix = transletPathPatternPrefix;
		
		if(transletPathPatternSuffix != null)
			setTransletPathPattern(transletPathPatternPrefix, transletPathPatternSuffix);
	}
	
	/**
	 * Sets the uri suffix pattern.
	 * 
	 * @param transletPathPatternSuffix the new uri pattern suffix
	 */
	public void setTransletPathPatternSuffix(String transletPathPatternSuffix) {
		this.transletPathPatternSuffix = transletPathPatternSuffix;
		
		if(transletPathPatternPrefix != null)
			setTransletPathPattern(transletPathPatternPrefix, transletPathPatternSuffix);
	}
	
	/**
	 * Gets the uri pattern prefix.
	 * 
	 * @return the uri pattern prefix
	 */
	public String getTransletNamePatternPrefix() {
		return transletPathPatternPrefix;
	}

	/**
	 * Gets the uri pattern suffix.
	 * 
	 * @return the uri pattern suffix
	 */
	public String getTransletNamePatternSuffix() {
		return transletPathPatternSuffix;
	}
	
	/**
	 * To real path as file.
	 * 
	 * @param filePath the file path
	 * 
	 * @return the file
	 */
	public File toRealPathAsFile(String filePath) {
		File file;

		if(activityRootPath != null && !filePath.startsWith("/"))
			file = new File(activityRootPath, filePath);
		else
			file = new File(filePath);
		
		return file;
	}
//	
//	public String toRequestUri(String transletPath) {
//		return toRequestUri(transletPathPatternPrefix, transletPathPatternSuffix, transletPath);
//	}
//	
//	/**
//	 * To service uri.
//	 * 
//	 * @param requestUriPatternPrefix the uri pattern prefix
//	 * @param requestUriPatternSuffix the uri pattern suffix
//	 * @param transletPath the translet path
//	 * 
//	 * @return the string
//	 * @deprecated
//	 */
//	public static String toRequestUri(String requestUriPatternPrefix, String requestUriPatternSuffix, String transletPath) {
//		if(requestUriPatternPrefix == null || requestUriPatternSuffix == null || transletPath == null)
//			return transletPath;
//		
//		StringBuilder sb = new StringBuilder(128);
//		
//		if(requestUriPatternPrefix != null)
//			sb.append(requestUriPatternPrefix);
//
//		sb.append(transletPath);
//		
//		if(requestUriPatternSuffix != null)
//			sb.append(requestUriPatternSuffix);
//		
//		return sb.toString();
//	}
//
//	public String toTransletPath(String requestUri) {
//		return toTransletPath(transletPathPatternPrefix, transletPathPatternSuffix, requestUri);
//	}
//
//	public static String toTransletPath(String requestUriPatternPrefix, String requestUriPatternSuffix, String requestUri) {
//		if(requestUriPatternPrefix == null || requestUriPatternSuffix == null)
//			return requestUri;
//		
//		int beginIndex;
//		int endIndex;
//		
//		if(requestUriPatternPrefix != null && requestUri.startsWith(requestUriPatternPrefix))
//			beginIndex = requestUriPatternPrefix.length();
//		else
//			beginIndex = 0;
//		
//		if(requestUriPatternSuffix != null && requestUri.endsWith(requestUriPatternSuffix))
//			endIndex = requestUri.length() - requestUriPatternSuffix.length();
//		else
//			endIndex = requestUri.length();
//		
//		return requestUri.substring(beginIndex, endIndex);
//	}
	
	
}
