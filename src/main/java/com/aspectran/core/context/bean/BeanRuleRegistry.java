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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.context.bean.scan.BeanClassScanner;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.PrefixSuffixPattern;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class BeanRuleRegistry.
 *
 * @since 2.0.0
 */
public class BeanRuleRegistry {
	
	private final Log log = LogFactory.getLog(BeanRuleRegistry.class);

	private final ClassLoader classLoader;
	
	private final BeanRuleMap beanRuleMap = new BeanRuleMap();

	private final Map<Class<?>, List<BeanRule>> typeBeanRuleMap = new HashMap<Class<?>, List<BeanRule>>();

	private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<Class<?>>();
	
	private TransletRuleRegistry transletRuleRegistry;

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
	
	public TransletRuleRegistry getTransletRuleRegistry() {
		return transletRuleRegistry;
	}

	public void setTransletRuleRegistry(TransletRuleRegistry transletRuleRegistry) {
		this.transletRuleRegistry = transletRuleRegistry;
	}

	public BeanRule getBeanRule(String beanId) {
		return beanRuleMap.get(beanId);
	}
	
	public BeanRule[] getBeanRule(Class<?> requiredType) {
		List<BeanRule> list = typeBeanRuleMap.get(requiredType);
		if(list.isEmpty())
			return null;
		
		return list.toArray(new BeanRule[list.size()]);
	}
	
	public boolean contains(String beanId) {
		return beanRuleMap.containsKey(beanId);
	}
	
	public boolean contains(Class<?> requiredType) {
		return typeBeanRuleMap.containsKey(requiredType);
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
	 * @throws ClassNotFoundException thrown when the bean class is not found.
	 */
	public void addBeanRule(BeanRule beanRule) throws ClassNotFoundException {
		PrefixSuffixPattern prefixSuffixPattern = PrefixSuffixPattern.parse(beanRule.getId());
		String scanPath = beanRule.getScanPath();

		if(scanPath != null) {
			BeanClassScanner scanner = new BeanClassScanner(classLoader);
			if(beanRule.getFilterParameters() != null)
				scanner.setFilterParameters(beanRule.getFilterParameters());
			if(beanRule.getMaskPattern() != null)
				scanner.setBeanIdMaskPattern(beanRule.getMaskPattern());
			
			Map<String, Class<?>> beanClassMap = scanner.scanClasses(scanPath);
			
			if(beanClassMap != null && !beanClassMap.isEmpty()) {
				for(Map.Entry<String, Class<?>> entry : beanClassMap.entrySet()) {
					BeanRule beanRule2 = beanRule.replicate();
					
					String beanId = entry.getKey();
					Class<?> beanClass = entry.getValue();
			
					if(prefixSuffixPattern != null) {
						beanRule2.setId(prefixSuffixPattern.join(beanId));
					} else {
						if(beanRule.getId() != null) {
							beanRule2.setId(beanRule.getId() + beanId);
						}
					}

					beanRule2.setBeanClass(beanClass);
					putBeanRule(beanRule2);
				}
			}
			
			if(log.isDebugEnabled())
				log.debug("Scanned class files: " + (beanClassMap == null ? 0 : beanClassMap.size()));
		} else {
			String className = beanRule.getClassName();

			if(prefixSuffixPattern != null) {
				beanRule.setId(prefixSuffixPattern.join(className));
			}
			
			Class<?> beanClass = classLoader.loadClass(className);
			beanRule.setBeanClass(beanClass);
			putBeanRule(beanRule);
		}
	}

	private void putBeanRule(BeanRule beanRule) {
		BeanRule.checkAccessibleMethod(beanRule);

		if(beanRule.getId() != null)
			beanRuleMap.putBeanRule(beanRule);
		
		Class<?> beanClass = beanRule.getBeanClass();
		putBeanRule(beanClass, beanRule);
		
		for(Class<?> ifc : beanClass.getInterfaces()) {
			if(!ignoredDependencyInterfaces.contains(ifc)) {
				putBeanRule(ifc, beanRule);
			}
		}

		if(log.isTraceEnabled())
			log.trace("add BeanRule " + beanRule);

		parseAnnotation(beanRule);
	}
	
	private void putBeanRule(Class<?> type, BeanRule beanRule) {
		List<BeanRule> list = typeBeanRuleMap.get(type);
		if(list == null) {
			list = new ArrayList<BeanRule>();
			typeBeanRuleMap.put(type, list);
		}
		list.add(beanRule);
	}

	private void parseAnnotation(BeanRule beanRule) {
		if(transletRuleRegistry != null) {
			AnnotatedTransletParser.parse(beanRule, transletRuleRegistry.getTransletRuleMap());
		}
	}

	public void ignoreDependencyInterface(Class<?> ifc) {
		this.ignoredDependencyInterfaces.add(ifc);
	}

	public void clear() {
		beanRuleMap.clear();
		typeBeanRuleMap.clear();
	}
	
}
