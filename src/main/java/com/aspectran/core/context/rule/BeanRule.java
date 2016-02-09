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
package com.aspectran.core.context.rule;

import java.lang.reflect.Method;
import java.util.List;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.bean.BeanRuleException;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class BeanRule.
 * 
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public class BeanRule implements Replicable<BeanRule> {

	private String id;

	private String className;

	private Class<?> beanClass;

	private String scanPath;
	
	private String maskPattern;
	
	private Parameters filterParameters;

	private ScopeType scopeType;

	private Boolean singleton;

	private String offerBeanId;
	
	private Class<?> offerBeanClass;

	private String offerMethodName;

	private boolean offered;

	private String factoryMethodName;

	private boolean factoryMethodRequiresTranslet;

	private String initMethodName;
	
	private boolean initMethodRequiresTranslet;
	
	private String destroyMethodName;

	private ItemRuleMap constructorArgumentItemRuleMap;
	
	private ItemRuleMap propertyItemRuleMap;

	private Boolean lazyInit;

	private Boolean important;

	private String description;

	private boolean factoryBean;

	private boolean disposableBean;

	private boolean initializableBean;

	private boolean initializableTransletBean;

	private boolean replicated;

	private boolean proxied;

	private Object bean; // only for singleton

	private boolean registered;

	/**
	 * Returns the bean id.
	 *
	 * @return the bean id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the bean id.
	 *
	 * @param id the bean id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the class type.
	 *
	 * @return the class type
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets the class type.
	 *
	 * @param className the new class name
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Gets the bean class.
	 *
	 * @return the bean class
	 */
	public Class<?> getBeanClass() {
		return beanClass;
	}

	/**
	 * Sets the bean class.
	 *
	 * @param beanClass the new bean class
	 */
	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
		this.className = beanClass.getName();
		this.factoryBean = FactoryBean.class.isAssignableFrom(beanClass);
		this.disposableBean = DisposableBean.class.isAssignableFrom(beanClass);
		this.initializableBean = InitializableBean.class.isAssignableFrom(beanClass);
		this.initializableTransletBean = InitializableTransletBean.class.isAssignableFrom(beanClass);
	}

	/**
	 * Gets the scan path.
	 *
	 * @return the scan path
	 */
	public String getScanPath() {
		return scanPath;
	}

	/**
	 * Sets the scan path.
	 *
	 * @param scanPath the new scan path
	 */
	public void setScanPath(String scanPath) {
		this.scanPath = scanPath;
	}

	/**
	 * Gets the mask pattern.
	 *
	 * @return the mask pattern
	 */
	public String getMaskPattern() {
		return maskPattern;
	}

	/**
	 * Gets the filter parameters.
	 *
	 * @return the filter parameters
	 */
	public Parameters getFilterParameters() {
		return filterParameters;
	}

	/**
	 * Sets the filter parameters.
	 *
	 * @param filterParameters the new filter parameters
	 */
	public void setFilterParameters(Parameters filterParameters) {
		this.filterParameters = filterParameters;
	}

	/**
	 * Sets the mask pattern.
	 *
	 * @param maskPattern the new mask pattern
	 */
	public void setMaskPattern(String maskPattern) {
		this.maskPattern = maskPattern;
	}

	/**
	 * Gets the scope type.
	 *
	 * @return the scope type
	 */
	public ScopeType getScopeType() {
		return scopeType;
	}

	/**
	 * Sets the scope type.
	 *
	 * @param scopeType the new scope type
	 */
	public void setScopeType(ScopeType scopeType) {
		this.scopeType = scopeType;
	}

	/**
	 * Returns whether Singleton. 
	 *
	 * @return whether Singleton
	 */
	public Boolean getSingleton() {
		return singleton;
	}

	/**
	 * Sets whether Singleton. 
	 *
	 * @param singleton whether Singleton
	 */
	public void setSingleton(Boolean singleton) {
		this.singleton = singleton;
	}

	/**
	 * Gets the offer bean id.
	 *
	 * @return the offer bean id
	 */
	public String getOfferBeanId() {
		return offerBeanId;
	}

	/**
	 * Sets the offer bean id.
	 *
	 * @param offerBeanId the new offer bean id
	 */
	public void setOfferBeanId(String offerBeanId) {
		this.offerBeanId = offerBeanId;
	}

	/**
	 * Gets offer bean class.
	 *
	 * @return the offer bean class
	 */
	public Class<?> getOfferBeanClass() {
		return offerBeanClass;
	}

	/**
	 * Sets offer bean class.
	 *
	 * @param offerBeanClass the offer bean class
	 */
	public void setOfferBeanClass(Class<?> offerBeanClass) {
		this.offerBeanClass = offerBeanClass;
	}

	/**
	 * Gets the offer bean's offer method name.
	 *
	 * @return the factory method
	 */
	public String getOfferMethodName() {
		return offerMethodName;
	}

	/**
	 * Sets the offer bean's method name.
	 *
	 * @param offerMethodName the new offer method name
	 */
	public void setOfferMethodName(String offerMethodName) {
		this.offerMethodName = offerMethodName;
	}
	
	public boolean isOffered() {
		return offered;
	}

	public void setOfferd(boolean offered) {
		this.offered = offered;
	}

	/**
	 * Gets the factory method name.
	 *
	 * @return the factory method
	 */
	public String getFactoryMethodName() {
		return factoryMethodName;
	}
	
	/**
	 * Sets the factory method name.
	 *
	 * @param factoryMethodName the new factory method name
	 */
	public void setFactoryMethodName(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}

	public boolean isFactoryMethodRequiresTranslet() {
		return factoryMethodRequiresTranslet;
	}

	public void setFactoryMethodRequiresTranslet(boolean factoryMethodRequiresTranslet) {
		this.factoryMethodRequiresTranslet = factoryMethodRequiresTranslet;
	}

	/**
	 * Gets the inits the method name.
	 *
	 * @return the inits the method name
	 */
	public String getInitMethodName() {
		return initMethodName;
	}

	/**
	 * Sets the inits the method name.
	 *
	 * @param initMethodName the new inits the method name
	 */
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}
	
	/**
	 * Returns whether or not the initialization method requiring Translet argument.
	 *
	 * @return whether or not the initialization method requiring Translet argument
	 */
	public boolean isInitMethodRequiresTranslet() {
		return initMethodRequiresTranslet;
	}

	/**
	 * Sets whether or not the initialization method requiring Translet argument.
	 *
	 * @param initMethodRequiresTranslet whether or not the initialization method requiring Translet argument
	 */
	public void setInitMethodRequiresTranslet(boolean initMethodRequiresTranslet) {
		this.initMethodRequiresTranslet = initMethodRequiresTranslet;
	}

	/**
	 * Gets the destroy method name.
	 *
	 * @return the destroy method name
	 */
	public String getDestroyMethodName() {
		return destroyMethodName;
	}

	/**
	 * Sets the destroy method name.
	 *
	 * @param destroyMethodName the new destroy method name
	 */
	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}

	/**
	 * Returns whether the lazy initialization mode.
	 *
	 * @return true, if is lazy initialization mode
	 */
	public Boolean getLazyInit() {
		return lazyInit;
	}

	/**
	 * Returns whether the lazy initialization mode.
	 *
	 * @return true, if is lazy initialization mode
	 */
	public boolean isLazyInit() {
		return BooleanUtils.toBoolean(lazyInit);
	}

	/**
	 * Sets whether the lazy initialization mode.
	 *
	 * @param lazyInit whether the lazy initialization mode
	 */
	public void setLazyInit(Boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	public Boolean getImportant() {
		return important;
	}

	public boolean isImportant() {
		return BooleanUtils.toBoolean(important);
	}
	
	public void setImportant(Boolean important) {
		this.important = important;
	}

	/**
	 * Gets the constructor argument item rule map.
	 *
	 * @return the constructor argument item rule map
	 */
	public ItemRuleMap getConstructorArgumentItemRuleMap() {
		return constructorArgumentItemRuleMap;
	}

	/**
	 * Sets the constructor argument item rule map.
	 *
	 * @param constructorArgumentItemRuleMap the new constructor argument item rule map
	 */
	public void setConstructorArgumentItemRuleMap(ItemRuleMap constructorArgumentItemRuleMap) {
		this.constructorArgumentItemRuleMap = constructorArgumentItemRuleMap;
	}

	/**
	 * Gets the property item rule map.
	 *
	 * @return the property item rule map
	 */
	public ItemRuleMap getPropertyItemRuleMap() {
		return propertyItemRuleMap;
	}

	/**
	 * Sets the property item rule map.
	 *
	 * @param propertyItemRuleMap the new property item rule map
	 */
	public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
		this.propertyItemRuleMap = propertyItemRuleMap;
	}
	
	/**
	 * Gets the bean.
	 *
	 * @return the bean
	 */
	public Object getBean() {
		return bean;
	}

	/**
	 * Sets the bean.
	 *
	 * @param bean the new bean
	 */
	public void setBean(Object bean) {
		this.bean = bean;
	}

	/**
	 * Returns whether bean implements FactoryBean.
	 *
	 * @return the boolean
	 */
	public boolean isFactoryBean() {
		return factoryBean;
	}

	/**
	 * Returns whether bean implements DisposableBean.
	 *
	 * @return the boolean
	 */
	public boolean isDisposableBean() {
		return disposableBean;
	}

	/**
	 * Returns whether bean implements InitializableBean.
	 *
	 * @return the boolean
	 */
	public boolean isInitializableBean() {
		return initializableBean;
	}

	/**
	 * Returns whether bean implements InitializableTransletBean.
	 *
	 * @return the boolean
	 */
	public boolean isInitializableTransletBean() {
		return initializableTransletBean;
	}

	/**
	 * Returns whether bean is registered.
	 *
	 * @return true, if bean is registered
	 */
	public boolean isRegistered() {
		return registered;
	}

	/**
	 * Sets whether bean is registered.
	 *
	 * @param registered the new registered
	 */
	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	/**
	 * Returns whether bean is replicated.
	 *
	 * @return true, if is replicated
	 */
	public boolean isReplicated() {
		return replicated;
	}

	/**
	 * Sets whether bean is replicated.
	 *
	 * @param replicated true, if is replicated
	 */
	public void setReplicated(boolean replicated) {
		this.replicated = replicated;
	}

	/**
	 * Returns whether bean is proxied.
	 *
	 * @return the boolean
	 */
	public boolean isProxied() {
		return proxied;
	}

	/**
	 * Sets whether bean is proxied.
	 *
	 * @param proxied true, if is proxied
	 */
	public void setProxied(boolean proxied) {
		this.proxied = proxied;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public BeanRule replicate() {
		return replicate(this);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("{id=").append(id);
		if(!offered) {
			sb.append(", class=").append(className);
			sb.append(", scope=").append(scopeType);
			if(initMethodName != null)
				sb.append(", initMethod=").append(initMethodName);
			if(initMethodName != null)
				sb.append(", factoryMethod=").append(factoryMethodName);
			if(destroyMethodName != null)
				sb.append(", destroyMethod=").append(destroyMethodName);
			if(factoryBean)
				sb.append(", factoryBean=").append(factoryBean);
			if(initializableBean)
				sb.append(", initializableBean=").append(initializableBean);
			if(initializableTransletBean)
				sb.append(", initializableBean=").append(initializableTransletBean);
			if(disposableBean)
				sb.append(", disposableBean=").append(disposableBean);
			if(lazyInit != null)
				sb.append(", lazyInit=").append(lazyInit);
			if(important != null)
				sb.append(", important=").append(important);
			sb.append(", proxied=").append(proxied);
			sb.append(", replicated=").append(replicated);
			if(constructorArgumentItemRuleMap != null) {
				sb.append(", constructorArguments=[");
				int sbLength = sb.length();
				for (String name : constructorArgumentItemRuleMap.keySet()) {
					if (sb.length() > sbLength)
						sb.append(", ");

					sb.append(name);
				}
				sb.append("]");
			}
			if(propertyItemRuleMap != null) {
				sb.append(", properties=[");
				int sbLength = sb.length();
				for (String name : propertyItemRuleMap.keySet()) {
					if (sb.length() > sbLength)
						sb.append(", ");

					sb.append(name);
				}
				sb.append("]");
			}
		} else {
			sb.append(", scope=").append(scopeType);
			sb.append(", offerBean=").append(offerBeanId);
			sb.append(", offerMethod=").append(offerMethodName);
			if(initMethodName != null)
				sb.append(", initMethod=").append(initMethodName);
			if(factoryMethodName != null)
				sb.append(", factoryMethod=").append(factoryMethodName);
			if(destroyMethodName != null)
				sb.append(", destroyMethod=").append(destroyMethodName);
			if(lazyInit != null)
				sb.append(", lazyInit=").append(lazyInit);
			if(important != null)
				sb.append(", important=").append(important);
			sb.append(", proxied=").append(proxied);
		}
		sb.append("}");
		
		return sb.toString();
	}

	public static BeanRule newInstance(String id, String className, String scanPath, String maskPattern, String scope, Boolean singleton, String initMethodName, String factoryMethodName, String destroyMethodName, Boolean lazyInit, Boolean important) {
		if(className == null && scanPath == null)
			throw new IllegalArgumentException("Bean class must not be null.");

		ScopeType scopeType = ScopeType.lookup(scope);

		if(scope != null && scopeType == null)
			throw new IllegalArgumentException("No scope type registered for '" + scope + "'.");

		if(scopeType == null)
			scopeType = (singleton == Boolean.TRUE) ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;

		BeanRule beanRule = new BeanRule();
		beanRule.setId(id);
		if(scanPath == null) {
			beanRule.setClassName(className);
		} else {
			beanRule.setScanPath(scanPath);
			beanRule.setMaskPattern(maskPattern);
		}
		beanRule.setScopeType(scopeType);
		beanRule.setSingleton(singleton);
		beanRule.setInitMethodName(initMethodName);
		beanRule.setFactoryMethodName(factoryMethodName);
		beanRule.setDestroyMethodName(destroyMethodName);
		beanRule.setLazyInit(lazyInit);
		beanRule.setImportant(important);

		return beanRule;
	}
	
	public static BeanRule newOfferedBeanInstance(String id, String scope, Boolean singleton, String offerBeanId, String offerMethodName, String initMethodName, String factoryMethodName, String destroyMethodName, Boolean lazyInit, Boolean important) {
		if(id == null)
			throw new IllegalArgumentException("Bean id must not be null.");
		
		ScopeType scopeType = ScopeType.lookup(scope);
		
		if(scope != null && scopeType == null)
			throw new IllegalArgumentException("No scope type registered for '" + scope + "'.");
		
		if(scopeType == null)
			scopeType = (singleton == Boolean.TRUE) ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
		
		BeanRule beanRule = new BeanRule();
		beanRule.setId(id);
		beanRule.setScopeType(scopeType);
		beanRule.setSingleton(singleton);
		beanRule.setOfferBeanId(offerBeanId);
		beanRule.setOfferMethodName(offerMethodName);
		beanRule.setOfferd(true);
		beanRule.setInitMethodName(initMethodName);
		beanRule.setFactoryMethodName(factoryMethodName);
		beanRule.setDestroyMethodName(destroyMethodName);
		beanRule.setLazyInit(lazyInit);
		beanRule.setImportant(important);
		
		return beanRule;
	}
	
	public static BeanRule replicate(BeanRule beanRule) {
		BeanRule br = new BeanRule();
		br.setId(beanRule.getId());
		if(beanRule.getScanPath() == null) {
			br.setBeanClass(beanRule.getBeanClass());
		}
		br.setScopeType(beanRule.getScopeType());
		br.setSingleton(beanRule.getSingleton());
		br.setOfferBeanId(beanRule.getOfferBeanId());
		br.setOfferMethodName(beanRule.getOfferMethodName());
		br.setOfferd(beanRule.isOffered());
		br.setInitMethodName(beanRule.getInitMethodName());
		br.setInitMethodRequiresTranslet(beanRule.isInitMethodRequiresTranslet());
		br.setFactoryMethodName(beanRule.getFactoryMethodName());
		br.setFactoryMethodRequiresTranslet(beanRule.isFactoryMethodRequiresTranslet());
		br.setDestroyMethodName(beanRule.getDestroyMethodName());
		br.setConstructorArgumentItemRuleMap(beanRule.getConstructorArgumentItemRuleMap());
		br.setPropertyItemRuleMap(beanRule.getPropertyItemRuleMap());
		br.setLazyInit(beanRule.getLazyInit());
		br.setImportant(beanRule.getImportant());
		br.setDescription(beanRule.getDescription());
		br.setReplicated(true);
		
		return br;
	}

	public static void checkAccessibleMethod(BeanRule beanRule) {
		Class<?> beanClass = beanRule.getBeanClass();
		String initMethodName = beanRule.getInitMethodName();
		String factoryMethodName = beanRule.getFactoryMethodName();
		String destroyMethodName = beanRule.getDestroyMethodName();

		Class<?>[] parameterTypes = { Translet.class };

		if(initMethodName != null) {
			if(beanRule.isInitializableBean())
				throw new BeanRuleException(beanRule, "Bean initialization method  is duplicated. Already implemented the InitializableBean");

			if(beanRule.isInitializableTransletBean())
				throw new BeanRuleException(beanRule, "Bean initialization method  is duplicated. Already implemented the InitializableTransletBean");

			Method m1 = MethodUtils.getAccessibleMethod(beanClass, initMethodName, null);
			Method m2 = MethodUtils.getAccessibleMethod(beanClass, initMethodName, parameterTypes);

			if(m1 == null && m2 == null)
				throw new IllegalArgumentException("No such initialization method '" + initMethodName + "() on bean class: " + beanClass);

			if(m2 != null)
				beanRule.setInitMethodRequiresTranslet(true);
		}

		if(factoryMethodName != null) {
			if(beanRule.isFactoryBean())
				throw new BeanRuleException(beanRule, "Bean factory method  is duplicated. Already implemented the FactoryBean");

			Method m1 = MethodUtils.getAccessibleMethod(beanClass, factoryMethodName, null);
			Method m2 = MethodUtils.getAccessibleMethod(beanClass, factoryMethodName, parameterTypes);

			if(m1 == null && m2 == null)
				throw new IllegalArgumentException("No such factory method '" + factoryMethodName + "() on bean class: " + beanClass);

			if(m2 != null)
				beanRule.setFactoryMethodRequiresTranslet(true);
		}

		if(destroyMethodName != null) {
			if(beanRule.isDisposableBean())
				throw new BeanRuleException(beanRule, "Bean destroy method  is duplicated. Already implemented the DisposableBean");

			Method m1 = MethodUtils.getAccessibleMethod(beanClass, destroyMethodName, null);
			if(m1 == null)
				throw new IllegalArgumentException("No such destroy method '" + destroyMethodName + "() on bean class: " + beanClass);
		}
	}

	public static void updateConstructorArgument(BeanRule beanRule, String text) {
		if(!beanRule.isOffered()) {
			List<Parameters> argumentParametersList = ItemRule.toItemParametersList(text);

			if(argumentParametersList == null)
				return;

			ItemRuleMap constructorArgumentItemRuleMap = ItemRule.toItemRuleMap(argumentParametersList);

			if(constructorArgumentItemRuleMap == null)
				return;

			beanRule.setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
		}
	}
	
	public static void updateProperty(BeanRule beanRule, String text) {
		if(!beanRule.isOffered()) {
			List<Parameters> propertyParametersList = ItemRule.toItemParametersList(text);

			if(propertyParametersList == null)
				return;

			ItemRuleMap propertyItemRuleMap = ItemRule.toItemRuleMap(propertyParametersList);

			if(propertyItemRuleMap == null)
				return;

			beanRule.setPropertyItemRuleMap(propertyItemRuleMap);
		}
	}

}
