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
package com.aspectran.core.context.rule;

import java.util.List;

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
 * <p>Created: 2009. 03. 09 오후 23:48:09</p>
 */
public class BeanRule implements Cloneable {

	protected String id;

	protected String className;

	protected Class<?> beanClass;

	protected String maskPattern;
	
	protected ScopeType scopeType;

	protected Boolean singleton;

	protected String factoryBeanId;
	
	protected String factoryMethodName;

	protected String initMethodName;
	
	private Boolean initMethodRequiresTranslet;
	
	protected String destroyMethodName;

	protected Boolean lazyInit;

	protected Boolean important;

	protected ItemRuleMap constructorArgumentItemRuleMap;
	
	protected ItemRuleMap propertyItemRuleMap;
	
	private Object bean;

	private boolean factoryBeanReferenced;
	
	private boolean factoryBeanImplmented;

	private boolean registered;
	
	private boolean overrided;

	private boolean scanned;
	
	private boolean proxied;
	
	private Parameters filterParameters;
	
	private String description;
	
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
	 * Gets the factory bean id.
	 *
	 * @return the factory bean id
	 */
	public String getFactoryBeanId() {
		return factoryBeanId;
	}

	/**
	 * Sets the factory bean id.
	 *
	 * @param factoryBeanId the new factory bean id
	 */
	public void setFactoryBeanId(String factoryBeanId) {
		this.factoryBeanId = factoryBeanId;
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
	public Boolean getInitMethodRequiresTranslet() {
		return initMethodRequiresTranslet;
	}

	/**
	 * Sets whether or not the initialization method requiring Translet argument.
	 *
	 * @param initMethodRequiresTranslet whether or not the initialization method requiring Translet argument
	 */
	public void setInitMethodRequiresTranslet(Boolean initMethodRequiresTranslet) {
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

	public boolean isFactoryBeanReferenced() {
		return factoryBeanReferenced;
	}

	public void setFactoryBeanReferenced(boolean factoryBeanReferenced) {
		this.factoryBeanReferenced = factoryBeanReferenced;
	}

	public boolean isFactoryBeanImplmented() {
		return factoryBeanImplmented;
	}

	public void setFactoryBeanImplmented(boolean factoryBeanImplmented) {
		this.factoryBeanImplmented = factoryBeanImplmented;
	}

	/**
	 * Checks if is registered.
	 *
	 * @return true, if is registered
	 */
	public boolean isRegistered() {
		return registered;
	}

	/**
	 * Sets the registered.
	 *
	 * @param registered the new registered
	 */
	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	/**
	 * Returns <code>true</code> if the bean definition was overridden.
	 *
	 * @return true, if is overrided
	 */
	public boolean isOverrided() {
		return overrided;
	}
	
	public void setOverrided(boolean overrided) {
		this.overrided = overrided;
	}

	/**
	 * Returns <code>true</code> if the bean is auto-scanned.
	 *
	 * @return true, if is scanned
	 */
	public boolean isScanned() {
		return scanned;
	}

	public void setScanned(boolean scanned) {
		this.scanned = scanned;
	}
	
	public boolean isProxied() {
		return proxied;
	}

	public void setProxied(boolean proxied) {
		this.proxied = proxied;
	}

	public Parameters getFilterParameters() {
		return filterParameters;
	}

	public void setFilterParameters(Parameters filterParameters) {
		this.filterParameters = filterParameters;
	}

	public BeanRule clone() throws CloneNotSupportedException {
		// shallow copy
		return (BeanRule)super.clone();              
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{id=").append(id);
		if(!factoryBeanReferenced) {
			sb.append(", class=").append(className);
			sb.append(", scope=").append(scopeType);
			sb.append(", factoryBean=").append(factoryBeanId);
			sb.append(", factoryMethod=").append(factoryMethodName);
			sb.append(", initMethod=").append(initMethodName);
			sb.append(", destroyMethod=").append(destroyMethodName);
			sb.append(", lazyInit=").append(lazyInit);
			sb.append(", important=").append(important);
			sb.append(", proxied=").append(proxied);
			sb.append(", scanned=").append(scanned);
			if (constructorArgumentItemRuleMap != null) {
				sb.append(", constructorArguments=[");
				int sbLength = sb.length();
				for (String name : constructorArgumentItemRuleMap.keySet()) {
					if (sb.length() > sbLength)
						sb.append(", ");

					sb.append(name);
				}
				sb.append("]");
			}
			if (propertyItemRuleMap != null) {
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
			sb.append(", factoryBean=").append(factoryBeanId);
			sb.append(", factoryMethod=").append(factoryMethodName);
			sb.append(", lazyInit=").append(lazyInit);
			sb.append(", important=").append(important);
		}
		sb.append("}");
		
		return sb.toString();
	}
	
	public static BeanRule newInstance(String id, String maskPattern, String className, String scope, Boolean singleton, String factoryBeanId, String factoryMethodName, String initMethodName, String destroyMethodName, Boolean lazyInit, Boolean important) {
		if(id == null)
			throw new IllegalArgumentException("The <bean> element requires a id attribute.");

		boolean factoryBeanReferenced;

		if(className == null && factoryBeanId != null) {
			factoryBeanReferenced = true;
		} else {
			factoryBeanReferenced = false;

			if(className == null)
				throw new IllegalArgumentException("The <bean> element requires a class attribute.");
		}

		ScopeType scopeType = ScopeType.valueOf(scope);

		if(scope != null && scopeType == null)
			throw new IllegalArgumentException("No scope-type registered for scope '" + scope + "'.");

		if(scopeType == null)
			scopeType = (singleton == Boolean.TRUE) ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;

		BeanRule beanRule = new BeanRule();

		if(factoryBeanReferenced) {
			beanRule.setId(id);
			beanRule.setScopeType(scopeType);
			beanRule.setSingleton(singleton);
			beanRule.setFactoryBeanId(factoryBeanId);
			beanRule.setFactoryMethodName(factoryMethodName);
			beanRule.setLazyInit(lazyInit);
			beanRule.setImportant(important);
			beanRule.setFactoryBeanReferenced(true);
		} else {
			beanRule.setId(id);
			beanRule.setMaskPattern(maskPattern);
			beanRule.setClassName(className);
			beanRule.setScopeType(scopeType);
			beanRule.setSingleton(singleton);
			beanRule.setFactoryMethodName(factoryMethodName);
			beanRule.setInitMethodName(initMethodName);
			beanRule.setDestroyMethodName(destroyMethodName);
			beanRule.setLazyInit(lazyInit);
			beanRule.setImportant(important);
		}

		return beanRule;
	}

	public static void checkFactoryBeanImplement(BeanRule beanRule) {
		Class<?> beanClass = beanRule.getBeanClass();

		if(FactoryBean.class.isAssignableFrom(beanClass)) {
			beanRule.setFactoryBeanImplmented(true);
		}
	}

	public static void checkAccessibleMethod(BeanRule beanRule) {
		if(!beanRule.isFactoryBeanReferenced()) {
			Class<?> beanClass = beanRule.getBeanClass();
			String factoryMethodName = beanRule.getFactoryMethodName();
			String initMethodName = beanRule.getInitMethodName();
			String destroyMethodName = beanRule.getDestroyMethodName();

			if(!beanRule.isFactoryBeanReferenced() && factoryMethodName != null) {
				if(MethodUtils.getAccessibleMethod(beanClass, factoryMethodName, null) == null) {
					throw new IllegalArgumentException("No such factory method '" + factoryMethodName + "() on bean class: " + beanClass);
				}
			}

			if(initMethodName == null) {
				if(InitializableTransletBean.class.isAssignableFrom(beanClass)) {
					initMethodName = InitializableBean.INITIALIZE_METHOD_NAME;
					beanRule.setInitMethodName(initMethodName);
					beanRule.setInitMethodRequiresTranslet(Boolean.TRUE);
				} else if (InitializableBean.class.isAssignableFrom(beanClass)) {
					initMethodName = InitializableBean.INITIALIZE_METHOD_NAME;
					beanRule.setInitMethodName(initMethodName);
				}
			} else {
				if(MethodUtils.getAccessibleMethod(beanClass, initMethodName, null) == null) {
					throw new IllegalArgumentException("No such initialization method '" + initMethodName + "() on bean class: " + beanClass);
				}
			}

			if(destroyMethodName == null) {
				if(DisposableBean.class.isAssignableFrom(beanClass)) {
					beanRule.setDestroyMethodName(DisposableBean.DESTROY_METHOD_NAME);
				}
			} else {
				if(MethodUtils.getAccessibleMethod(beanClass, destroyMethodName, null) == null) {
					throw new IllegalArgumentException("No such destroy method '" + destroyMethodName + "() on bean class: " + beanClass);
				}
			}
		}
	}

	public static void updateConstructorArgument(BeanRule beanRule, String text) {
		if(!beanRule.isFactoryBeanReferenced()) {
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
		if(!beanRule.isFactoryBeanReferenced()) {
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
