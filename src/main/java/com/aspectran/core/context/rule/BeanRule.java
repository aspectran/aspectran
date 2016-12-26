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
import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.bean.InstantiatedBean;
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

	public static final String CLASS_DIRECTIVE_PREFIX = "class:";

	private static final BeanReferrerType BEAN_REFERRER_TYPE = BeanReferrerType.BEAN_RULE;

	private String id;

	private String className;

	private Class<?> beanClass;

	private String scanPattern;
	
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

	private List<AutowireTargetRule> autowireTargetRuleList;

	private boolean fieldAutowireParsed;

	private boolean methodAutowireParsed;

	private InstantiatedBean instantiatedBean; // only for singleton

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
	 * Gets the scan pattern.
	 *
	 * @return the scan pattern
	 */
	public String getScanPattern() {
		return scanPattern;
	}

	/**
	 * Sets the scan pattern.
	 *
	 * @param scanPattern the new scan pattern
	 */
	public void setScanPattern(String scanPattern) {
		this.scanPattern = scanPattern;
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

	public boolean isSingleton() {
		return (this.scopeType == ScopeType.SINGLETON);
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

	public Class<?> getTargetBeanClass() {
		return (targetBeanClass != null ?  targetBeanClass : beanClass);
	}

	public void setTargetBeanClass(Class<?> targetBeanClass) {
		this.targetBeanClass = targetBeanClass;
	}

	public String getTargetBeanClassName() {
		return (targetBeanClass != null ? targetBeanClass.getName() : className);
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

	public boolean isFactoryOperationRequired() {
		return !isOffered() && (isFactoryBean() || getFactoryMethod() != null);
	}

	/**
	 * Returns the initialization method name.
	 *
	 * @return the initialization method name
	 */
	public String getInitMethodName() {
		return initMethodName;
	}

	/**
	 * Sets the initialization method name.
	 *
	 * @param initMethodName the new initialization method name
	 */
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}

	/**
	 * Returns the initialization method.
	 *
	 * @return the initialization method
	 */
	public Method getInitMethod() {
		return initMethod;
	}

	/**
	 * Sets the initialization method.
	 *
	 * @param initMethod the initialization method
	 */
	public void setInitMethod(Method initMethod) {
		this.initMethod = initMethod;
	}

	/**
	 * Returns whether the initialization method requires the Translate argument.
	 *
	 * @return true if the initialization method requires the Translate argument, otherwise false
	 */
	public boolean isInitMethodRequiresTranslet() {
		return initMethodRequiresTranslet;
	}

	/**
	 * Sets whether the initialization method requires the Translate argument.
	 *
	 * @param initMethodRequiresTranslet whether or not the initialization method requires Translet argument
	 */
	public void setInitMethodRequiresTranslet(boolean initMethodRequiresTranslet) {
		this.initMethodRequiresTranslet = initMethodRequiresTranslet;
	}

	/**
	 * Returns the destroy method name.
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
	 * Returns the destroy method.
	 *
	 * @return the destroy method
	 */
	public Method getDestroyMethod() {
		return destroyMethod;
	}

	/**
	 * Sets the destroy method.
	 *
	 * @param destroyMethod the new destroy method
	 */
	public void setDestroyMethod(Method destroyMethod) {
		this.destroyMethod = destroyMethod;
	}

	/**
	 * Returns whether this bean is to be lazily initialized.
	 *
	 * @return true, if this bean is to be lazily initialized
	 */
	public Boolean getLazyInit() {
		return lazyInit;
	}

	/**
	 * Sets whether this bean is to be lazily initialized.
	 *
	 * @param lazyInit whether this bean is to be lazily initialized
	 */
	public void setLazyInit(Boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * Returns whether this bean is to be lazily initialized.
	 *
	 * @return true, if this bean is to be lazily initialized
	 */
	public boolean isLazyInit() {
		return BooleanUtils.toBoolean(lazyInit);
	}

	/**
	 * Returns whether this bean is important.
	 *
	 * @return whether this bean is important
	 */
	public Boolean getImportant() {
		return important;
	}
	
	/**
	 * Sets whether important bean.
	 * 
	 * @param important whether important bean
	 */
	public void setImportant(Boolean important) {
		this.important = important;
	}

	/**
	 * Returns whether this bean is important.
	 *
	 * @return whether this bean is important
	 */
	public boolean isImportant() {
		return BooleanUtils.toBoolean(important);
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
	 * Returns whether this bean implements FactoryBean.
	 *
	 * @return the boolean
	 */
	public boolean isFactoryBean() {
		return factoryBean;
	}

	/**
	 * Returns whether this bean implements DisposableBean.
	 *
	 * @return the boolean
	 */
	public boolean isDisposableBean() {
		return disposableBean;
	}

	/**
	 * Returns whether this bean implements InitializableBean.
	 *
	 * @return the boolean
	 */
	public boolean isInitializableBean() {
		return initializableBean;
	}

	/**
	 * Returns whether this bean implements InitializableTransletBean.
	 *
	 * @return the boolean
	 */
	public boolean isInitializableTransletBean() {
		return initializableTransletBean;
	}

	/**
	 * Returns whether this bean has been replicated.
	 *
	 * @return true if this bean has been replicated, otherwise false
	 */
	public boolean isReplicated() {
		return replicated;
	}

	/**
	 * Sets whether this bean is replicated.
	 *
	 * @param replicated true, if this bean is replicated
	 */
	public void setReplicated(boolean replicated) {
		this.replicated = replicated;
	}

	/**
	 * Returns whether this bean is proxied.
	 *
	 * @return true if this bean is proxied, otherwise false
	 */
	public boolean isProxied() {
		return proxied;
	}

	/**
	 * Sets whether this bean is proxied.
	 *
	 * @param proxied true, if this bean is proxied
	 */
	public void setProxied(boolean proxied) {
		this.proxied = proxied;
	}

	/**
	 * Returns whether this bean can be proxied.
	 *
	 * @return true if this bean can be proxied, otherwise false
	 */
	public boolean isProxiable() {
		return (!offered && !factoryBean && factoryMethod == null);
	}

	public List<AutowireTargetRule> getAutowireTargetRuleList() {
		return autowireTargetRuleList;
	}

	public void addAutowireTargetRule(AutowireTargetRule autowireTargetRule) {
		if (autowireTargetRuleList == null) {
			autowireTargetRuleList = new ArrayList<AutowireTargetRule>();
		}
		autowireTargetRuleList.add(autowireTargetRule);
	}

	public boolean isFieldAutowireParsed() {
		return fieldAutowireParsed;
	}

	public void setFieldAutowireParsed(boolean fieldAutowireParsed) {
		this.fieldAutowireParsed = fieldAutowireParsed;
	}

	public boolean isMethodAutowireParsed() {
		return methodAutowireParsed;
	}

	public void setMethodAutowireParsed(boolean methodAutowireParsed) {
		this.methodAutowireParsed = methodAutowireParsed;
	}

	/**
	 * Returns the instantiated object of this bean.
	 *
	 * @return the instantiated object of this bean
	 */
	public InstantiatedBean getInstantiatedBean() {
		return instantiatedBean;
	}

	/**
	 * Sets the instantiated object of this bean.
	 *
	 * @param instantiatedBean the instantiated object of this bean
	 */
	public void setInstantiatedBean(InstantiatedBean instantiatedBean) {
		this.instantiatedBean = instantiatedBean;
	}

	/**
	 * Gets the description of this bean.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this bean.
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
		return BEAN_REFERRER_TYPE;
	}
	
	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("id", id);
		if (!offered) {
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
			if (constructorArgumentItemRuleMap != null) {
				tsb.append("constructorArguments", constructorArgumentItemRuleMap.keySet());
			}
			if (propertyItemRuleMap != null) {
				tsb.append("properties", propertyItemRuleMap.keySet());
			}
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
			String scanPattern,
			String maskPattern,
			String initMethodName,
			String destroyMethodName,
			String factoryMethodName,
			String scope,
			Boolean singleton,
			Boolean lazyInit,
			Boolean important) {
		
		if (className == null && scanPattern == null) {
			throw new IllegalArgumentException("The 'bean' element requires a 'class' attribute.");
		}

		ScopeType scopeType = ScopeType.resolve(scope);

		if (scope != null && scopeType == null) {
			throw new IllegalArgumentException("No scope type registered for '" + scope + "'.");
		}

		if (scopeType == null) {
			scopeType = (singleton == null || singleton == Boolean.TRUE) ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
		}

		BeanRule beanRule = new BeanRule();
		beanRule.setId(id);
		if (scanPattern == null) {
			beanRule.setClassName(className);
		} else {
			beanRule.setScanPattern(scanPattern);
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

        if (offerBeanId == null || offerMethodName == null) {
			throw new IllegalArgumentException("The 'bean' element requires both 'offerBean' attribute and 'offerMethod' attribute.");
		}

		ScopeType scopeType = ScopeType.resolve(scope);
		
		if (scope != null && scopeType == null) {
			throw new IllegalArgumentException("No scope type registered for '" + scope + "'.");
		}
		
		if (scopeType == null) {
			scopeType = (singleton == null || singleton == Boolean.TRUE) ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
		}
		
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
		if (beanRule.getScanPattern() == null) {
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
		if (!beanRule.isOffered()) {
			List<Parameters> argumentParametersList = ItemRule.toItemParametersList(text);
			if (argumentParametersList == null) {
				return;
			}
			ItemRuleMap constructorArgumentItemRuleMap = ItemRule.toItemRuleMap(argumentParametersList);
			if (constructorArgumentItemRuleMap == null) {
				return;
			}
			beanRule.setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
		}
	}
	
	public static void updateProperty(BeanRule beanRule, String text) {
		if (!beanRule.isOffered()) {
			List<Parameters> propertyParametersList = ItemRule.toItemParametersList(text);
			if (propertyParametersList == null) {
				return;
			}
			ItemRuleMap propertyItemRuleMap = ItemRule.toItemRuleMap(propertyParametersList);
			if (propertyItemRuleMap == null) {
				return;
			}
			beanRule.setPropertyItemRuleMap(propertyItemRuleMap);
		}
	}

}
