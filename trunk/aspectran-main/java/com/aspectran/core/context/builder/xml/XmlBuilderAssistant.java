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
package com.aspectran.core.context.builder.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.util.ArrayStack;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.AspectRuleMap;
import com.aspectran.core.var.rule.BeanRule;
import com.aspectran.core.var.rule.BeanRuleMap;
import com.aspectran.core.var.rule.TransletRule;
import com.aspectran.core.var.rule.TransletRuleMap;
import com.aspectran.core.var.type.DefaultSettingType;


/**
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class XmlBuilderAssistant {

	private ArrayStack objectStack;
	
	private Map<DefaultSettingType, String> settings;
	
	private Map<String, String> typeAliases;
	
	private String applicationBasePath;

	private DefaultSettings inheritedDefaultSettings;

	private AspectRuleMap aspectRuleMap;
	
	private BeanRuleMap beanRuleMap;
	
	private TransletRuleMap transletRuleMap;

	private String namespace;
	
	private BeanReferenceInspector beanReferenceInspector;
	
	/**
	 * Instantiates a new translets config.
	 */
	public XmlBuilderAssistant(String applicationBasePath) {
		this.objectStack = new ArrayStack(); 
		this.typeAliases = new HashMap<String, String>();
		this.settings = new HashMap<DefaultSettingType, String>();
		this.beanReferenceInspector = new BeanReferenceInspector();
		
		this.applicationBasePath = applicationBasePath;
		inheritedDefaultSettings = new DefaultSettings();
	}
	
	/**
	 * 상속.
	 * Instantiates a new activity context builder assistant.
	 *
	 * @param assistant the assistant
	 */
	public XmlBuilderAssistant(XmlBuilderAssistant assistant) {
		applicationBasePath = assistant.getApplicationBasePath();
		inheritedDefaultSettings = assistant.getInheritedDefaultSettings();
		
		if(inheritedDefaultSettings == null)
			inheritedDefaultSettings = new DefaultSettings();
		
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
	 * Peek object.
	 * 
	 * @return the object
	 */
	public Object peekObject(int n) {
		return objectStack.peek(n);
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

	public Map<DefaultSettingType, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<DefaultSettingType, String> settings) {
		this.settings = settings;
		applyInheritedSettings();
	}

	public void putSetting(DefaultSettingType settingType, String value) {
		settings.put(settingType, value);
	}
	
	public Object getSetting(DefaultSettingType settingType) {
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
		inheritedDefaultSettings.set(getSettings());
	}
	
	/**
	 * Returns the trnaslet name of the prefix and suffix are combined.
	 * 
	 * @param transletName
	 * @return
	 */
	public String getFullTransletName(String transletName) {
		if(transletName != null && transletName.length() > 0 && transletName.charAt(0) == AspectranConstant.TRANSLET_NAME_SEPARATOR)
			return transletName;
		
		if(inheritedDefaultSettings.getTransletNamePatternPrefix() == null && 
				inheritedDefaultSettings.getTransletNamePatternSuffix() == null)
			return transletName;
		
		StringBuilder sb = new StringBuilder();
		
		if(inheritedDefaultSettings.getTransletNamePatternPrefix() != null)
			sb.append(inheritedDefaultSettings.getTransletNamePatternPrefix());
		
		sb.append(transletName);
		
		if(inheritedDefaultSettings.getTransletNamePatternSuffix() != null)
			sb.append(inheritedDefaultSettings.getTransletNamePatternSuffix());
		
		return sb.toString();
	}
	
	/**
	 * Apply namespace for a translet.
	 * 
	 * @param transletName the name
	 * 
	 * @return the string
	 */
	public String applyNamespaceForTranslet(String transletName) {
		if(transletName != null && transletName.length() > 0 && transletName.charAt(0) == AspectranConstant.TRANSLET_NAME_SEPARATOR)
			return transletName;
		
		StringBuilder sb = new StringBuilder();
		
		if(inheritedDefaultSettings.getTransletNamePatternPrefix() != null)
			sb.append(inheritedDefaultSettings.getTransletNamePatternPrefix());
		
		if(inheritedDefaultSettings.isUseNamespaces() && namespace != null) {
			sb.append(namespace);
			sb.append(AspectranConstant.TRANSLET_NAME_SEPARATOR);
		}
		
		sb.append(transletName);
		
		if(inheritedDefaultSettings.getTransletNamePatternSuffix() != null)
			sb.append(inheritedDefaultSettings.getTransletNamePatternSuffix());
		
		return sb.toString();
	}
	
	public String applyNamespaceForBean(String beanId) {
		if(!inheritedDefaultSettings.isUseNamespaces() || namespace == null)
			return beanId;
		
		StringBuilder sb = new StringBuilder();
		sb.append(namespace);
		sb.append(AspectranConstant.BEAN_ID_SEPARATOR);
		sb.append(beanId);
		
		return sb.toString();
	}
/*	
	public String replaceTransletNameSuffix(String name, String transletNameSuffix) {
		if(inheritedAspectranSettings.getTransletNamePatternSuffix() == null)
			return name + AspectranConstant.TRANSLET_NAME_EXTENSION_DELIMITER + transletNameSuffix;
		
		int index = name.indexOf(inheritedAspectranSettings.getTransletNamePatternSuffix());
		
		StringBuilder sb = new StringBuilder();
		sb.append(name.substring(0, index));
		sb.append(AspectranConstant.TRANSLET_NAME_EXTENSION_DELIMITER);
		sb.append(transletNameSuffix);
		
		return sb.toString();
	}
*/	
	/**
	 * Checks if is allow null content id.
	 * 
	 * @return true, if is allow null content id
	 */
	public boolean isNullableContentId() {
		return inheritedDefaultSettings.isNullableContentId();
	}

	/**
	 * Checks if is allow null action id.
	 * 
	 * @return true, if is allow null action id
	 */
	public boolean isNullableActionId() {
		return inheritedDefaultSettings.isNullableActionId();
	}

	public String getApplicationBasePath() {
		return applicationBasePath;
	}
	
	public DefaultSettings getInheritedDefaultSettings() {
		return inheritedDefaultSettings;
	}

	public void setInheritedDefaultSettings(DefaultSettings defaultSettings) {
		this.inheritedDefaultSettings = defaultSettings;
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
	
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleMap;
	}

	public void setAspectRuleMap(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}
	
	public void addAspectRule(AspectRule aspectRule) {
		if(aspectRuleMap == null)
			aspectRuleMap = new AspectRuleMap();
		
		aspectRuleMap.putAspectRule(aspectRule);
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

		if(applicationBasePath != null && !filePath.startsWith("/"))
			file = new File(applicationBasePath, filePath);
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
	
	public void putBeanReference(String beanId, Object rule) {
		if(beanRuleMap == null || !beanRuleMap.containsKey(beanId)) {
			beanReferenceInspector.putRelation(beanId, rule);
		}
	}
	
	public BeanReferenceInspector getBeanReferenceInspector() {
		return beanReferenceInspector;
	}
	
}
