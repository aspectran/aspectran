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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.bean.scan.BeanClassScanner;
import com.aspectran.core.context.rule.*;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.template.scan.TemplateFileScanner;
import com.aspectran.core.util.ArrayStack;
import com.aspectran.core.util.PrefixSuffixPattern;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class ContextBuilderAssistant
 * 
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class ContextBuilderAssistant {

	private final Log log = LogFactory.getLog(ContextBuilderAssistant.class);
	
	private ApplicationAdapter applicationAdapter;
	
	private String applicationBasePath;

	private ClassLoader classLoader;
	
	private ArrayStack objectStack = new ArrayStack();
	
	private AssistantLocal assistantLocal = new AssistantLocal();
	
	private Map<String, String> typeAliases = new HashMap<String, String>();
	
	private Map<DefaultSettingType, String> settings = new HashMap<DefaultSettingType, String>();
	
	private BeanReferenceInspector beanReferenceInspector = new BeanReferenceInspector();
	
	protected AspectRuleMap aspectRuleMap = new AspectRuleMap();
	
	protected BeanRuleMap beanRuleMap = new BeanRuleMap();

	protected TemplateRuleMap templateRuleMap = new TemplateRuleMap();

	protected TransletRuleMap transletRuleMap = new TransletRuleMap();
	
	private ImportHandler importHandler;
	
	private boolean hybridLoading;
	
	public ContextBuilderAssistant() {
	}
	
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
		this.applicationBasePath = applicationAdapter.getApplicationBasePath();
		this.classLoader = applicationAdapter.getClassLoader();
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
	@SuppressWarnings("unchecked")
	public void applySettings() throws ClassNotFoundException {
		DefaultSettings defaultSettings = assistantLocal.touchDefaultSettings();

		defaultSettings.apply(getSettings());

		if(classLoader != null) {
			if(defaultSettings.getTransletInterfaceClassName() != null) {
				Class<?> transletInterfaceClass = classLoader.loadClass(defaultSettings.getTransletInterfaceClassName());
				defaultSettings.setTransletInterfaceClass((Class<Translet>)transletInterfaceClass);
			}
			if(defaultSettings.getTransletImplementClassName() != null) {
				Class<?> transletImplementClass = classLoader.loadClass(defaultSettings.getTransletImplementClassName());
				defaultSettings.setTransletImplementClass((Class<CoreTranslet>)transletImplementClass);
			}
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
	}

	/**
	 * Backup assistant local.
	 *
	 * @return the assistant local
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public AssistantLocal backupAssistantLocal() throws CloneNotSupportedException {
		AssistantLocal oldAssistantLocal = assistantLocal;
		assistantLocal = assistantLocal.clone();
		return oldAssistantLocal;
	}

	/**
	 * Restore assistant local.
	 *
	 * @param assistantLocal the assistant local
	 */
	public void restoreAssistantLocal(AssistantLocal assistantLocal) {
		this.assistantLocal = assistantLocal;
	}
	
	/**
	 * Returns the trnaslet name of the prefix and suffix are combined.
	 * 
	 * @param transletName the translet name
	 * 
	 * @return the string
	 */
	public String applyTransletNamePattern(String transletName) {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		
		if(defaultSettings == null)
			return transletName;

		if(transletName != null && transletName.length() > 0 && transletName.charAt(0) == AspectranConstant.TRANSLET_NAME_SEPARATOR)
			return transletName;

		if(defaultSettings.getTransletNamePrefix() == null && 
				defaultSettings.getTransletNameSuffix() == null)
			return transletName;
		
		StringBuilder sb = new StringBuilder();
		
		if(defaultSettings.getTransletNamePrefix() != null)
			sb.append(defaultSettings.getTransletNamePrefix());

		sb.append(transletName);
		
		if(defaultSettings.getTransletNameSuffix() != null)
			sb.append(defaultSettings.getTransletNameSuffix());
		
		return sb.toString();
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
	 * Gets the aspect rule map.
	 *
	 * @return the aspect rule map
	 */
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleMap;
	}
	
	/**
	 * Adds the aspect rule.
	 *
	 * @param aspectRule the aspect rule
	 */
	public void addAspectRule(AspectRule aspectRule) {
		aspectRuleMap.putAspectRule(aspectRule);
		
		if(log.isTraceEnabled())
			log.trace("add AspectRule " + aspectRule);
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
	 * Adds the bean rule.
	 *
	 * @param beanRule the bean rule
	 * @throws CloneNotSupportedException the clone not supported exception
	 * @throws ClassNotFoundException the class not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addBeanRule(BeanRule beanRule) throws CloneNotSupportedException, ClassNotFoundException, IOException {
		String className = beanRule.getClassName();

		PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern();
		boolean patterned = prefixSuffixPattern.split(beanRule.getId());
		
		if(WildcardPattern.hasWildcards(className)) {
			BeanClassScanner scanner = new BeanClassScanner(classLoader);
			if(beanRule.getFilterParameters() != null)
				scanner.setFilterParameters(beanRule.getFilterParameters());
			if(beanRule.getMaskPattern() != null)
				scanner.setBeanIdMaskPattern(beanRule.getMaskPattern());
			
			Map<String, Class<?>> beanClassMap = scanner.scanClasses(className);
			
			if(beanClassMap != null && !beanClassMap.isEmpty()) {
				for(Map.Entry<String, Class<?>> entry : beanClassMap.entrySet()) {
					BeanRule beanRule2 = beanRule.clone();
					
					String beanId = entry.getKey();
					Class<?> beanClass = entry.getValue();
			
					if(patterned) {
						beanRule2.setId(prefixSuffixPattern.join(beanId));
					} else {
						if(beanRule.getId() != null) {
							beanRule2.setId(beanRule.getId() + beanId);
						}
					}

					beanRule2.setClassName(beanClass.getName());
					beanRule2.setBeanClass(beanClass);
					beanRule2.setScanned(true);
					BeanRule.checkFactoryBeanImplement(beanRule2);
					BeanRule.checkAccessibleMethod(beanRule2);
					beanRuleMap.putBeanRule(beanRule2);
					
					if(log.isTraceEnabled())
						log.trace("add BeanRule " + beanRule2);
				}
			}
			
			if(log.isDebugEnabled())
				log.debug("scanned class files: " + (beanClassMap == null ? 0 : beanClassMap.size()));
		} else {
			if(patterned) {
				beanRule.setId(prefixSuffixPattern.join(className));
			}
			
			Class<?> beanClass = classLoader.loadClass(className);
			beanRule.setBeanClass(beanClass);
			BeanRule.checkFactoryBeanImplement(beanRule);
			BeanRule.checkAccessibleMethod(beanRule);
			beanRuleMap.putBeanRule(beanRule);
			
			if(log.isTraceEnabled())
				log.trace("add BeanRule " + beanRule);
		}
	}

	public TemplateRuleMap getTemplateRuleMap() {
		return templateRuleMap;
	}

	public void addTemplateRule(TemplateRule templateRule) {
		templateRuleMap.putTemplateRule(templateRule);
	}

	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

	public void addTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		if(defaultSettings != null) {
			transletRule.setTransletInterfaceClass(defaultSettings.getTransletInterfaceClass());
			transletRule.setTransletImplementClass(defaultSettings.getTransletImplementClass());
		}
		
		if(transletRule.getPath() != null) {
			TemplateFileScanner scanner = new TemplateFileScanner(applicationBasePath, classLoader);
			if(transletRule.getFilterParameters() != null)
				scanner.setFilterParameters(transletRule.getFilterParameters());
			if(transletRule.getMaskPattern() != null)
				scanner.setTransletNameMaskPattern(transletRule.getMaskPattern());
			else
				scanner.setTransletNameMaskPattern(transletRule.getPath());
			
			Map<String, File> templateFileMap = scanner.scanFiles(transletRule.getPath());
			
			if(templateFileMap != null && !templateFileMap.isEmpty()) {
				PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern();
				boolean patterned = prefixSuffixPattern.split(transletRule.getName());

				for(Map.Entry<String, File> entry : templateFileMap.entrySet()) {
					String filePath = entry.getKey();
					TransletRule newTransletRule = TransletRule.newDerivedTransletRule(transletRule, filePath);
					
					if(patterned) {
						newTransletRule.setName(prefixSuffixPattern.join(filePath));
					} else {
						if(transletRule.getName() != null) {
							newTransletRule.setName(transletRule.getName() + filePath);
						}
					}
					
					putTransletRule(newTransletRule);
				}
			}
		} else {
			putTransletRule(transletRule);
		}
	}
	
	private void putTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		if(transletRule.getRequestRule() == null) {
			RequestRule requestRule = new RequestRule();
			transletRule.setRequestRule(requestRule);
		}
		
		List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
		
		if(responseRuleList == null || responseRuleList.isEmpty()) {
			transletRule.determineResponseRule();
			transletRule.setName(applyTransletNamePattern(transletRule.getName()));
			transletRuleMap.putTransletRule(transletRule);
			if(log.isTraceEnabled())
				log.trace("add TransletRule " + transletRule);
		} else if(responseRuleList.size() == 1) {
			transletRule.setResponseRule(responseRuleList.get(0));
			transletRule.determineResponseRule();
			transletRule.setName(applyTransletNamePattern(transletRule.getName()));
			transletRuleMap.putTransletRule(transletRule);

			if(log.isTraceEnabled())
				log.trace("add TransletRule " + transletRule);
		} else {
			ResponseRule defaultResponseRule = null;
			
			for(ResponseRule responseRule : responseRuleList) {
				String responseName = responseRule.getName();
				
				if(responseName == null || responseName.length() == 0) {
					if(defaultResponseRule != null) {
						log.warn("ignore duplicated default response rule " + defaultResponseRule + " of transletRule " + transletRule);
					}
					defaultResponseRule = responseRule;
				} else {
					TransletRule subTransletRule = TransletRule.newSubTransletRule(transletRule, responseRule);
					subTransletRule.determineResponseRule();
					subTransletRule.setName(applyTransletNamePattern(subTransletRule.getName()));
					transletRuleMap.putTransletRule(subTransletRule);
					
					if(log.isTraceEnabled())
						log.trace("add sub TransletRule " + subTransletRule);
				}
			}
			
			if(defaultResponseRule != null) {
				transletRule.setResponseRule(defaultResponseRule);
				transletRule.determineResponseRule();
				transletRule.setName(applyTransletNamePattern(transletRule.getName()));
				transletRuleMap.putTransletRule(transletRule);
				
				if(log.isTraceEnabled())
					log.trace("add TransletRule " + transletRule);
			}
		}
	}
	
	public String putBeanReference(String beanId, Object rule) {
		if(!beanRuleMap.containsKey(beanId)) {
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
