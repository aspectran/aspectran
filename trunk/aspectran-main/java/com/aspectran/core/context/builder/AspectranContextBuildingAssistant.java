/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.rule.BeanRule;
import com.aspectran.core.rule.BeanRuleMap;
import com.aspectran.core.rule.ExceptionHandlingRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.TransletRuleMap;
import com.aspectran.core.type.AspectranSettingType;
import com.aspectran.core.util.ArrayStack;

/**
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class AspectranContextBuildingAssistant {

	/** The object stack. */
	private ArrayStack objectStack;
	
	private Map<AspectranSettingType, String> settings;
	
	/** The type aliases. */
	private Map<String, String> typeAliases;
	
	/** The service root path. */
	private String activityRootPath;

	private InheritedAspectranSettings inheritedSettings;

	private BeanRuleMap beanRuleMap;
	
	private TransletRuleMap transletRuleMap;

	private String namespace;
	
	/**
	 * Instantiates a new translets config.
	 */
	public AspectranContextBuildingAssistant(String serviceRootPath) {
		this.objectStack = new ArrayStack(); 
		this.typeAliases = new HashMap<String, String>();
		this.settings = new HashMap<AspectranSettingType, String>();
		
		this.activityRootPath = serviceRootPath;
		inheritedSettings = new InheritedAspectranSettings();
	}
	
	/**
	 * 상속.
	 * Instantiates a new activity context builder assistant.
	 *
	 * @param assistant the assistant
	 */
	public AspectranContextBuildingAssistant(AspectranContextBuildingAssistant assistant) {
		activityRootPath = assistant.getActivityRootPath();
		inheritedSettings = assistant.getActivitySettingsRule();
		
		if(inheritedSettings == null)
			inheritedSettings = new InheritedAspectranSettings();
		
		beanRuleMap = assistant.getBeanRuleMap();
		transletRuleMap = assistant.getTransletRuleMap();
	}
	
	/**
	 * Push object.
	 * 
	 * @param object the item
	 */
	public void pushObject(Object object) {
		objectStack.push(object);
	}
	
	/**
	 * Pop object.
	 * 
	 * @return the object
	 */
	public Object popObject() {
		return objectStack.pop();
	}
	
	/**
	 * Peek object.
	 * 
	 * @return the object
	 */
	public Object peekObject() {
		return objectStack.peek();
	}
	
	/**
	 * Clear object stack.
	 */
	public void clearObjectStack() {
		objectStack.clear();
	}
	
	/**
	 * Clear type aliases.
	 */
	public void clearTypeAliases() {
		typeAliases.clear();
	}

	public Map<AspectranSettingType, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<AspectranSettingType, String> settings) {
		this.settings = settings;
		applyInheritedSettings();
	}

	public void putSetting(AspectranSettingType settingType, String value) {
		settings.put(settingType, value);
	}
	
	public Object getSetting(AspectranSettingType settingType) {
		return settings.get(settingType);
	}
	
	/**
	 * Adds the type alias.
	 * 
	 * @param alias the alias
	 * @param type the type
	 */
	public void addTypeAlias(String alias, String type) {
		typeAliases.put(alias, type);
	}
	
	/**
	 * Gets the alias type.
	 * 
	 * @param alias the alias
	 * 
	 * @return the alias type
	 */
	public String getAliasType(String alias) {
		return typeAliases.get(alias);
	}
	
	/**
	 * Sets the namespace.
	 * 
	 * @param namespace the new namespace
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public void applyInheritedSettings() {
		inheritedSettings.set(getSettings());
	}
	
	/**
	 * Apply namespace for a translet.
	 * 
	 * @param transletName the name
	 * 
	 * @return the string
	 */
	public String applyNamespaceForTranslet(String transletName) {
		StringBuilder sb = new StringBuilder();
		
		if(inheritedSettings.getTransletNamePatternPrefix() != null)
			sb.append(inheritedSettings.getTransletNamePatternPrefix());
		
		if(inheritedSettings.isUseNamespaces() && namespace != null) {
			sb.append(namespace);
			sb.append(AspectranContextConstant.TRANSLET_NAME_SEPARATOR);
		}
			
		sb.append(transletName);
		
		if(inheritedSettings.getTransletNamePatternSuffix() != null)
			sb.append(inheritedSettings.getTransletNamePatternSuffix());
		
		return sb.toString();
	}
	
	public String applyNamespaceForBean(String beanId) {
		if(!inheritedSettings.isUseNamespaces() || namespace == null)
			return beanId;
		
		StringBuilder sb = new StringBuilder();
		sb.append(namespace);
		sb.append(AspectranContextConstant.BEAN_ID_SEPARATOR);
		sb.append(beanId);
		return sb.toString();
	}
	
	public String replaceTransletNameSuffix(String name, String transletNameSuffix) {
		if(inheritedSettings.getTransletNamePatternSuffix() == null)
			return name + transletNameSuffix;
		
		int index = name.indexOf(inheritedSettings.getTransletNamePatternSuffix());
		
		StringBuilder sb = new StringBuilder();
		sb.append(name.substring(0, index));
		sb.append(AspectranContextConstant.TRANSLET_NAME_SUFFIX_SEPARATOR);
		sb.append(transletNameSuffix);
		
		return sb.toString();
	}
	
	/**
	 * Checks if is allow null content id.
	 * 
	 * @return true, if is allow null content id
	 */
	public boolean isNullableContentId() {
		return inheritedSettings.isNullableContentId();
	}

	/**
	 * Checks if is allow null action id.
	 * 
	 * @return true, if is allow null action id
	 */
	public boolean isNullableActionId() {
		return inheritedSettings.isNullableActionId();
	}
	
	public boolean isMultipleTransletEnable() {
		return inheritedSettings.isMultipleTransletEnable();
	}

	public String getActivityRootPath() {
		return activityRootPath;
	}
	
	public InheritedAspectranSettings getActivitySettingsRule() {
		return inheritedSettings;
	}

	public void setActivitySettingsRule(InheritedAspectranSettings activityRule) {
		this.inheritedSettings = activityRule;
	}

	/**
	 * Gets the generic exception rule.
	 * 
	 * @return the generic exception rule
	 */
	public ExceptionHandlingRule getDefaultExceptionRule() {
		return inheritedSettings.getDefaultExceptionRule();
	}

	/**
	 * Sets the generic exception rule.
	 * 
	 * @param defaultExceptionRule the new generic exception rule
	 */
	public void setDefaultExceptionRule(ExceptionHandlingRule defaultExceptionRule) {
		inheritedSettings.setDefaultExceptionRule(defaultExceptionRule);
	}

	/**
	 * Gets the bean rule map.
	 * 
	 * @return the bean rule map
	 */
	public BeanRuleMap getBeanRuleMap() {
		return beanRuleMap;
	}

	/**
	 * Sets the bean rule map.
	 * 
	 * @param beanRuleMap the new bean rule map
	 */
	public void setBeanRuleMap(BeanRuleMap beanRuleMap) {
		this.beanRuleMap = beanRuleMap;
	}
	
	/**
	 * Adds the bean rule.
	 *
	 * @param beanRule the bean rule
	 */
	public void addBeanRule(BeanRule beanRule) {
		if(beanRuleMap == null)
			beanRuleMap = new BeanRuleMap();
		
		beanRuleMap.putBeanRule(beanRule);
	}
	
	/**
	 * To real path as file.
	 * 
	 * @param filePath the file path
	 * 
	 * @return the file
	 */
	public File toRealPathFile(String filePath) {
		File file;

		if(activityRootPath != null && !filePath.startsWith("/"))
			file = new File(activityRootPath, filePath);
		else
			file = new File(filePath);
		
		return file;
	}
	
	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

	public void addTransletRule(TransletRule transletRule) {
		if(transletRuleMap == null)
			transletRuleMap = new TransletRuleMap();
		
		transletRuleMap.put(transletRule.getName(), transletRule);
	}
	
}
