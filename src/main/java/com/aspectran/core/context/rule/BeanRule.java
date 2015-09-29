/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.rule;

import java.util.List;

import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.apon.Parameters;

/**
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public class BeanRule implements Cloneable {

	public static final String BEAN_ID_PATTERN_DELIMITER = "*";

	protected String id;

	protected String idPrefix;
	
	protected String idSuffix;
	
	protected String className;

	protected Class<?> beanClass;

	protected ScopeType scopeType;

	protected Boolean singleton;

	protected String factoryMethodName;
	
	protected String initMethodName;
	
	private Boolean initMethodRequiresTranslet;
	
	protected String destroyMethodName;

	protected Boolean lazyInit;

	protected Boolean important;

	protected ItemRuleMap constructorArgumentItemRuleMap;
	
	protected ItemRuleMap propertyItemRuleMap;
	
	private Object bean;
	
	private boolean registered;
	
	private boolean overrided;

	private boolean scanned;
	
	private boolean proxied;
	
	private String classScanFilterClassName;
	
	private Parameters filterParameters;
	
	/**
	 * Gets the bean's id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the bean's id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
		
		String[] splitted = splitBeanIdPattern(id);
		
		if(splitted != null) {
			this.idPrefix = splitted[0];
			this.idSuffix = splitted[1];
		}
	}
	
	/**
	 * Sets the bean's id.
	 *
	 * @param id the id
	 * @param idPrefix the prefix of bean's id
	 * @param idSuffix the suffix of bean's id
	 */
	public void setId(String id, String idPrefix, String idSuffix) {
		if(idPrefix != null && idSuffix != null) {
			this.id = idPrefix + id + idSuffix;
		} else if(idPrefix != null) {
			this.id = idPrefix +id;
		} else if(idSuffix != null) {
			this.id = id + idSuffix;
		} else {
			this.id = id;
		}
	}
	
	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	public String getIdSuffix() {
		return idSuffix;
	}

	public void setIdSuffix(String idSuffix) {
		this.idSuffix = idSuffix;
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

	public Boolean getSingleton() {
		return singleton;
	}

	public void setSingleton(Boolean singleton) {
		this.singleton = singleton;
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
	
	public Boolean getInitMethodRequiresTranslet() {
		return initMethodRequiresTranslet;
	}

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
	 * Checks if is lazy init.
	 *
	 * @return true, if is lazy init
	 */
	public Boolean getLazyInit() {
		return lazyInit;
	}

	public boolean isLazyInit() {
		return BooleanUtils.toBoolean(lazyInit);
	}

	/**
	 * Sets the lazy init.
	 *
	 * @param lazyInit the new lazy init
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
	
	public boolean isPatternedBeanId() {
		return (idPrefix != null || idSuffix != null || id.equals(BEAN_ID_PATTERN_DELIMITER));
	}
	
	public boolean isProxied() {
		return proxied;
	}

	public void setProxied(boolean proxied) {
		this.proxied = proxied;
	}

	public String getClassScanFilterClassName() {
		return classScanFilterClassName;
	}

	public void setClassScanFilterClassName(String classScanFilterClassName) {
		this.classScanFilterClassName = classScanFilterClassName;
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
		sb.append(", important=").append(important);
		sb.append(", proxied=").append(proxied);
		sb.append(", scanned=").append(scanned);
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
	
	public static BeanRule newInstance(String id, String className, String scope, Boolean singleton, String factoryMethod, String initMethodName, String destroyMethodName, Boolean lazyInit, Boolean important) {
		if(id == null)
			throw new IllegalArgumentException("The <bean> element requires a id attribute.");

		if(className == null)
			throw new IllegalArgumentException("The <bean> element requires a class attribute.");

		ScopeType scopeType = ScopeType.valueOf(scope);
		
		if(scope != null && scopeType == null)
			throw new IllegalArgumentException("No scope-type registered for scope '" + scope + "'.");
		
		if(scopeType == null)
			scopeType = (singleton == Boolean.TRUE) ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
		
		BeanRule beanRule = new BeanRule();
		beanRule.setId(id);
		beanRule.setClassName(className);
		beanRule.setScopeType(scopeType);
		beanRule.setSingleton(singleton);
		beanRule.setFactoryMethodName(factoryMethod);
		beanRule.setInitMethodName(initMethodName);
		beanRule.setDestroyMethodName(destroyMethodName);
		beanRule.setLazyInit(lazyInit);
		beanRule.setImportant(important);
		
		return beanRule;
	}
	
	public static String combineBeanIdPattern(String beanIdPrefix, String beanIdSuffix) {
		if(beanIdPrefix == null && beanIdSuffix != null) {
			return BEAN_ID_PATTERN_DELIMITER + beanIdSuffix;
		} else if(beanIdPrefix != null && beanIdSuffix == null) {
			return beanIdPrefix + BEAN_ID_PATTERN_DELIMITER;
		} else {
			return BEAN_ID_PATTERN_DELIMITER;
		}
	}
	
	public static String[] splitBeanIdPattern(String beanId) {
		int wildcardStartIndex = beanId.indexOf(BEAN_ID_PATTERN_DELIMITER);
		
		if(wildcardStartIndex == -1)
			return null;
		
		String beanIdPrefix = null;
		String beanIdSuffix = null;
		
		if(wildcardStartIndex > 0)
			beanIdPrefix = beanId.substring(0, wildcardStartIndex);
		
		if(wildcardStartIndex < beanId.length() - 1)
			beanIdSuffix = beanId.substring(wildcardStartIndex + 1);
		
		return new String[] { beanIdPrefix, beanIdSuffix };
	}
	
/*
	public static BeanRule[] newInstance2(ClassLoader classLoader, String id, String className, String scope, Boolean singleton, String factoryMethod, String initMethodName, String destroyMethodName, Boolean lazyInit, Boolean important) throws ClassNotFoundException, IOException {
		if(id == null)
			throw new IllegalArgumentException("The <bean> element requires a id attribute.");
		
		if(className == null)
			throw new IllegalArgumentException("The <bean> element requires a class attribute.");
		
		ScopeType scopeType = ScopeType.valueOf(scope);
		
		if(scope != null && scopeType == null)
			throw new IllegalArgumentException("No scope-type registered for scope '" + scope + "'.");
		
		if(scopeType == null)
			scopeType = singleton == Boolean.TRUE ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
		
		BeanRule[] beanRules = null;
		
		if(!WildcardPattern.hasWildcards(className)) {
			Class<?> beanClass = classLoader.loadClass(className);
			
			BeanRule beanRule = new BeanRule();
			beanRule.setId(id);
			beanRule.setClassName(className);
			beanRule.setBeanClass(beanClass);
			beanRule.setScopeType(scopeType);
			beanRule.setFactoryMethodName(factoryMethod);
			beanRule.setLazyInit(lazyInit);
			beanRule.setImportanct(important);
			
			updateAccessibleMethod(beanRule, beanClass, initMethodName, destroyMethodName);			
			
			beanRules = new BeanRule[] { beanRule };
		} else {
			BeanClassScanner scanner = new BeanClassScanner(id, classLoader);
			Map<String, Class<?>> beanClassMap = scanner.scanClass(className);
			
			if(beanClassMap != null && beanClassMap.size() > 0) {
				beanRules = new BeanRule[beanClassMap.size()];
				
				int i = 0;
				for(Map.Entry<String, Class<?>> entry : beanClassMap.entrySet()) {
					String beanId = entry.getKey();
					Class<?> beanClass2 = entry.getValue();
					
					BeanRule beanRule = new BeanRule();
					beanRule.setId(beanId);
					beanRule.setClassName(className);
					beanRule.setBeanClass(beanClass2);
					beanRule.setScopeType(scopeType);
					beanRule.setFactoryMethodName(factoryMethod);
					beanRule.setLazyInit(lazyInit);
					beanRule.setImportanct(important);
					beanRule.setStealthily(true);
					
					updateAccessibleMethod(beanRule, beanClass2, initMethodName, destroyMethodName);			
					
					beanRules[i++] = beanRule;
				}
			}
		}
		
		return beanRules;
	}
	
	private static void updateAccessibleMethod(BeanRule beanRule, Class<?> beanClass, String initMethodName, String destroyMethodName) {
		if(initMethodName == null && beanClass.isAssignableFrom(InitializableBean.class)) {
			initMethodName = InitializableBean.INITIALIZE_METHOD_NAME;
		}

		if(initMethodName != null) {
			if(MethodUtils.getAccessibleMethod(beanClass, initMethodName, null) == null) {
				throw new IllegalArgumentException("No such initialization method '" + initMethodName + "() on bean class: " + beanClass);
			}
		}
		
		if(destroyMethodName == null && beanClass.isAssignableFrom(DisposableBean.class)) {
			destroyMethodName = DisposableBean.DESTROY_METHOD_NAME;
		}

		if(destroyMethodName != null) {
			if(MethodUtils.getAccessibleMethod(beanClass, destroyMethodName, null) == null) {
				throw new IllegalArgumentException("No such destroy method '" + destroyMethodName + "() on bean class: " + beanClass);
			}
		}

		beanRule.setInitMethodName(initMethodName);
		beanRule.setDestroyMethodName(destroyMethodName);
	}
*/
	public static void checkAccessibleMethod(BeanRule beanRule) {
		Class<?> beanClass = beanRule.getBeanClass();
		String initMethodName = beanRule.getInitMethodName();
		String destroyMethodName = beanRule.getDestroyMethodName();
		
		if(initMethodName == null) {
			if(beanClass.isAssignableFrom(InitializableTransletBean.class)) {
				initMethodName = InitializableBean.INITIALIZE_METHOD_NAME;
				beanRule.setInitMethodName(initMethodName);
				beanRule.setInitMethodRequiresTranslet(Boolean.TRUE);
			} else if(beanClass.isAssignableFrom(InitializableBean.class)) {
				initMethodName = InitializableBean.INITIALIZE_METHOD_NAME;
				beanRule.setInitMethodName(initMethodName);
			}
		} else {
			if(MethodUtils.getAccessibleMethod(beanClass, initMethodName, null) == null) {
				throw new IllegalArgumentException("No such initialization method '" + initMethodName + "() on bean class: " + beanClass);
			}
		}
		
		if(destroyMethodName == null) {
			if(beanClass.isAssignableFrom(DisposableBean.class)) {
				beanRule.setDestroyMethodName(DisposableBean.DESTROY_METHOD_NAME);
			}
		} else {
			if(MethodUtils.getAccessibleMethod(beanClass, destroyMethodName, null) == null) {
				throw new IllegalArgumentException("No such destroy method '" + destroyMethodName + "() on bean class: " + beanClass);
			}
		}
	}

	public static void updateConstructorArgument(BeanRule beanRule, String text) {
		List<Parameters> argumentParametersList = ItemRule.toItemParametersList(text);
		
		if(argumentParametersList == null)
			return;
		
		ItemRuleMap constructorArgumentItemRuleMap = ItemRule.toItemRuleMap(argumentParametersList);
		
		if(constructorArgumentItemRuleMap == null)
			return;
		
		beanRule.setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
	}
	
	public static void updateProperty(BeanRule beanRule, String text) {
		List<Parameters> propertyParametersList = ItemRule.toItemParametersList(text);
		
		if(propertyParametersList == null)
			return;
		
		ItemRuleMap propertyItemRuleMap = ItemRule.toItemRuleMap(propertyParametersList);
		
		if(propertyItemRuleMap == null)
			return;
		
		beanRule.setPropertyItemRuleMap(propertyItemRuleMap);
	}
/*
	public static void updateConstructorArgument(BeanRule[] beanRules, List<Parameters> argumentParametersList) {
		ItemRuleMap itemRuleMap = ItemRule.toItemRuleMap(argumentParametersList);
		
		if(itemRuleMap == null)
			return;
		
		if(beanRules.length == 1) {
			beanRules[0].setConstructorArgumentItemRuleMap(itemRuleMap);
		} else if(beanRules.length > 1) {
			beanRules[0].setConstructorArgumentItemRuleMap(itemRuleMap);
			
			for(BeanRule beanRule : beanRules) {
				if(beanRule == beanRules[0])
					beanRule.setConstructorArgumentItemRuleMap(itemRuleMap);
				else
					beanRule.setConstructorArgumentItemRuleMap((ItemRuleMap)itemRuleMap.clone());
			}
		}
	}
	
	public static void updateProperty(BeanRule[] beanRules, List<Parameters> propertyParameterList) {
		ItemRuleMap itemRuleMap = ItemRule.toItemRuleMap(propertyParameterList);
		
		if(itemRuleMap == null)
			return;
		
		if(beanRules.length == 1) {
			beanRules[0].setPropertyItemRuleMap(itemRuleMap);
		} else if(beanRules.length > 1) {
			beanRules[0].setPropertyItemRuleMap(itemRuleMap);
			
			for(BeanRule beanRule : beanRules) {
				if(beanRule == beanRules[0])
					beanRule.setPropertyItemRuleMap(itemRuleMap);
				else
					beanRule.setPropertyItemRuleMap((ItemRuleMap)itemRuleMap.clone());
			}
		}
	}
*/
}
