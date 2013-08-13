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
package com.aspectran.core.context.builder;

import java.util.Map;

import com.aspectran.core.rule.ExceptionHandlingRule;
import com.aspectran.core.type.AspectranSettingType;

/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class InheritedAspectranSettings {

	private String transletNameSeparator;
	
	private String transletNamePattern;
	
	private String transletNamePatternPrefix;

	private String transletNamePatternSuffix;
	
	private String transletInterfaceClass;

	private String transletInstanceClass;
	
	private boolean useNamespaces = true;

	private boolean nullableContentId = true;
	
	private boolean nullableActionId = true;
	
	private boolean multipleTransletEnable = true;
	
	private ExceptionHandlingRule defaultExceptionRule;
	
	protected InheritedAspectranSettings() {
	}
	
	public String getTransletNameSeparator() {
		return transletNameSeparator;
	}

	public void setTransletNameSeparator(String transletNameSeparator) {
		this.transletNameSeparator = transletNameSeparator;
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
	
	public String getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	public void setTransletInterfaceClass(String transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	public String getTransletInstanceClass() {
		return transletInstanceClass;
	}

	public void setTransletInstanceClass(String transletInstanceClass) {
		this.transletInstanceClass = transletInstanceClass;
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

	public boolean isMultipleTransletEnable() {
		return multipleTransletEnable;
	}

	public void setMultipleTransletEnable(boolean multipleTransletEnable) {
		this.multipleTransletEnable = multipleTransletEnable;
	}

	/**
	 * Gets the generic exception rule.
	 * 
	 * @return the generic exception rule
	 */
	public ExceptionHandlingRule getDefaultExceptionRule() {
		return defaultExceptionRule;
	}

	/**
	 * Sets the generic exception rule.
	 * 
	 * @param defaultExceptionRule the new generic exception rule
	 */
	public void setDefaultExceptionRule(ExceptionHandlingRule defaultExceptionRule) {
		this.defaultExceptionRule = defaultExceptionRule;
	}
	
	public void set(Map<AspectranSettingType, String> settings) {
		if(settings.get(AspectranSettingType.USE_NAMESPACES) != null)
			useNamespaces = Boolean.valueOf(settings.get(AspectranSettingType.USE_NAMESPACES));

		if(settings.get(AspectranSettingType.NULLABLE_CONTENT_ID) != null)
			nullableContentId = Boolean.valueOf(settings.get(AspectranSettingType.NULLABLE_CONTENT_ID));
		
		if(settings.get(AspectranSettingType.NULLABLE_ACTION_ID) != null)
			nullableActionId = Boolean.valueOf(settings.get(AspectranSettingType.NULLABLE_ACTION_ID));
		
		if(settings.get(AspectranSettingType.MULTIPLE_TRANSLET_ENABLE) != null)
			multipleTransletEnable = Boolean.valueOf(settings.get(AspectranSettingType.MULTIPLE_TRANSLET_ENABLE));
		
		if(settings.get(AspectranSettingType.TRANSLET_INTERFACE_CLASS) != null)
			transletInterfaceClass = settings.get(AspectranSettingType.TRANSLET_INTERFACE_CLASS);
		
		if(settings.get(AspectranSettingType.TRANSLET_INSTANCE_CLASS) != null)
			transletInstanceClass = settings.get(AspectranSettingType.TRANSLET_INSTANCE_CLASS);
		
		
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
