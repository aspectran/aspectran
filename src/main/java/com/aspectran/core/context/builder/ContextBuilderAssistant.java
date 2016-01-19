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
package com.aspectran.core.context.builder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.bean.BeanRuleRegistry;
import com.aspectran.core.context.rule.*;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.template.TemplateRuleRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ArrayStack;

/**
 * The Class ContextBuilderAssistant
 * 
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class ContextBuilderAssistant {

	private ApplicationAdapter applicationAdapter;
	
	private String applicationBasePath;

	private ClassLoader classLoader;
	
	private ArrayStack objectStack = new ArrayStack();
	
	private AssistantLocal assistantLocal = new AssistantLocal();
	
	private Map<String, String> typeAliases = new HashMap<String, String>();
	
	private Map<DefaultSettingType, String> settings = new HashMap<DefaultSettingType, String>();
	
	private BeanReferenceInspector beanReferenceInspector = new BeanReferenceInspector();
	
	private final AspectRuleRegistry aspectRuleRegistry;
	
	private final BeanRuleRegistry beanRuleRegistry;

	private final TemplateRuleRegistry templateRuleRegistry;

	private final TransletRuleRegistry transletRuleRegistry;
	
	private ImportHandler importHandler;
	
	private boolean hybridLoading;
	
	public ContextBuilderAssistant(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
		this.applicationBasePath = applicationAdapter.getApplicationBasePath();
		this.classLoader = applicationAdapter.getClassLoader();
		
		aspectRuleRegistry = new AspectRuleRegistry();
		beanRuleRegistry = new BeanRuleRegistry(classLoader);
		transletRuleRegistry = new TransletRuleRegistry(applicationAdapter);
		transletRuleRegistry.setAssistantLocal(assistantLocal);
		templateRuleRegistry = new TemplateRuleRegistry();
	}
	
	protected ContextBuilderAssistant() {
		this.aspectRuleRegistry = null;
		this.beanRuleRegistry = null;
		this.transletRuleRegistry = null;
		this.templateRuleRegistry = null;
	}
	
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public String getApplicationBasePath() {
		return applicationBasePath;
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public boolean isHybridLoading() {
		return hybridLoading;
	}

	public void setHybridLoading(boolean hybridLoading) {
		this.hybridLoading = hybridLoading;
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
	 * @param <T> the generic type
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public <T> T popObject() {
		return (T)objectStack.pop();
	}
	
	/**
	 * Peek object.
	 *
	 * @param <T> the generic type
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public <T> T peekObject() {
		return (T)objectStack.peek();
	}
	
	/**
	 * Peek object.
	 *
	 * @param <T> the generic type
	 * @param n the n
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public <T> T peekObject(int n) {
		return (T)objectStack.peek(n);
	}
	
	/**
	 * Clear object stack.
	 */
	public void clearObjectStack() {
		objectStack.clear();
	}

	/**
	 * Gets the settings.
	 *
	 * @return the settings
	 */
	public Map<DefaultSettingType, String> getSettings() {
		return settings;
	}

	/**
	 * Sets the settings.
	 *
	 * @param settings the settings
	 * @throws ClassNotFoundException the class not found exception
	 */
	public void setSettings(Map<DefaultSettingType, String> settings) throws ClassNotFoundException {
		this.settings = settings;
		applySettings();
	}

	/**
	 * Put setting.
	 *
	 * @param settingType the setting type
	 * @param value the value
	 */
	public void putSetting(DefaultSettingType settingType, String value) {
		settings.put(settingType, value);
	}
	
	/**
	 * Gets the setting.
	 *
	 * @param settingType the setting type
	 * @return the setting
	 */
	public Object getSetting(DefaultSettingType settingType) {
		return settings.get(settingType);
	}
	
	/**
	 * Apply settings.
	 *
	 * @throws ClassNotFoundException the class not found exception
	 */
	public void applySettings() throws ClassNotFoundException {
		DefaultSettings defaultSettings = assistantLocal.touchDefaultSettings();
		defaultSettings.apply(getSettings());

		applyTransletInterface(defaultSettings);
	}
	
	/**
	 * Apply translet interface.
	 *
	 * @param defaultSettings the default settings
	 * @throws ClassNotFoundException the class not found exception
	 */
	@SuppressWarnings("unchecked")
	public void applyTransletInterface(DefaultSettings defaultSettings) throws ClassNotFoundException {
		if(defaultSettings.getTransletInterfaceClassName() != null) {
			Class<?> transletInterfaceClass = classLoader.loadClass(defaultSettings.getTransletInterfaceClassName());
			defaultSettings.setTransletInterfaceClass((Class<Translet>)transletInterfaceClass);
		}
		if(defaultSettings.getTransletImplementClassName() != null) {
			Class<?> transletImplementClass = classLoader.loadClass(defaultSettings.getTransletImplementClassName());
			defaultSettings.setTransletImplementClass((Class<CoreTranslet>)transletImplementClass);
		}
	}
	
	/**
	 * Gets the type aliases.
	 *
	 * @return the type aliases
	 */
	public Map<String, String> getTypeAliases() {
		return typeAliases;
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
	 * Returns the resolve alias type.
	 * 
	 * @param alias the alias
	 * 
	 * @return the string
	 */
	public String resolveAliasType(String alias) {
		String type = getAliasType(alias);

		if(type == null)
			return alias;

		return type;
	}
	
	/**
	 * Clear type aliases.
	 */
	public void clearTypeAliases() {
		typeAliases.clear();
	}
	
	/**
	 * Returns the trnaslet name of the prefix and suffix are combined.
	 * 
	 * @param transletName the translet name
	 * 
	 * @return the string
	 */
	public String applyTransletNamePattern(String transletName) {
		return transletRuleRegistry.applyTransletNamePattern(transletName);
	}
	
	/**
	 * Gets the assistant local.
	 *
	 * @return the assistant local
	 */
	public AssistantLocal getAssistantLocal() {
		return assistantLocal;
	}

	/**
	 * Sets the assistant local.
	 *
	 * @param assistantLocal the new assistant local
	 */
	public void setAssistantLocal(AssistantLocal assistantLocal) {
		this.assistantLocal = assistantLocal;
		transletRuleRegistry.setAssistantLocal(assistantLocal);
	}

	/**
	 * Backup assistant local.
	 *
	 * @return the assistant local
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public AssistantLocal backupAssistantLocal() throws CloneNotSupportedException {
		AssistantLocal oldAssistantLocal = assistantLocal;

		setAssistantLocal(assistantLocal.clone());
		
		return oldAssistantLocal;
	}

	/**
	 * Restore assistant local.
	 *
	 * @param assistantLocal the assistant local
	 */
	public void restoreAssistantLocal(AssistantLocal assistantLocal) {
		setAssistantLocal(assistantLocal);
	}
	
	/**
	 * Checks if is allow null content id.
	 * 
	 * @return true, if is allow null content id
	 */
	public boolean isNullableContentId() {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		
		if(defaultSettings == null)
			return true;
		
		return defaultSettings.isNullableContentId();
	}

	/**
	 * Checks if is allow null action id.
	 * 
	 * @return true, if is allow null action id
	 */
	public boolean isNullableActionId() {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		
		if(defaultSettings == null)
			return true;

		return defaultSettings.isNullableActionId();
	}

	/**
	 * Checks if is pointcut pattern verifiable.
	 *
	 * @return true, if is pointcut pattern verifiable
	 */
	public boolean isPointcutPatternVerifiable() {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		
		if(defaultSettings == null)
			return true;
		
		return defaultSettings.isPointcutPatternVerifiable();
	}

	/**
	 * Adds the aspect rule.
	 *
	 * @param aspectRule the aspect rule
	 */
	public void addAspectRule(AspectRule aspectRule) {
		aspectRuleRegistry.addAspectRule(aspectRule);
	}

	/**
	 * Adds the bean rule.
	 *
	 * @param beanRule the bean rule
	 * @throws CloneNotSupportedException the clone not supported exception
	 * @throws ClassNotFoundException the class not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addBeanRule(BeanRule beanRule) throws CloneNotSupportedException, ClassNotFoundException, IOException {
		beanRuleRegistry.addBeanRule(beanRule);
	}

	/**
	 * Add translet rule.
	 *
	 * @param transletRule the translet rule
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public void addTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		transletRuleRegistry.addTransletRule(transletRule);
	}

	/**
	 * Add template rule.
	 *
	 * @param templateRule the template rule
	 */
	public void addTemplateRule(TemplateRule templateRule) {
		templateRuleRegistry.addTemplateRule(templateRule);
	}

	/**
	 * Gets aspect rule registry.
	 *
	 * @return the aspect rule registry
	 */
	public AspectRuleRegistry getAspectRuleRegistry() {
		return aspectRuleRegistry;
	}

	/**
	 * Gets bean rule registry.
	 *
	 * @return the bean rule registry
	 */
	public BeanRuleRegistry getBeanRuleRegistry() {
		return beanRuleRegistry;
	}

	/**
	 * Gets translet rule registry.
	 *
	 * @return the translet rule registry
	 */
	public TransletRuleRegistry getTransletRuleRegistry() {
		return transletRuleRegistry;
	}

	/**
	 * Gets template rule registry.
	 *
	 * @return the template rule registry
	 */
	public TemplateRuleRegistry getTemplateRuleRegistry() {
		return templateRuleRegistry;
	}

	/**
	 * Gets aspect rule map.
	 *
	 * @return the aspect rule map
	 */
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleRegistry.getAspectRuleMap();
	}

	/**
	 * Gets bean rule map.
	 *
	 * @return the bean rule map
	 */
	public BeanRuleMap getBeanRuleMap() {
		return beanRuleRegistry.getBeanRuleMap();
	}

	/**
	 * Gets template rule map.
	 *
	 * @return the template rule map
	 */
	public TemplateRuleMap getTemplateRuleMap() {
		return templateRuleRegistry.getTemplateRuleMap();
	}

	/**
	 * Gets translet rule map.
	 *
	 * @return the translet rule map
	 */
	public TransletRuleMap getTransletRuleMap() {
		return transletRuleRegistry.getTransletRuleMap();
	}

	public String putBeanReference(String beanId, Object rule) {
		if(beanRuleRegistry != null && !beanRuleRegistry.contains(beanId)) {
			beanReferenceInspector.putRelation(beanId, rule);
		}
		
		return beanId;
	}
	
	public BeanReferenceInspector getBeanReferenceInspector() {
		return beanReferenceInspector;
	}

	public ImportHandler getImportHandler() {
		return importHandler;
	}

	public void setImportHandler(ImportHandler importHandler) {
		this.importHandler = importHandler;
	}
	
}
