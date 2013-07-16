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

import java.io.File;
import java.util.Map;

import com.aspectran.core.context.builder.AspectranContextConstant;
import com.aspectran.core.type.ActivitySettingType;

/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class AspectranSettingsRule {

	private String activityRootPath;

	private String transletNameSeparator;
	
	private String transletNamePattern;
	
	private String transletNamePatternPrefix;

	private String transletNamePatternSuffix;
	
	private String transletInterface;

	private String transletClass;
	
	private boolean useNamespaces = true;

	private boolean nullableContentId = true;
	
	private boolean nullableActionId = true;
	
	private boolean multiActivityEnable = true;
	
	public String getActivityRootPath() {
		return activityRootPath;
	}

	public String getTransletNameSeparator() {
		return transletNameSeparator;
	}

	public void setTransletNameSeparator(String transletNameSeparator) {
		this.transletNameSeparator = transletNameSeparator;
	}

	public void setActivityRootPath(String activityRootPath) {
		this.activityRootPath = activityRootPath;
	}

	public String getTransletNamePattern() {
		return transletNamePattern;
	}

	public void setTransletNamePattern(String transletNamePattern) {
		this.transletNamePattern = transletNamePattern;
		
		if(transletNamePattern != null) {
			int index = transletNamePattern.indexOf(AspectranContextConstant.WILDCARD_CHAR);
			
			if(index != -1) {
				if(index == 0) {
					transletNamePatternPrefix = null;
					transletNamePatternSuffix = transletNamePattern.substring(AspectranContextConstant.WILDCARD_CHAR.length());
				} else if(index == (transletNamePattern.length() - 1)) {
					transletNamePatternPrefix = transletNamePattern.substring(0, transletNamePattern.length() - AspectranContextConstant.WILDCARD_CHAR.length());
					transletNamePatternSuffix = null;
				} else {
					transletNamePatternPrefix = transletNamePattern.substring(0, index);
					transletNamePatternSuffix = transletNamePattern.substring(index + 1);
				}
			}
		}
	}
	
	public void setTransletNamePattern(String transletNamePatternPrefix, String transletNamePatternSuffix) {
		transletNamePattern = transletNamePatternPrefix + AspectranContextConstant.WILDCARD_CHAR + transletNamePatternSuffix;
	}
	
	public void setTransletNamePatternPrefix(String transletNamePatternPrefix) {
		this.transletNamePatternPrefix = transletNamePatternPrefix;
		
		if(transletNamePatternSuffix != null)
			setTransletNamePattern(transletNamePatternPrefix, transletNamePatternSuffix);
	}
	
	public void setTransletNamePatternSuffix(String transletNamePatternSuffix) {
		this.transletNamePatternSuffix = transletNamePatternSuffix;
		
		if(transletNamePatternPrefix != null)
			setTransletNamePattern(transletNamePatternPrefix, transletNamePatternSuffix);
	}
	
	public String getTransletNamePatternPrefix() {
		return transletNamePatternPrefix;
	}

	public String getTransletNamePatternSuffix() {
		return transletNamePatternSuffix;
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
	
	public String getTransletInterface() {
		return transletInterface;
	}

	public void setTransletInterface(String transletInterface) {
		this.transletInterface = transletInterface;
	}

	public String getTransletClass() {
		return transletClass;
	}

	public void setTransletClass(String transletClass) {
		this.transletClass = transletClass;
	}

	public boolean isUseNamespaces() {
		return useNamespaces;
	}

	public void setUseNamespaces(boolean useNamespaces) {
		this.useNamespaces = useNamespaces;
	}

	public boolean isNullableContentId() {
		return nullableContentId;
	}

	public void setNullableContentId(boolean nullableContentId) {
		this.nullableContentId = nullableContentId;
	}

	public boolean isNullableActionId() {
		return nullableActionId;
	}

	public void setNullableActionId(boolean nullableActionId) {
		this.nullableActionId = nullableActionId;
	}

	public boolean isMultiActivityEnable() {
		return multiActivityEnable;
	}

	public void setMultiActivityEnable(boolean multiActivityEnable) {
		this.multiActivityEnable = multiActivityEnable;
	}

	public void set(Map<ActivitySettingType, String> settings) {
		if(settings.get(ActivitySettingType.USE_NAMESPACES) != null)
			useNamespaces = Boolean.valueOf(settings.get(ActivitySettingType.USE_NAMESPACES));

//TODO
		//		nullableContentId = isSettedTrue(ActivitySettingType.NULLABLE_CONTENT_ID);
//		nullableActionId = isSettedTrue(ActivitySettingType.NULLABLE_ACTION_ID);
//		multiActivityEnable = isSettedTrue(ActivitySettingType.MULTI_ACTIVITY_ENABLE);

		
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
