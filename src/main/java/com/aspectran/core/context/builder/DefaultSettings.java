/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.builder;

import java.util.Map;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.BooleanUtils;

/**
 * The Class DefaultSettings
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class DefaultSettings implements Cloneable {
	
	private String transletNamePattern;
	
	private String transletNamePrefix;

	private String transletNameSuffix;
	
	private String transletInterfaceClassName;
	
	private Class<Translet> transletInterfaceClass;
	
	private String transletImplementClassName;
	
	private Class<CoreTranslet> transletImplementClass;
	
	private Boolean nullableContentId;
	
	private Boolean nullableActionId;
	
	private String beanProxifier;

	private Boolean pointcutPatternVerifiable;
	
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
					transletNamePrefix = null;
					transletNameSuffix = transletNamePattern.substring(1);
				} else if(index == (transletNamePattern.length() - 1)) {
					transletNamePrefix = transletNamePattern.substring(0, transletNamePattern.length() - 1);
					transletNameSuffix = null;
				} else {
					transletNamePrefix = transletNamePattern.substring(0, index);
					transletNameSuffix = transletNamePattern.substring(index + 1);
				}
			}
		}
	}
	
	public void setTransletNamePattern(String transletNamePrefix, String transletNameSuffix) {
		this.transletNamePattern = transletNamePrefix + AspectranConstant.TRANSLET_NAME_PATTERN_SEPARATOR + transletNameSuffix;
		this.transletNamePrefix = transletNamePrefix;
		this.transletNameSuffix = transletNameSuffix;
	}
	
	public void setTransletNamePrefix(String transletNamePrefix) {
		this.transletNamePrefix = transletNamePrefix;
		
		if(transletNameSuffix != null)
			setTransletNamePattern(transletNamePrefix, transletNameSuffix);
	}
	
	public void setTransletNameSuffix(String transletNameSuffix) {
		this.transletNameSuffix = transletNameSuffix;
		
		if(transletNamePrefix != null)
			setTransletNamePattern(transletNamePrefix, transletNameSuffix);
	}
	
	public String getTransletNamePrefix() {
		return transletNamePrefix;
	}

	public String getTransletNameSuffix() {
		return transletNameSuffix;
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
	
	public String getBeanProxifier() {
		return beanProxifier;
	}

	public void setBeanProxifier(String beanProxifier) {
		this.beanProxifier = beanProxifier;
	}

	public boolean isPointcutPatternVerifiable() {
		return BooleanUtils.toBoolean(pointcutPatternVerifiable, true);
	}
	
	public Boolean getPointcutPatternVerifiable() {
		return pointcutPatternVerifiable;
	}

	public void setPointcutPatternVerifiable(boolean pointcutPatternVerifiable) {
		this.pointcutPatternVerifiable = pointcutPatternVerifiable;
	}

	public void apply(Map<DefaultSettingType, String> settings) throws ClassNotFoundException {
		if(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN) != null)
			setTransletNamePattern(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN));
		
		if(settings.get(DefaultSettingType.TRANSLET_NAME_PREFIX) != null)
			setTransletNamePrefix(settings.get(DefaultSettingType.TRANSLET_NAME_PREFIX));
		
		if(settings.get(DefaultSettingType.TRANSLET_NAME_SUFFIX) != null)
			setTransletNameSuffix(settings.get(DefaultSettingType.TRANSLET_NAME_SUFFIX));
		
		if(settings.get(DefaultSettingType.TRANSLET_INTERFACE_CLASS) != null)
			setTransletInterfaceClassName(settings.get(DefaultSettingType.TRANSLET_INTERFACE_CLASS));
		
		if(settings.get(DefaultSettingType.TRANSLET_IMPLEMENT_CLASS) != null)
			setTransletImplementClassName(settings.get(DefaultSettingType.TRANSLET_IMPLEMENT_CLASS));

		if(settings.get(DefaultSettingType.NULLABLE_CONTENT_ID) != null)
			nullableContentId = (settings.get(DefaultSettingType.NULLABLE_CONTENT_ID) == null || Boolean.parseBoolean(settings.get(DefaultSettingType.NULLABLE_CONTENT_ID)));
		
		if(settings.get(DefaultSettingType.NULLABLE_ACTION_ID) != null)
			nullableActionId = (settings.get(DefaultSettingType.NULLABLE_ACTION_ID) == null || Boolean.parseBoolean(settings.get(DefaultSettingType.NULLABLE_ACTION_ID)));
		
		if(settings.get(DefaultSettingType.BEAN_PROXIFIER) != null)
			beanProxifier = settings.get(DefaultSettingType.BEAN_PROXIFIER);

		if(settings.get(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE) != null)
			pointcutPatternVerifiable = (settings.get(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE) == null || Boolean.parseBoolean(settings.get(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE)));
	}
	
	public DefaultSettings clone() throws CloneNotSupportedException {
		return (DefaultSettings)super.clone();              
	}
	
}
