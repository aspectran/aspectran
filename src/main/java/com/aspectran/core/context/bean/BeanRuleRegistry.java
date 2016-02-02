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
package com.aspectran.core.context.bean;

import java.io.IOException;
import java.util.*;

import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.context.bean.scan.BeanClassScanner;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.util.PrefixSuffixPattern;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The type Bean rule registry.
 *
 * @since 2.0.0
 */
public class BeanRuleRegistry {
	
	private final Log log = LogFactory.getLog(BeanRuleRegistry.class);

	private final ClassLoader classLoader;
	
	private final BeanRuleMap beanRuleMap = new BeanRuleMap();

	private final Map<Class<?>, BeanRule> classBeanRuleMap = new HashMap<Class<?>, BeanRule>();

	private final Map<Class<?>, BeanRule> interfaceBeanRuleMap = new HashMap<Class<?>, BeanRule>();

	private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<Class<?>>();

	public BeanRuleRegistry() {
		this(AspectranClassLoader.getDefaultClassLoader());
	}
	
	public BeanRuleRegistry(ClassLoader classLoader) {
		this.classLoader = classLoader;

		ignoreDependencyInterface(DisposableBean.class);
		ignoreDependencyInterface(FactoryBean.class);
		ignoreDependencyInterface(InitializableBean.class);
		ignoreDependencyInterface(InitializableTransletBean.class);
	}

	public BeanRule getBeanRule(String beanId) {
		return beanRuleMap.get(beanId);
	}
	
	public BeanRule getBeanRule(Class<?> requiredType) {
		if(requiredType.isInterface())
			return interfaceBeanRuleMap.get(requiredType);

		return classBeanRuleMap.get(requiredType);
	}
	
	public boolean contains(String beanId) {
		return beanRuleMap.containsKey(beanId);
	}
	
	public boolean contains(Class<?> requiredType) {
		if(requiredType.isInterface())
			return interfaceBeanRuleMap.containsKey(requiredType);

		return classBeanRuleMap.containsKey(requiredType);
	}

	public Collection<BeanRule> getBeanRules() {
		return beanRuleMap.values();
	}

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
		String scanPath = beanRule.getScanPath();

		PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern();
		boolean splited = prefixSuffixPattern.split(beanRule.getId());
		
		if(scanPath != null) {
			BeanClassScanner scanner = new BeanClassScanner(classLoader);
			if(beanRule.getFilterParameters() != null)
				scanner.setFilterParameters(beanRule.getFilterParameters());
			if(beanRule.getMaskPattern() != null)
				scanner.setBeanIdMaskPattern(beanRule.getMaskPattern());
			
			Map<String, Class<?>> beanClassMap = scanner.scanClasses(scanPath);
			
			if(beanClassMap != null && !beanClassMap.isEmpty()) {
				for(Map.Entry<String, Class<?>> entry : beanClassMap.entrySet()) {
					BeanRule beanRule2 = beanRule.clone();
					
					String beanId = entry.getKey();
					Class<?> beanClass = entry.getValue();
			
					if(splited) {
						beanRule2.setId(prefixSuffixPattern.join(beanId));
					} else {
						if(beanRule.getId() != null) {
							beanRule2.setId(beanRule.getId() + beanId);
						}
					}

					beanRule2.setBeanClass(beanClass);
					beanRule2.setScanned(true);
					BeanRule.checkFactoryBeanImplement(beanRule2);
					BeanRule.checkAccessibleMethod(beanRule2);
					beanRuleMap.putBeanRule(beanRule2);

					addClassBeanRule(beanRule2);

					if(log.isTraceEnabled())
						log.trace("add BeanRule " + beanRule2);
				}
			}
			
			if(log.isDebugEnabled())
				log.debug("scanned class files: " + (beanClassMap == null ? 0 : beanClassMap.size()));
		} else {
			String className = beanRule.getClassName();

			if(splited) {
				beanRule.setId(prefixSuffixPattern.join(className));
			}
			
			Class<?> beanClass = classLoader.loadClass(className);
			beanRule.setBeanClass(beanClass);
			BeanRule.checkFactoryBeanImplement(beanRule);
			BeanRule.checkAccessibleMethod(beanRule);
			beanRuleMap.putBeanRule(beanRule);

			addClassBeanRule(beanRule);

			if(log.isTraceEnabled())
				log.trace("add BeanRule " + beanRule);
		}
	}

	private void addClassBeanRule(BeanRule beanRule) {
		Class<?> beanClass = beanRule.getBeanClass();

		classBeanRuleMap.put(beanClass, beanRule);

		for(Class<?> ifc : beanClass.getInterfaces()) {
			if(!ignoredDependencyInterfaces.contains(ifc)) {
				interfaceBeanRuleMap.put(ifc, beanRule);
			}
		}
	}

	public void ignoreDependencyInterface(Class<?> ifc) {
		this.ignoredDependencyInterfaces.add(ifc);
	}

	public void clear() {
		beanRuleMap.clear();
		classBeanRuleMap.clear();
		interfaceBeanRuleMap.clear();
	}
	
}
