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

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.BooleanUtils;

/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class DefaultSettings implements Cloneable {

	private String transletNamePattern;
	
	private String transletNamePatternPrefix;

	private String transletNamePatternSuffix;
	
	private String transletInterfaceClassName;
	
	private Class<Translet> transletInterfaceClass;
	
	private String transletImplementClassName;
	
	private Class<CoreTranslet> transletImplementClass;
	
	private Boolean nullableContentId;
	
	private Boolean nullableActionId;
	
	private String activityDefaultHandler;
	
	private String beanProxyMode;
	
	public DefaultSettings() {
	}
	
	public String getTransletNamePattern() {
		return transletNamePattern;
	}

	public void setTransletNamePattern(String transletNamePattern) {
		this.transletNamePattern = transletNamePattern;
		
		if(transletNamePattern != null) {
			int index = transletNamePattern.indexOf(AspectranConstant.TRANSLET_NAME_PATTERN_SEPARATOR);
			
			if(index != -1) {
				if(index == 0) {
					transletNamePatternPrefix = null;
					transletNamePatternSuffix = transletNamePattern.substring(1);
				} else if(index == (transletNamePattern.length() - 1)) {
					transletNamePatternPrefix = transletNamePattern.substring(0, transletNamePattern.length() - 1);
					transletNamePatternSuffix = null;
				} else {
					transletNamePatternPrefix = transletNamePattern.substring(0, index);
					transletNamePatternSuffix = transletNamePattern.substring(index + 1);
				}
			}
		}
	}
	
	public void setTransletNamePattern(String transletNamePatternPrefix, String transletNamePatternSuffix) {
		transletNamePattern = transletNamePatternPrefix + AspectranConstant.TRANSLET_NAME_PATTERN_SEPARATOR + transletNamePatternSuffix;
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
	
	public String getTransletInterfaceClassName() {
		return transletInterfaceClassName;
	}

	public void setTransletInterfaceClassName(String transletInterfaceClassName) {
		this.transletInterfaceClassName = transletInterfaceClassName;
	}

	public Class<Translet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	public void setTransletInterfaceClass(Class<Translet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	public String getTransletImplementClassName() {
		return transletImplementClassName;
	}

	public void setTransletImplementClassName(String transletImplementClassName) {
		this.transletImplementClassName = transletImplementClassName;
	}

	public Class<CoreTranslet> getTransletImplementClass() {
		return transletImplementClass;
	}

	public void setTransletImplementClass(Class<CoreTranslet> transletImplementClass) {
		this.transletImplementClass = transletImplementClass;
	}

	public boolean isNullableContentId() {
		return BooleanUtils.toBoolean(nullableContentId, true);
	}

	public Boolean getNullableContentId() {
		return nullableContentId;
	}

	public void setNullableContentId(boolean nullableContentId) {
		this.nullableContentId = nullableContentId;
	}

	public boolean isNullableActionId() {
		return BooleanUtils.toBoolean(nullableActionId, true);
	}

	public Boolean getNullableActionId() {
		return nullableActionId;
	}

	public void setNullableActionId(boolean nullableActionId) {
		this.nullableActionId = nullableActionId;
	}
	
	public String getActivityDefaultHandler() {
		return activityDefaultHandler;
	}
	
	public void setActivityDefaultHandler(String activityDefaultHandler) {
		this.activityDefaultHandler = activityDefaultHandler;
	}
	
	public String getBeanProxyMode() {
		return beanProxyMode;
	}

	public void setBeanProxyMode(String beanProxyMode) {
		this.beanProxyMode = beanProxyMode;
	}

	public void apply(Map<DefaultSettingType, String> settings) throws ClassNotFoundException {
		if(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN) != null)
			setTransletNamePattern(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN));
		
		if(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN_PREFIX) != null)
			setTransletNamePatternPrefix(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN_PREFIX));
		
		if(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN_SUFFIX) != null)
			setTransletNamePatternSuffix(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN_SUFFIX));
		
		if(settings.get(DefaultSettingType.TRANSLET_INTERFACE_CLASS) != null)
			setTransletInterfaceClassName(settings.get(DefaultSettingType.TRANSLET_INTERFACE_CLASS));
		
		if(settings.get(DefaultSettingType.TRANSLET_IMPLEMENT_CLASS) != null)
			setTransletImplementClassName(settings.get(DefaultSettingType.TRANSLET_IMPLEMENT_CLASS));

		if(settings.get(DefaultSettingType.NULLABLE_CONTENT_ID) != null)
			nullableContentId = (settings.get(DefaultSettingType.NULLABLE_CONTENT_ID) == null || Boolean.parseBoolean(settings.get(DefaultSettingType.NULLABLE_CONTENT_ID)));
		
		if(settings.get(DefaultSettingType.NULLABLE_ACTION_ID) != null)
			nullableActionId = (settings.get(DefaultSettingType.NULLABLE_ACTION_ID) == null || Boolean.parseBoolean(settings.get(DefaultSettingType.NULLABLE_ACTION_ID)));
		
		if(settings.get(DefaultSettingType.ACTIVITY_DEFAULT_HANDLER) != null)
			activityDefaultHandler = settings.get(DefaultSettingType.ACTIVITY_DEFAULT_HANDLER);
		
		if(settings.get(DefaultSettingType.BEAN_PROXY_MODE) != null)
			beanProxyMode = settings.get(DefaultSettingType.BEAN_PROXY_MODE);
	}
	
	public DefaultSettings clone() throws CloneNotSupportedException {                      
		return (DefaultSettings)super.clone();              
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
