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
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.context.bean.annotation.Configuration;
import com.aspectran.core.context.bean.scan.BeanClassScanFailedException;
import com.aspectran.core.context.bean.scan.BeanClassScanner;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ClassScanner;
import com.aspectran.core.util.MethodUtils;
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

	private final Map<Class<?>, Set<BeanRule>> typeBeanRuleMap = new HashMap<Class<?>, Set<BeanRule>>();

	private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<Class<?>>();

	private final Set<BeanRule> postProcessBeanRuleMap = new HashSet<BeanRule>();

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
		Set<BeanRule> list = typeBeanRuleMap.get(requiredType);
		if(list.isEmpty())
			return null;
		
		return list.toArray(new BeanRule[list.size()]);
	}

	public boolean contains(Object beanIdOrClass) {
		if(beanIdOrClass == null)
			return false;

		if(beanIdOrClass instanceof Class<?>)
			return contains((Class<?>)beanIdOrClass);
		else
			return contains(beanIdOrClass.toString());
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
	public void addBeanRule(final BeanRule beanRule) throws ClassNotFoundException {
		final PrefixSuffixPattern prefixSuffixPattern = PrefixSuffixPattern.parse(beanRule.getId());
		String scanPath = beanRule.getScanPath();

		if(scanPath != null) {
			BeanClassScanner scanner = new BeanClassScanner(classLoader);
			if(beanRule.getFilterParameters() != null)
				scanner.setFilterParameters(beanRule.getFilterParameters());
			if(beanRule.getMaskPattern() != null)
				scanner.setBeanIdMaskPattern(beanRule.getMaskPattern());

			try {
				scanner.scan(scanPath, new ClassScanner.SaveHandler() {
					@Override
					public void save(String resourceName, Class<?> scannedClass) {
						if(scannedClass.isAnnotationPresent(Configuration.class)) {

						} else {
							BeanRule beanRule2 = beanRule.replicate();

							if(prefixSuffixPattern != null) {
								beanRule2.setId(prefixSuffixPattern.join(resourceName));
							} else {
								if(beanRule.getId() != null) {
									beanRule2.setId(beanRule.getId() + resourceName);
								}
							}

							beanRule2.setBeanClass(scannedClass);
							putBeanRule(beanRule2);
						}
					}
				});
			} catch(IOException e) {
				throw new BeanClassScanFailedException("Failed to scan bean class. scanPath: " + scanPath, e);
			}
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

	private Class<?> resolveOfferBeanClass(BeanRule beanRule) {
		Class<?> offerBeanClass = null;
		
		if(beanRule.isOffered()) {
			if(beanRule.getOfferBeanClass() == null) {
				BeanRule offerBeanRule = beanRuleMap.get(beanRule.getOfferBeanId());
				if(offerBeanRule != null) {
					offerBeanClass = resolveOfferBeanClass(offerBeanRule);
				}
			} else {
				offerBeanClass = beanRule.getOfferBeanClass();
			}
		} else {
			offerBeanClass = beanRule.getBeanClass();
		}

		if(offerBeanClass == null)
			throw new BeanRuleException("Cannot resolve offer bean of ", beanRule);
		
		return offerBeanClass;
	}
	
	private void putBeanRule(BeanRule beanRule) {
		Class<?> beanClass = determineBeanClass(beanRule);

		if(beanClass == null) {
			postProcessBeanRuleMap.add(beanRule);
		}

		if(beanRule.getId() != null)
			beanRuleMap.putBeanRule(beanRule);

		if(beanClass != null && !beanRule.isOffered()) {
			putBeanRule(beanClass, beanRule);

			for(Class<?> ifc : beanClass.getInterfaces()) {
				if(!ignoredDependencyInterfaces.contains(ifc)) {
					putBeanRule(ifc, beanRule);
				}
			}
		}

		if(log.isTraceEnabled())
			log.trace("add BeanRule " + beanRule);
	}
	
	private void putBeanRule(Class<?> type, BeanRule beanRule) {
		Set<BeanRule> list = typeBeanRuleMap.get(type);
		if(list == null) {
			list = new HashSet<BeanRule>();
			typeBeanRuleMap.put(type, list);
		}
		list.add(beanRule);
	}

	private void parseAnnotation(BeanRule beanRule) {
		if(transletRuleRegistry != null) {
			AnnotatedConfigParser.parse(beanRule, transletRuleRegistry.getTransletRuleMap());
		}
	}
	
	public void postProcess() {
		if(postProcessBeanRuleMap.isEmpty())
			return;
		
		for(BeanRule beanRule : postProcessBeanRuleMap) {
			if(beanRule.isOffered() && beanRule.getTargetBeanClass() == null) {
				Class<?> offerBeanClass = resolveOfferBeanClass(beanRule);
				Class<?> targetBeanClass = determineOfferMethodTargetBeanClass(offerBeanClass, beanRule.getOfferMethodName(), beanRule);
				
				String initMethodName = beanRule.getInitMethodName();
				if(initMethodName != null) {
					checkInitMethod(targetBeanClass, initMethodName, beanRule);
				}

				String destroyMethodName = beanRule.getDestroyMethodName();
				if(destroyMethodName != null) {
					checkDestroyMethod(targetBeanClass, destroyMethodName, beanRule);
				}

				String factoryMethodName = beanRule.getFactoryMethodName();
				if(factoryMethodName != null) {
					targetBeanClass = determineFactoryMethodTargetBeanClass(targetBeanClass, factoryMethodName, beanRule);
				}
				
				putBeanRule(targetBeanClass, beanRule);

				for(Class<?> ifc : targetBeanClass.getInterfaces()) {
					if(!ignoredDependencyInterfaces.contains(ifc)) {
						putBeanRule(ifc, beanRule);
					}
				}
			}
		}
		
		postProcessBeanRuleMap.clear();
	}

	public void ignoreDependencyInterface(Class<?> ifc) {
		this.ignoredDependencyInterfaces.add(ifc);
	}

	public void clear() {
		beanRuleMap.clear();
		typeBeanRuleMap.clear();
	}

	public static Class<?> determineBeanClass(BeanRule beanRule) {
		Class<?> targetBeanClass;

		if(beanRule.isOffered()) {
			targetBeanClass = beanRule.getOfferBeanClass();
			if(targetBeanClass == null) {
				// will be post processing
				return null;
			}
			targetBeanClass = determineOfferMethodTargetBeanClass(targetBeanClass, beanRule.getOfferMethodName(), beanRule);
		} else {
			targetBeanClass = beanRule.getBeanClass();
		}

		if(targetBeanClass == null)
			throw new BeanRuleException("Invalid BeanRule", beanRule);

		String initMethodName = beanRule.getInitMethodName();
		if(initMethodName != null) {
			checkInitMethod(targetBeanClass, initMethodName, beanRule);
		}

		String destroyMethodName = beanRule.getDestroyMethodName();
		if(destroyMethodName != null) {
			checkDestroyMethod(targetBeanClass, destroyMethodName, beanRule);
		}

		if(beanRule.isFactoryBean()) {
			targetBeanClass = determineTargetBeanClassForFactoryBean(targetBeanClass, beanRule);
		} else {
			String factoryMethodName = beanRule.getFactoryMethodName();
			if(factoryMethodName != null) {
				targetBeanClass = determineFactoryMethodTargetBeanClass(targetBeanClass, factoryMethodName, beanRule);
			}
		}

		return targetBeanClass;
	}

	private static Class<?> determineOfferMethodTargetBeanClass(Class<?> beanClass, String offerMethodName, BeanRule beanRule) {
		Class<?>[] parameterTypes = { Translet.class };
		Method m1 = MethodUtils.getAccessibleMethod(beanClass, offerMethodName, null);
		Method m2 = MethodUtils.getAccessibleMethod(beanClass, offerMethodName, parameterTypes);

		if(m1 == null && m2 == null)
			throw new IllegalArgumentException("No such offer method " + offerMethodName + "() on bean class: " + beanClass);

		Class<?> targetBeanClass;
		
		if(m2 != null) {
			beanRule.setOfferMethodRequiresTranslet(true);
			targetBeanClass = m2.getReturnType();
		} else {
			targetBeanClass = m1.getReturnType();
		}
		
		beanRule.setTargetBeanClass(targetBeanClass);
		
		return targetBeanClass;
	}

	private static Class<?> determineTargetBeanClassForFactoryBean(Class<?> beanClass, BeanRule beanRule) {
		try {
			Method m = MethodUtils.getAccessibleMethod(beanClass, FactoryBean.FACTORY_METHOD_NAME, null);
			Class<?> targetBeanClass = m.getReturnType();
			beanRule.setTargetBeanClass(targetBeanClass);
			return targetBeanClass;
		} catch(Exception e) {
			throw new BeanRuleException("Invalid BeanRule", beanRule);
		}
	}
	
	private static Class<?> determineFactoryMethodTargetBeanClass(Class<?> beanClass, String factoryMethodName, BeanRule beanRule) {
		if(beanRule.isFactoryBean())
			throw new BeanRuleException("Bean factory method is duplicated. Already implemented the FactoryBean", beanRule);
		
		Class<?>[] parameterTypes = { Translet.class };
		Method m1 = MethodUtils.getAccessibleMethod(beanClass, factoryMethodName, null);
		Method m2 = MethodUtils.getAccessibleMethod(beanClass, factoryMethodName, parameterTypes);

		if(m1 == null && m2 == null)
			throw new IllegalArgumentException("No such factory method " + factoryMethodName + "() on bean class: " + beanClass);

		Class<?> targetBeanClass;
		
		if(m2 != null) {
			beanRule.setFactoryMethodRequiresTranslet(true);
			targetBeanClass = m2.getReturnType();
		} else {
			targetBeanClass = m1.getReturnType();
		}
		
		beanRule.setTargetBeanClass(targetBeanClass);
		
		return targetBeanClass;
	}

	private static void checkInitMethod(Class<?> beanClass, String initMethodName, BeanRule beanRule) {
		if(beanRule.isInitializableBean())
			throw new BeanRuleException("Bean initialization method is duplicated. Already implemented the InitializableBean", beanRule);

		if(beanRule.isInitializableTransletBean())
			throw new BeanRuleException("Bean initialization method is duplicated. Already implemented the InitializableTransletBean", beanRule);

		Class<?>[] parameterTypes = { Translet.class };
		Method m1 = MethodUtils.getAccessibleMethod(beanClass, initMethodName, null);
		Method m2 = MethodUtils.getAccessibleMethod(beanClass, initMethodName, parameterTypes);

		if(m1 == null && m2 == null)
			throw new IllegalArgumentException("No such initialization method " + initMethodName + "() on bean class: " + beanClass);

		if(m2 != null) {
			beanRule.setInitMethodRequiresTranslet(true);
		}
	}
	
	private static void checkDestroyMethod(Class<?> beanClass, String destroyMethodName, BeanRule beanRule) {
		if(beanRule.isDisposableBean())
			throw new BeanRuleException("Bean destroy method  is duplicated. Already implemented the DisposableBean", beanRule);

		Method m = MethodUtils.getAccessibleMethod(beanClass, destroyMethodName, null);
		
		if(m == null)
			throw new IllegalArgumentException("No such destroy method " + destroyMethodName + "() on bean class: " + beanClass);
	}

}
