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

import com.aspectran.core.context.loader.resource.AspectranClassLoader;
import com.aspectran.core.context.rule.BeanRule;

public class NonContextBeanRegistryFactory {

	private final ClassLoader classLoader;
	
	private BeanRuleRegistry beanRuleRegistry;

	public NonContextBeanRegistryFactory() {
		this(null);
	}
	
	public NonContextBeanRegistryFactory(ClassLoader classLoader) {
		if(classLoader == null) {
			this.classLoader = AspectranClassLoader.getDefaultClassLoader();
		} else {
			this.classLoader = classLoader;
		}
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public BeanRuleRegistry getBeanRuleRegistry() {
		return beanRuleRegistry;
	}

	public void setBeanRuleRegistry(BeanRuleRegistry beanRuleRegistry) {
		this.beanRuleRegistry = beanRuleRegistry;
	}
	
	public void addBeanRule(BeanRule beanRule) throws ClassNotFoundException {
		if(beanRuleRegistry == null) {
			beanRuleRegistry = new BeanRuleRegistry(classLoader);
		}
		beanRuleRegistry.addBeanRule(beanRule);
	}

	public void addScanPattern(String scanPattern) throws ClassNotFoundException {
		addScanPattern(null, scanPattern, null);
	}
	
	public void addScanPattern(String scanPattern, String maskPattern) throws ClassNotFoundException {
		addScanPattern(null, scanPattern, maskPattern);
	}
	
	public void addScanPattern(String idPattern, String scanPattern, String maskPattern) throws ClassNotFoundException {
		BeanRule beanRule = new BeanRule();
		beanRule.setId(idPattern);
		beanRule.setScanPattern(scanPattern);
		beanRule.setMaskPattern(maskPattern);
		addBeanRule(beanRule);
	}
	
	public NonContextBeanRegistry createNonContextBeanRegistry() {
		if(beanRuleRegistry == null) {
			throw new IllegalArgumentException("'beanRuleRegistry' is not yet created.");
		}
		beanRuleRegistry.postProcess();
		NonContextBeanRegistry beanRegistry = new NonContextBeanRegistry(beanRuleRegistry);
		return beanRegistry;
	}
	
}
