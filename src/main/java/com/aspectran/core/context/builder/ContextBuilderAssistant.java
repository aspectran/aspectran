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
package com.aspectran.core.context.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.activity.GenericTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.bean.BeanRuleRegistry;
import com.aspectran.core.context.builder.env.Environment;
import com.aspectran.core.context.builder.importer.FileImporter;
import com.aspectran.core.context.builder.importer.ImportHandler;
import com.aspectran.core.context.builder.importer.Importer;
import com.aspectran.core.context.builder.importer.ResourceImporter;
import com.aspectran.core.context.builder.importer.UrlImporter;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.template.TemplateRuleRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ArrayStack;
import com.aspectran.core.util.BeanDescriptor;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ProfilesUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class ContextBuilderAssistant
 * 
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class ContextBuilderAssistant {

	protected final Log log = LogFactory.getLog(getClass());
	
	private ApplicationAdapter applicationAdapter;
	
	private String applicationBasePath;

	private ClassLoader classLoader;
	
	private ArrayStack objectStack = new ArrayStack();
	
	private AssistantLocal assistantLocal = new AssistantLocal();
	
	private Map<String, String> typeAliases = new HashMap<>();
	
	private Map<DefaultSettingType, String> settings = new HashMap<>();
	
	private BeanReferenceInspector beanReferenceInspector = new BeanReferenceInspector();
	
	private AspectRuleRegistry aspectRuleRegistry;
	
	private BeanRuleRegistry beanRuleRegistry;

	private TemplateRuleRegistry templateRuleRegistry;

	private TransletRuleRegistry transletRuleRegistry;
	
	private ImportHandler importHandler;
	
	public ContextBuilderAssistant() {
	}
	
	public void readyAssist(Environment environment) {
		this.applicationAdapter = environment.getApplicationAdapter();
		this.applicationBasePath = applicationAdapter.getApplicationBasePath();
		this.classLoader = applicationAdapter.getClassLoader();
		
		aspectRuleRegistry = new AspectRuleRegistry();
		
		beanRuleRegistry = new BeanRuleRegistry(environment);
		
		transletRuleRegistry = new TransletRuleRegistry(applicationAdapter);
		transletRuleRegistry.setAssistantLocal(assistantLocal);
		beanRuleRegistry.setTransletRuleRegistry(transletRuleRegistry);
		
		templateRuleRegistry = new TemplateRuleRegistry();
		templateRuleRegistry.setAssistantLocal(assistantLocal);

		BeanDescriptor.clearCache();
		MethodUtils.clearCache();
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
	
	public void pushObject(Object object) {
		objectStack.push(object);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T popObject() {
		return (T)objectStack.pop();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T peekObject() {
		return (T)objectStack.peek();
	}
	
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
		if(defaultSettings.getTransletImplementationClassName() != null) {
			Class<?> transletImplementationClass = classLoader.loadClass(defaultSettings.getTransletImplementationClassName());
			defaultSettings.setTransletImplementationClass((Class<GenericTranslet>)transletImplementationClass);
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
	 * @return the alias type
	 */
	public String getAliasType(String alias) {
		return typeAliases.get(alias);
	}
	
	/**
	 * Returns the resolve alias type.
	 * 
	 * @param alias the alias
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
	 * Returns the translet name of the prefix and suffix are combined.
	 *
	 * @param transletName the translet name
	 * @return the string
	 */
	public String applyTransletNamePattern(String transletName) {
		return transletRuleRegistry.applyTransletNamePattern(transletName, true);
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
	 */
	public AssistantLocal backupAssistantLocal() {
		AssistantLocal oldAssistantLocal = assistantLocal;

		setAssistantLocal(assistantLocal.replicate());
		
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
	 * Checks if is allow null action id.
	 * 
	 * @return true, if is allow null action id
	 */
	public boolean isNullableActionId() {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		return defaultSettings == null || defaultSettings.isNullableActionId();

	}

	/**
	 * Checks if is pointcut pattern verifiable.
	 *
	 * @return true, if is pointcut pattern verifiable
	 */
	public boolean isPointcutPatternVerifiable() {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		return defaultSettings == null || defaultSettings.isPointcutPatternVerifiable();
	}

	/**
	 * Resolve bean class for aspect rule.
	 *
	 * @param beanId the bean id
	 * @param aspectRule the aspect rule
	 */
	public void resolveBeanClass(String beanId, AspectRule aspectRule) {
		Class<?> beanClass = resolveBeanClass(beanId);
        if(beanClass != null) {
			aspectRule.setAdviceBeanClass(beanClass);
            putBeanReference(beanClass, aspectRule);
        } else {
            putBeanReference(beanId, aspectRule);
        }
	}

	/**
	 * Resolve bean class for bean action rule.
	 *
	 * @param beanId the bean id
	 * @param beanActionRule the aspect rule
	 */
	public void resolveBeanClass(String beanId, BeanActionRule beanActionRule) {
		Class<?> beanClass = resolveBeanClass(beanId);
        if(beanClass != null) {
			beanActionRule.setBeanClass(beanClass);
            putBeanReference(beanClass, beanActionRule);
        } else {
            putBeanReference(beanId, beanActionRule);
        }
	}

	/**
	 * Resolve bean class for bean rule.
	 *
	 * @param beanId the bean id
	 * @param beanRule the aspect rule
	 */
	public void resolveBeanClass(String beanId, BeanRule beanRule) {
		Class<?> beanClass = resolveBeanClass(beanId);
        if(beanClass != null) {
			beanRule.setOfferBeanClass(beanClass);
            putBeanReference(beanClass, beanRule);
        } else {
            putBeanReference(beanId, beanRule);
        }
	}

	/**
	 * Resolve bean class for token.
	 *
	 * @param token the token
	 */
	public void resolveBeanClass(Token token) {
		String name = token.getName();
		if(name != null) {
			if(name.equals(BeanRule.CLASS_DIRECTIVE)) {
				Class<?> beanClass = loadClass(token.getValue());
				token.setBeanClass(beanClass);
				putBeanReference(beanClass, token);
			} else {
				putBeanReference(name, token);
			}
		}
	}

	private Class<?> resolveBeanClass(String beanId) {
		if(beanId != null && beanId.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
			String className = beanId.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
			return loadClass(className);
		}
		return null;
	}

	private Class<?> loadClass(String className) {
		try {
			return classLoader.loadClass(className);
		} catch(ClassNotFoundException e) {
			throw new IllegalArgumentException("Failed to load class: " + className, e);
		}
	}

	public void putBeanReference(String beanId, BeanReferenceInspectable someRule) {
		beanReferenceInspector.putRelation(beanId, someRule);
	}

	public void putBeanReference(Class<?> beanClass, BeanReferenceInspectable someRule) {
		beanReferenceInspector.putRelation(beanClass, someRule);
	}

	public BeanReferenceInspector getBeanReferenceInspector() {
		return beanReferenceInspector;
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
	 * @throws ClassNotFoundException the class not found exception
	 */
	public void addBeanRule(BeanRule beanRule) throws ClassNotFoundException {
		beanRuleRegistry.addBeanRule(beanRule);
	}

	/**
	 * Add translet rule.
	 *
	 * @param transletRule the translet rule
	 */
	public void addTransletRule(TransletRule transletRule) {
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
	 * Gets all aspect rules.
	 *
	 * @return the aspect rules
	 */
	public Collection<AspectRule> getAspectRules() {
		return aspectRuleRegistry.getAspectRuleMap().values();
	}

	/**
	 * Gets all bean rules.
	 *
	 * @return the bean rules
	 */
	public Collection<BeanRule> getBeanRules() {
		Set<BeanRule> beanRuleSet = new HashSet<BeanRule>();
		beanRuleSet.addAll(beanRuleRegistry.getIdBasedBeanRuleMap().values());
		for(Set<BeanRule> brs : beanRuleRegistry.getTypeBasedBeanRuleMap().values()) {
			beanRuleSet.addAll(brs);
		}
		beanRuleSet.addAll(beanRuleRegistry.getConfigBeanRuleMap().values());
		return beanRuleSet;
	}

	/**
	 * Gets all template rules.
	 *
	 * @return the template rules
	 */
	public Collection<TemplateRule> getTemplateRules() {
		return templateRuleRegistry.getTemplateRuleMap().values();
	}

	/**
	 * Gets all translet rules.
	 *
	 * @return the translet rules
	 */
	public Collection<TransletRule> getTransletRules() {
		return transletRuleRegistry.getTransletRuleMap().values();
	}

	public ImportHandler getImportHandler() {
		return importHandler;
	}

	public void setImportHandler(ImportHandler importHandler) {
		this.importHandler = importHandler;
	}

	public Importer newImporter(String file, String resource, String url, String fileType, String profiles) {
		ImportFileType importFileType = ImportFileType.lookup(fileType);
		Importer importer = null;

		if(StringUtils.hasText(file)) {
			importer = new FileImporter(getApplicationBasePath(), file, importFileType);
		} else if(StringUtils.hasText(resource)) {
			importer = new ResourceImporter(getClassLoader(), resource, importFileType);
		} else if(StringUtils.hasText(url)) {
			importer = new UrlImporter(url, importFileType);
		}
		
		if(profiles != null && !profiles.isEmpty()) {
			String[] arr = ProfilesUtils.split(profiles);
			if(arr != null) {
				importer.setProfiles(arr);
			}
		}

		return importer;
	}

}
