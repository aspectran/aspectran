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
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.bean.scan.BeanClassScanner;
import com.aspectran.core.context.loader.AspectranClassLoader;
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
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class ContextBuilderAssistant {

	private final Logger logger = LoggerFactory.getLogger(ContextBuilderAssistant.class);
	
	private final String applicationBasePath;

	private final ClassLoader classLoader;
	
	private ArrayStack objectStack = new ArrayStack();
	
	private Map<String, String> typeAliases = new HashMap<String, String>();
	
	private Map<DefaultSettingType, String> settings = new HashMap<DefaultSettingType, String>();
	
	private DefaultSettings defaultSettings;

	private DefaultSettings previousDefaultSettings;
	
	private BeanReferenceInspector beanReferenceInspector = new BeanReferenceInspector();
	
	protected AspectRuleMap aspectRuleMap = new AspectRuleMap();
	
	protected BeanRuleMap beanRuleMap = new BeanRuleMap();
	
	protected TransletRuleMap transletRuleMap = new TransletRuleMap();
	
	private ImportHandler importHandler;
	
	public ContextBuilderAssistant(String applicationBasePath, ClassLoader classLoader) {
		if(applicationBasePath == null)
			this.applicationBasePath = new File(".").getAbsoluteFile().toString();
		else
			this.applicationBasePath = applicationBasePath;
		
		if(classLoader == null)
			this.classLoader = new AspectranClassLoader();
		else
			this.classLoader = classLoader;
		
		logger.info("Application base directory path is [" + applicationBasePath + "]");
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
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
	
	public String getApplicationBasePath() {
		return applicationBasePath;
	}

	/**
	 * To real path.
	 * 
	 * @param filePath the file path
	 * 
	 * @return the file
	 * @throws IOException 
	 */
	public String toRealPath(String filePath) throws IOException {
		File file = toRealPathAsFile(filePath);
		return file.getCanonicalPath();
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
		
		if(filePath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
			URI uri = URI.create(filePath);
			file = new File(uri);
		} else if(filePath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			file = new File(getClassLoader().getResource(filePath).getFile());
		} else {
			if(applicationBasePath != null)
				file = new File(applicationBasePath, filePath);
			else
				file = new File(filePath);
		}
		
		return file;
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
	
	public void applySettings() throws ClassNotFoundException {
		if(defaultSettings == null)
			defaultSettings = new DefaultSettings(classLoader);

		defaultSettings.set(getSettings());
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
		if(defaultSettings == null)
			return transletName;

		if(transletName != null && transletName.length() > 0 && transletName.charAt(0) == AspectranConstant.TRANSLET_NAME_SEPARATOR)
			return transletName;

		if(defaultSettings.getTransletNamePatternPrefix() == null && 
				defaultSettings.getTransletNamePatternSuffix() == null)
			return transletName;
		
		StringBuilder sb = new StringBuilder();
		
		if(defaultSettings.getTransletNamePatternPrefix() != null)
			sb.append(defaultSettings.getTransletNamePatternPrefix());

		sb.append(transletName);
		
		if(defaultSettings.getTransletNamePatternSuffix() != null)
			sb.append(defaultSettings.getTransletNamePatternSuffix());
		
		return sb.toString();
	}
/*	
	public String applyNamespaceForBean(String beanId) {
		if(!defaultSettings.isUseNamespaces() || namespace == null)
			return beanId;
		
		StringBuilder sb = new StringBuilder();
		sb.append(namespace);
		sb.append(AspectranConstant.ID_SEPARATOR);
		sb.append(beanId);
		
		return sb.toString();
	}
*/
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

	public DefaultSettings getDefaultSettings() {
		return defaultSettings;
	}

	public void setDefaultSettings(DefaultSettings defaultSettings) {
		this.defaultSettings = defaultSettings;
	}
	
	public void backupDefaultSettings() throws CloneNotSupportedException {
		previousDefaultSettings = defaultSettings;
		
		if(defaultSettings != null)
			defaultSettings = defaultSettings.clone();
	}

	public void restoreDefaultSettings() {
		defaultSettings = previousDefaultSettings;
		previousDefaultSettings = null;
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
		
		if(!WildcardPattern.hasWildcards(className)) {
			Class<?> beanClass = classLoader.loadClass(className);
			beanRule.setBeanClass(beanClass);
			BeanRule.checkAccessibleMethod(beanRule);
			beanRuleMap.putBeanRule(beanRule);
			if(logger.isTraceEnabled())
				logger.trace("add BeanRule " + beanRule);
		} else {
			BeanClassScanner scanner = new BeanClassScanner(beanRule.getId(), classLoader);
			Map<String, Class<?>> beanClassMap = scanner.scanClass(className);
			
			if(beanClassMap != null && !beanClassMap.isEmpty()) {
				for(Map.Entry<String, Class<?>> entry : beanClassMap.entrySet()) {
					BeanRule beanRule2 = beanRule.clone();
					
					String beanId = entry.getKey();
					Class<?> beanClass = entry.getValue();
					
					beanRule2.setId(beanId);
					beanRule2.setClassName(beanClass.getName());
					beanRule2.setBeanClass(beanClass);
					beanRule2.setStealthily(true);

					BeanRule.checkAccessibleMethod(beanRule2);
					
					beanRuleMap.putBeanRule(beanRule2);
					if(logger.isTraceEnabled())
						logger.trace("add BeanRule " + beanRule2);
				}
			}
		}
	}
	
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleMap;
	}
	
	public void addAspectRule(AspectRule aspectRule) {
		aspectRuleMap.putAspectRule(aspectRule);
		
		if(logger.isTraceEnabled())
			logger.trace("add AspectRule " + aspectRule);
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
		
		if(transletRule.getResponseRule() == null) {
			ResponseRule responseRule = new ResponseRule();
			transletRule.setResponseRule(responseRule);
		}
		
		List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
		
		if(responseRuleList == null || responseRuleList.size() == 0) {
			transletRule.setName(applyTransletNamePattern(transletRule.getName()));
			
			transletRuleMap.put(transletRule.getName(), transletRule);

			if(logger.isTraceEnabled())
				logger.trace("add TransletRule " + transletRule);
		} else if(responseRuleList.size() == 1) {
			transletRule.setResponseRule(responseRuleList.get(0));
			transletRule.setResponseRuleList(null);
			transletRule.setName(applyTransletNamePattern(transletRule.getName()));
			
			transletRuleMap.put(transletRule.getName(), transletRule);

			if(logger.isTraceEnabled())
				logger.trace("add TransletRule " + transletRule);
		} else if(responseRuleList.size() > 1) {
			ResponseRule defaultResponseRule = null;
			
			for(ResponseRule responseRule : responseRuleList) {
				String responseName = responseRule.getName();
				
				if(responseName == null || responseName.length() == 0) {
					defaultResponseRule = responseRule;
				} else {
					TransletRule subTransletRule = transletRule.newSubTransletRule(responseRule);
					subTransletRule.setName(applyTransletNamePattern(subTransletRule.getName()));
					
					transletRuleMap.put(subTransletRule.getName(), subTransletRule);
					
					if(logger.isTraceEnabled())
						logger.trace("add sub TransletRule " + subTransletRule);
				}
			}
			
			if(defaultResponseRule != null) {
				transletRule.setResponseRule(defaultResponseRule);
				transletRule.setName(applyTransletNamePattern(transletRule.getName()));
				responseRuleList.remove(defaultResponseRule);
				transletRule.setResponseRuleList(responseRuleList);
				
				transletRuleMap.put(transletRule.getName(), transletRule);
				
				if(logger.isTraceEnabled())
					logger.trace("add TransletRule " + transletRule);
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
