/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.var.rule;

import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.var.type.ScopeType;

/**
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public class BeanRule {

	protected String id;

	protected String className;

	protected Class<?> beanClass;

	protected ScopeType scopeType;

	protected String factoryMethodName;
	
	protected String initMethodName;
	
	protected String destroyMethodName;
	
	protected boolean lazyInit;

	protected ItemRuleMap constructorArgumentItemRuleMap;
	
	protected ItemRuleMap propertyItemRuleMap;
	
	private Object bean;
	
	private boolean registered;
	
	private boolean override;

	private boolean overrided;

	private boolean stealthily;
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
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
	 * Checks if is lazy init.
	 *
	 * @return true, if is lazy init
	 */
	public boolean isLazyInit() {
		return lazyInit;
	}

	/**
	 * Sets the lazy init.
	 *
	 * @param lazyInit the new lazy init
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
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

	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}
	
	public boolean isOverrided() {
		return overrided;
	}
	
	public void setOverrided(boolean overrided) {
		this.overrided = overrided;
	}

	public boolean isStealthily() {
		return stealthily;
	}

	public void setStealthily(boolean stealthily) {
		this.stealthily = stealthily;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{id=").append(id);
		sb.append(", class=").append(className);
		sb.append(", scope=").append(scopeType);
		sb.append(", factoryMethod=").append(factoryMethodName);
		sb.append(", initMethod=").append(initMethodName);
		sb.append(", destroyMethod=").append(destroyMethodName);
		sb.append(", lazyInit=").append(lazyInit);
		sb.append(", override=").append(override);

		if(constructorArgumentItemRuleMap != null) {
			sb.append(", constructorArguments=[");
			int sbLength = sb.length();

			for(String name : constructorArgumentItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}

			sb.append("]");
		}
		
		if(propertyItemRuleMap != null) {
			sb.append(", properties=[");
			int sbLength = sb.length();
			
			for(String name : propertyItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}
			
			sb.append("]");
		}
		
		sb.append("}");
		
		return sb.toString();
	}

	public static BeanRule newInstance(String id, Class<?> beanClass, String scope, boolean singleton, String factoryMethod, String initMethodName, String destroyMethodName, boolean lazyInit, boolean override) {
		if(id == null)
			throw new IllegalArgumentException("The <bean> element requires a id attribute.");
		
		String className = beanClass.getName();
		
		ScopeType scopeType = ScopeType.valueOf(scope);
		
		if(scope != null && scopeType == null)
			throw new IllegalArgumentException("No scope-type registered for scope '" + scope + "'.");
		
		if(scopeType == null)
			scopeType = singleton ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;

		if(initMethodName == null && beanClass.isAssignableFrom(InitializableBean.class)) {
			initMethodName = InitializableBean.INITIALIZE_METHOD_NAME;
		}

		if(initMethodName != null) {
			if(MethodUtils.getAccessibleMethod(beanClass, initMethodName, null) == null) {
				throw new IllegalArgumentException("No such initialization method '" + initMethodName + "() on bean class: " + className);
			}
		}
		
		if(destroyMethodName == null && beanClass.isAssignableFrom(DisposableBean.class)) {
			destroyMethodName = DisposableBean.DESTROY_METHOD_NAME;
		}

		if(destroyMethodName != null) {
			if(MethodUtils.getAccessibleMethod(beanClass, destroyMethodName, null) == null) {
				throw new IllegalArgumentException("No such destroy method '" + destroyMethodName + "() on bean class: " + className);
			}
		}

		BeanRule beanRule = new BeanRule();
		beanRule.setId(id);
		beanRule.setClassName(className);
		beanRule.setBeanClass(beanClass);
		beanRule.setScopeType(scopeType);
		beanRule.setFactoryMethodName(factoryMethod);
		beanRule.setInitMethodName(initMethodName);
		beanRule.setDestroyMethodName(destroyMethodName);
		beanRule.setLazyInit(lazyInit);
		beanRule.setOverride(override);
		
		return beanRule;
	}
	
}
