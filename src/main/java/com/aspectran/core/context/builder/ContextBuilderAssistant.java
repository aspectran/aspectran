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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.bean.scan.BeanClassScanner;
import com.aspectran.core.context.bean.scan.ClassScanner;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.ArrayStack;
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
	
	private Map<String, String> typeAliases = new HashMap<String, String>();
	
	private Map<DefaultSettingType, String> settings = new HashMap<DefaultSettingType, String>();
	
	private DefaultSettings defaultSettings;

	private BeanReferenceInspector beanReferenceInspector = new BeanReferenceInspector();
	
	protected AspectRuleMap aspectRuleMap = new AspectRuleMap();
	
	protected BeanRuleMap beanRuleMap = new BeanRuleMap();
	
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
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public <T> T popObject() {
		return (T)objectStack.pop();
	}
	
	/**
	 * Peek object.
	 * 
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public <T> T peekObject() {
		return (T)objectStack.peek();
	}
	
	/**
	 * Peek object.
	 * 
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

	public Map<DefaultSettingType, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<DefaultSettingType, String> settings) throws ClassNotFoundException {
		this.settings = settings;
		applySettings();
	}

	public void putSetting(DefaultSettingType settingType, String value) {
		settings.put(settingType, value);
	}
	
	public Object getSetting(DefaultSettingType settingType) {
		return settings.get(settingType);
	}
	
	@SuppressWarnings("unchecked")
	public void applySettings() throws ClassNotFoundException {
		if(defaultSettings == null)
			defaultSettings = new DefaultSettings();

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
	
	public DefaultSettings getDefaultSettings() {
		return defaultSettings;
	}

	public void setDefaultSettings(DefaultSettings defaultSettings) {
		this.defaultSettings = defaultSettings;
	}
	
	public DefaultSettings backupDefaultSettings() throws CloneNotSupportedException {
		DefaultSettings previousDefaultSettings = defaultSettings;
		
		if(defaultSettings != null)
			defaultSettings = defaultSettings.clone();
		
		return previousDefaultSettings;
	}

	public void restoreDefaultSettings(DefaultSettings defaultSettings) {
		setDefaultSettings(defaultSettings);
	}
	
	/**
	 * Returns the trnaslet name of the prefix and suffix are combined.
	 * 
	 * @param transletName the translet name
	 * 
	 * @return the string
	 */
	public String applyTransletNamePattern(String transletName) {
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
		if(defaultSettings == null)
			return true;
		
		return defaultSettings.isPointcutPatternVerifiable();
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
	 * @throws CloneNotSupportedException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void addBeanRule(BeanRule beanRule) throws CloneNotSupportedException, ClassNotFoundException, IOException {
		String className = beanRule.getClassName();
		
		if(WildcardPattern.hasWildcards(className)) {
			ClassScanner scanner = new BeanClassScanner(classLoader);
			if(beanRule.getFilterParameters() != null)
				scanner.setFilterParameters(beanRule.getFilterParameters());
			
			Map<String, Class<?>> beanClassMap = scanner.scanClasses(className);
			
			if(beanClassMap != null && !beanClassMap.isEmpty()) {
				for(Map.Entry<String, Class<?>> entry : beanClassMap.entrySet()) {
					BeanRule beanRule2 = beanRule.clone();
					
					String beanId = entry.getKey();
					Class<?> beanClass = entry.getValue();
			
					if(beanRule.isPatternedBeanId()) {
						beanRule2.setId(beanId, beanRule.getIdPrefix(), beanRule.getIdSuffix());
						beanRule2.setIdPrefix(null);
						beanRule2.setIdSuffix(null);
					} else {
						if(beanRule.getId() != null) {
							beanRule2.setId(beanId, beanRule.getId(), null);
						}
					}
						
					beanRule2.setClassName(beanClass.getName());
					beanRule2.setBeanClass(beanClass);
					beanRule2.setScanned(true);

					BeanRule.checkAccessibleMethod(beanRule2);
					
					beanRuleMap.putBeanRule(beanRule2);
					
					if(log.isTraceEnabled())
						log.trace("add BeanRule " + beanRule2);
				}
			}
			
			if(log.isDebugEnabled())
				log.debug("scanned class files: " + (beanClassMap == null ? 0 : beanClassMap.size()));
		} else {
			if(beanRule.isPatternedBeanId()) {
				beanRule.setId(className, beanRule.getIdPrefix(), beanRule.getIdSuffix());
				beanRule.setIdPrefix(null);
				beanRule.setIdSuffix(null);
			}
			
			Class<?> beanClass = classLoader.loadClass(className);
			beanRule.setBeanClass(beanClass);
			BeanRule.checkAccessibleMethod(beanRule);
			beanRuleMap.putBeanRule(beanRule);
			
			if(log.isTraceEnabled())
				log.trace("add BeanRule " + beanRule);
		}
	}
	
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleMap;
	}
	
	public void addAspectRule(AspectRule aspectRule) {
		aspectRuleMap.putAspectRule(aspectRule);
		
		if(log.isTraceEnabled())
			log.trace("add AspectRule " + aspectRule);
	}

	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

	public void addTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		if(defaultSettings != null) {
			transletRule.setTransletInterfaceClass(defaultSettings.getTransletInterfaceClass());
			transletRule.setTransletImplementClass(defaultSettings.getTransletImplementClass());
		}
		
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
