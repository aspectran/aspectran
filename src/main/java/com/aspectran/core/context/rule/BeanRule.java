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

import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class BeanRule.
 * 
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public class BeanRule implements Replicable<BeanRule>, BeanReferenceInspectable {

	public static final String CLASS_DIRECTIVE = "class";

	public static final String CLASS_DIRECTIVE_PREFIX = "class:";

	private static final BeanReferrerType BEAN_REFERABLE_RULE_TYPE = BeanReferrerType.BEAN_RULE;

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

	private Method offerMethod;
	
	private boolean offerMethodRequiresTranslet;

	private boolean offered;
	
	private Class<?> targetBeanClass;

	private String factoryMethodName;

	private Method factoryMethod;
	
	private boolean factoryMethodRequiresTranslet;

	private String initMethodName;
	
	private Method initMethod;
	
	private boolean initMethodRequiresTranslet;
	
	private String destroyMethodName;

	private Method destroyMethod;
	
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
	 * @throws SecurityException 
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
	
	public Method getOfferMethod() {
		return offerMethod;
	}

	public void setOfferMethod(Method offerMethod) {
		this.offerMethod = offerMethod;
	}

	public boolean isOfferMethodRequiresTranslet() {
		return offerMethodRequiresTranslet;
	}

	public void setOfferMethodRequiresTranslet(boolean offerMethodRequiresTranslet) {
		this.offerMethodRequiresTranslet = offerMethodRequiresTranslet;
	}

	public boolean isOffered() {
		return offered;
	}

	public void setOffered(boolean offered) {
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
	
	public Class<?> getTargetBeanClass() {
		if(targetBeanClass != null)
			return targetBeanClass;
		return beanClass;
	}

	public void setTargetBeanClass(Class<?> targetBeanClass) {
		this.targetBeanClass = targetBeanClass;
	}
	
	public String getTargetBeanClassName() {
		if(targetBeanClass != null)
			return targetBeanClass.getName();
		return className;
	}

	/**
	 * Sets the factory method name.
	 *
	 * @param factoryMethodName the new factory method name
	 */
	public void setFactoryMethodName(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}

	public Method getFactoryMethod() {
		return factoryMethod;
	}

	public void setFactoryMethod(Method factoryMethod) {
		this.factoryMethod = factoryMethod;
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
	
	public Method getInitMethod() {
		return initMethod;
	}

	public void setInitMethod(Method initMethod) {
		this.initMethod = initMethod;
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

	public Method getDestroyMethod() {
		return destroyMethod;
	}

	public void setDestroyMethod(Method destroyMethod) {
		this.destroyMethod = destroyMethod;
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

	public boolean isProxiable() {
		return (!offered && !factoryBean && factoryMethod == null);
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
	public BeanReferrerType getBeanReferrerType() {
		return BEAN_REFERABLE_RULE_TYPE;
	}
	
	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("id", id);
		if(!offered) {
			tsb.append("class", className);
			tsb.append("scope", scopeType);
			tsb.append("initMethod", initMethodName);
			tsb.append("factoryMethod", factoryMethodName);
			tsb.append("destroyMethod", destroyMethodName);
			tsb.append("factoryBean", factoryBean);
			tsb.append("initializableBean", initializableBean);
			tsb.append("initializableTransletBean", initializableTransletBean);
			tsb.append("disposableBean", disposableBean);
			tsb.append("lazyInit", lazyInit);
			tsb.append("important", important);
			tsb.append("proxied", proxied);
			tsb.append("replicated", replicated);
			if(constructorArgumentItemRuleMap != null)
				tsb.append("constructorArguments", constructorArgumentItemRuleMap.keySet());
			if(propertyItemRuleMap != null)
				tsb.append("properties", propertyItemRuleMap.keySet());
		} else {
			tsb.append("scope", scopeType);
			tsb.append("offerBean", offerBeanId);
			tsb.append("offerMethod", offerMethodName);
			tsb.append("initMethod", initMethodName);
			tsb.append("factoryMethod", factoryMethodName);
			tsb.append("destroyMethod", destroyMethodName);
			tsb.append("lazyInit", lazyInit);
			tsb.append("important", important);
			tsb.append("proxied", proxied);
		}
		return tsb.toString();
	}

	public static BeanRule newInstance(
			String id,
			String className,
			String scanPath,
			String maskPattern,
			String initMethodName,
			String destroyMethodName,
			String factoryMethodName,
			String scope,
			Boolean singleton,
			Boolean lazyInit,
			Boolean important) {
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
		beanRule.setDestroyMethodName(destroyMethodName);
		beanRule.setFactoryMethodName(factoryMethodName);
		beanRule.setLazyInit(lazyInit);
		beanRule.setImportant(important);

		return beanRule;
	}
	
	public static BeanRule newOfferedBeanInstance(
			String id,
			String offerBeanId,
			String offerMethodName,
			String initMethodName,
			String destroyMethodName,
			String factoryMethodName,
			String scope,
			Boolean singleton,
			Boolean lazyInit,
			Boolean important) {
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
		beanRule.setOffered(true);
		beanRule.setInitMethodName(initMethodName);
		beanRule.setDestroyMethodName(destroyMethodName);
		beanRule.setFactoryMethodName(factoryMethodName);
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
		br.setInitMethodName(beanRule.getInitMethodName());
		br.setDestroyMethodName(beanRule.getDestroyMethodName());
		br.setFactoryMethodName(beanRule.getFactoryMethodName());
		br.setConstructorArgumentItemRuleMap(beanRule.getConstructorArgumentItemRuleMap());
		br.setPropertyItemRuleMap(beanRule.getPropertyItemRuleMap());
		br.setLazyInit(beanRule.getLazyInit());
		br.setImportant(beanRule.getImportant());
		br.setDescription(beanRule.getDescription());
		br.setReplicated(true);
		
		return br;
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
