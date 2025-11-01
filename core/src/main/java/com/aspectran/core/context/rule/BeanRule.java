/*
 * Copyright (c) 2008-present The Aspectran Project
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

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.core.context.rule.ability.HasArguments;
import com.aspectran.core.context.rule.ability.HasProperties;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a bean to be managed by the Aspectran IoC container.
 * This rule specifies the bean's identity (ID), its class, scope, lifecycle methods (init/destroy),
 * and configuration for constructor arguments and properties.
 * It serves as the blueprint for creating and wiring application components.
 *
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public class BeanRule implements Replicable<BeanRule>, BeanReferenceable, Describable, HasArguments, HasProperties {

    public static final String CLASS_DIRECTIVE_PREFIX = "class:";

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.BEAN_RULE;

    private String id;

    private String className;

    private Class<?> beanClass;

    private String scanPattern;

    private String maskPattern;

    private FilterParameters filterParameters;

    private ScopeType scopeType;

    private Boolean singleton;

    private String factoryBeanId;

    private Class<?> factoryBeanClass;

    private String factoryMethodName;

    private Method factoryMethod;

    private ParameterBindingRule[] factoryMethodParameterBindingRules;

    private boolean factoryOffered;

    private Class<?> targetBeanClass;

    private String initMethodName;

    private Method initMethod;

    private ParameterBindingRule[] initMethodParameterBindingRules;

    private String destroyMethodName;

    private Method destroyMethod;

    private ItemRuleMap argumentItemRuleMap;

    private ItemRuleMap propertyItemRuleMap;

    private Boolean lazyInit;

    private Boolean lazyDestroy;

    private Boolean important;

    private boolean factoryBean;

    private boolean disposableBean;

    private boolean initializableBean;

    private boolean innerBean;

    private Boolean proxied;

    private List<AutowireRule> autowireRuleList;

    private AutowireRule constructorAutowireRule;

    private boolean constructorAutowireParsed;

    private boolean fieldAutowireParsed;

    private boolean methodAutowireParsed;

    private DescriptionRule descriptionRule;

    /**
     * Returns the bean id.
     * @return the bean id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the bean id.
     * @param id the bean id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the class name of the bean.
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the class name of the bean.
     * @param className the new class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets the bean class.
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the bean class and determines if it is a FactoryBean, DisposableBean, or InitializableBean.
     * @param beanClass the new bean class
     */
    public void setBeanClass(@NonNull Class<?> beanClass) {
        this.beanClass = beanClass;
        this.className = beanClass.getName();
        this.factoryBean = FactoryBean.class.isAssignableFrom(beanClass);
        this.disposableBean = DisposableBean.class.isAssignableFrom(beanClass);
        this.initializableBean = InitializableBean.class.isAssignableFrom(beanClass);
    }

    /**
     * Gets the scan pattern for component scanning.
     * @return the scan pattern
     */
    public String getScanPattern() {
        return scanPattern;
    }

    /**
     * Sets the scan pattern for component scanning.
     * @param scanPattern the new scan pattern
     */
    public void setScanPattern(String scanPattern) {
        this.scanPattern = scanPattern;
    }

    /**
     * Gets the mask pattern to exclude from component scanning.
     * @return the mask pattern
     */
    public String getMaskPattern() {
        return maskPattern;
    }

    /**
     * Sets the mask pattern to exclude from component scanning.
     * @param maskPattern the new mask pattern
     */
    public void setMaskPattern(String maskPattern) {
        this.maskPattern = maskPattern;
    }

    /**
     * Gets the filter parameters for component scanning.
     * @return the filter parameters
     */
    public FilterParameters getFilterParameters() {
        return filterParameters;
    }

    /**
     * Sets the filter parameters for component scanning.
     * @param filterParameters the new filter parameters
     */
    public void setFilterParameters(FilterParameters filterParameters) {
        this.filterParameters = filterParameters;
    }

    /**
     * Gets the scope of the bean.
     * @return the scope type
     */
    public ScopeType getScopeType() {
        return scopeType;
    }

    /**
     * Sets the scope of the bean.
     * @param scopeType the new scope type
     */
    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    /**
     * Returns whether this bean is a singleton (alternative to scope).
     * @return whether this bean is a singleton
     */
    public Boolean getSingleton() {
        return singleton;
    }

    /**
     * Sets whether this bean is a singleton.
     * @param singleton whether this bean is a singleton
     */
    public void setSingleton(Boolean singleton) {
        this.singleton = singleton;
    }

    /**
     * Returns whether this bean is a singleton.
     * @return true if the scope is singleton, false otherwise
     */
    public boolean isSingleton() {
        return (this.scopeType == ScopeType.SINGLETON);
    }

    /**
     * Gets the factory bean id.
     * @return the factory bean id
     */
    public String getFactoryBeanId() {
        return factoryBeanId;
    }

    /**
     * Sets the factory bean id.
     * @param factoryBeanId the new factory bean id
     */
    public void setFactoryBeanId(String factoryBeanId) {
        this.factoryBeanId = factoryBeanId;
    }

    /**
     * Gets factory bean class.
     * @return the factory bean class
     */
    public Class<?> getFactoryBeanClass() {
        return factoryBeanClass;
    }

    /**
     * Sets factory bean class.
     * @param factoryBeanClass the factory bean class
     */
    public void setFactoryBeanClass(Class<?> factoryBeanClass) {
        this.factoryBeanClass = factoryBeanClass;
    }

    /**
     * Gets the factory method name.
     * @return the factory method name
     */
    public String getFactoryMethodName() {
        return factoryMethodName;
    }

    /**
     * Sets the factory method name.
     * @param factoryMethodName the new factory method name
     */
    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    /**
     * Gets the resolved factory method.
     * @return the factory method
     */
    public Method getFactoryMethod() {
        return factoryMethod;
    }

    /**
     * Sets the resolved factory method.
     * @param factoryMethod the factory method
     */
    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    /**
     * Gets the parameter binding rules for the factory method.
     * @return the parameter binding rules
     */
    public ParameterBindingRule[] getFactoryMethodParameterBindingRules() {
        return factoryMethodParameterBindingRules;
    }

    /**
     * Sets the parameter binding rules for the factory method.
     * @param parameterBindingRules the parameter binding rules
     */
    public void setFactoryMethodParameterBindingRules(ParameterBindingRule[] parameterBindingRules) {
        this.factoryMethodParameterBindingRules = parameterBindingRules;
    }

    /**
     * Returns whether this bean is created by an external factory bean.
     * @return true if created by a factory, false otherwise
     */
    public boolean isFactoryOffered() {
        return factoryOffered;
    }

    /**
     * Sets whether this bean is created by an external factory bean.
     * @param factoryOffered true if created by a factory
     */
    public void setFactoryOffered(boolean factoryOffered) {
        this.factoryOffered = factoryOffered;
    }

    /**
     * Returns whether this bean requires production via a factory (either FactoryBean or factory-method).
     * @return true if a factory is required
     */
    public boolean isFactoryProductionRequired() {
        return (!isFactoryOffered() && (isFactoryBean() || getFactoryMethod() != null));
    }

    /**
     * Gets the target bean class, which may be different from the bean class if a factory is used.
     * @return the target bean class
     */
    public Class<?> getTargetBeanClass() {
        return (targetBeanClass != null ?  targetBeanClass : beanClass);
    }

    /**
     * Sets the target bean class.
     * @param targetBeanClass the target bean class
     */
    public void setTargetBeanClass(Class<?> targetBeanClass) {
        this.targetBeanClass = targetBeanClass;
    }

    /**
     * Gets the class name of the target bean.
     * @return the target bean class name
     */
    public String getTargetBeanClassName() {
        return (targetBeanClass != null ? targetBeanClass.getName() : className);
    }

    /**
     * Returns the initialization method name.
     * @return the initialization method name
     */
    public String getInitMethodName() {
        return initMethodName;
    }

    /**
     * Sets the initialization method name.
     * @param initMethodName the new initialization method name
     */
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    /**
     * Returns the resolved initialization method.
     * @return the initialization method
     */
    public Method getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the resolved initialization method.
     * @param initMethod the initialization method
     */
    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Gets the parameter binding rules for the init method.
     * @return the parameter binding rules
     */
    public ParameterBindingRule[] getInitMethodParameterBindingRules() {
        return initMethodParameterBindingRules;
    }

    /**
     * Sets the parameter binding rules for the init method.
     * @param parameterBindingRules the parameter binding rules
     */
    public void setInitMethodParameterBindingRules(ParameterBindingRule[] parameterBindingRules) {
        this.initMethodParameterBindingRules = parameterBindingRules;
    }

    /**
     * Returns the destroy method name.
     * @return the destroy method name
     */
    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    /**
     * Sets the destroy method name.
     * @param destroyMethodName the new destroy method name
     */
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    /**
     * Returns the resolved destroy method.
     * @return the destroy method
     */
    public Method getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the resolved destroy method.
     * @param destroyMethod the new destroy method
     */
    public void setDestroyMethod(Method destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    /**
     * Returns whether this bean is to be lazily initialized.
     * @return true, if this bean is to be lazily initialized
     */
    public Boolean getLazyInit() {
        return lazyInit;
    }

    /**
     * Sets whether this bean is to be lazily initialized.
     * @param lazyInit whether this bean is to be lazily initialized
     */
    public void setLazyInit(Boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    /**
     * Returns whether this bean is to be lazily initialized.
     * @return true, if this bean is to be lazily initialized
     */
    public boolean isLazyInit() {
        return BooleanUtils.toBoolean(lazyInit);
    }

    /**
     * Returns whether this bean is to be lazily destroyed.
     * @return true, if this bean is to be lazily destroyed
     */
    public Boolean getLazyDestroy() {
        return lazyDestroy;
    }

    /**
     * Sets whether this bean is to be lazily destroyed.
     * @param lazyDestroy whether this bean is to be lazily destroyed
     */
    public void setLazyDestroy(Boolean lazyDestroy) {
        this.lazyDestroy = lazyDestroy;
    }

    /**
     * Returns whether this bean is to be lazily destroyed
     * @return true, if this bean is to be lazily destroyed
     */
    public boolean isLazyDestroy() {
        return BooleanUtils.toBoolean(lazyDestroy);
    }

    /**
     * Returns whether this bean is important.
     * @return whether this bean is important
     */
    public Boolean getImportant() {
        return important;
    }

    /**
     * Sets whether this bean is important.
     * If specified as an important bean, it cannot be overridden
     * by another bean rule with the same name.
     * @param important whether important bean
     */
    public void setImportant(Boolean important) {
        this.important = important;
    }

    /**
     * Returns whether this bean is important.
     * @return whether this bean is important
     */
    public boolean isImportant() {
        return BooleanUtils.toBoolean(important);
    }

    @Override
    public ItemRuleMap getArgumentItemRuleMap() {
        return argumentItemRuleMap;
    }

    @Override
    public void setArgumentItemRuleMap(ItemRuleMap constructorArgumentItemRuleMap) {
        this.argumentItemRuleMap = constructorArgumentItemRuleMap;
    }

    @Override
    public void addArgumentItemRule(ItemRule constructorArgumentItemRule) {
        if (argumentItemRuleMap == null) {
            argumentItemRuleMap = new ItemRuleMap();
        }
        argumentItemRuleMap.putItemRule(constructorArgumentItemRule);
    }

    @Override
    public ItemRule newArgumentItemRule(String argumentName) {
        throw new UnsupportedOperationException("Argument name not supported");
    }

    /**
     * Adds a new constructor argument item rule and returns it.
     * @return the constructor argument item rule
     */
    public ItemRule newArgumentItemRule() {
        ItemRule itemRule = new ItemRule();
        itemRule.setAutoNamed(true);
        addArgumentItemRule(itemRule);
        return itemRule;
    }

    @Override
    public ItemRuleMap getPropertyItemRuleMap() {
        return propertyItemRuleMap;
    }

    @Override
    public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        this.propertyItemRuleMap = propertyItemRuleMap;
    }

    @Override
    public void addPropertyItemRule(ItemRule propertyItemRule) {
        if (propertyItemRuleMap == null) {
            propertyItemRuleMap = new ItemRuleMap();
        }
        propertyItemRuleMap.putItemRule(propertyItemRule);
    }

    /**
     * Returns whether this bean implements FactoryBean.
     * @return true if the bean class implements FactoryBean
     */
    public boolean isFactoryBean() {
        return factoryBean;
    }

    /**
     * Returns whether this bean implements DisposableBean.
     * @return true if the bean class implements DisposableBean
     */
    public boolean isDisposableBean() {
        return disposableBean;
    }

    /**
     * Returns whether this bean implements InitializableBean.
     * @return true if the bean class implements InitializableBean
     */
    public boolean isInitializableBean() {
        return initializableBean;
    }

    /**
     * Returns whether this is an inner bean.
     * @return true if this is an inner bean
     */
    public boolean isInnerBean() {
        return innerBean;
    }

    /**
     * Sets whether this is an inner bean.
     * @param innerBean true if this is an inner bean
     */
    public void setInnerBean(boolean innerBean) {
        this.innerBean = innerBean;
    }

    /**
     * Returns whether this bean is proxied.
     * @return true if this bean is proxied; false otherwise
     */
    public boolean isProxied() {
        return BooleanUtils.toBoolean(proxied);
    }

    /**
     * Gets the proxied flag.
     * @return the proxied flag
     */
    public Boolean getProxied() {
        return proxied;
    }

    /**
     * Sets whether this bean is proxied.
     * @param proxied true, if this bean is proxied
     */
    public void setProxied(Boolean proxied) {
        this.proxied = proxied;
    }

    /**
     * Returns whether this bean can be created by a factory.
     * @return true if this bean can be created by a factory; false otherwise
     */
    public boolean isFactoryable() {
        return (factoryOffered || factoryBean || factoryMethod != null);
    }

    /**
     * Gets the list of autowire rules for this bean.
     * @return the list of autowire rules
     */
    public List<AutowireRule> getAutowireRuleList() {
        return autowireRuleList;
    }

    /**
     * Adds an autowire rule to this bean.
     * @param autowireRule the autowire rule to add
     */
    public void addAutowireRule(AutowireRule autowireRule) {
        if (autowireRuleList == null) {
            autowireRuleList = new ArrayList<>();
        }
        autowireRuleList.add(autowireRule);
    }

    /**
     * Gets the autowire rule for the constructor.
     * @return the constructor autowire rule
     */
    public AutowireRule getConstructorAutowireRule() {
        return constructorAutowireRule;
    }

    /**
     * Sets the autowire rule for the constructor.
     * @param constructorAutowireRule the constructor autowire rule
     */
    public void setConstructorAutowireRule(AutowireRule constructorAutowireRule) {
        this.constructorAutowireRule = constructorAutowireRule;
    }

    /**
     * Returns whether constructor autowiring has been parsed.
     * @return true if parsed, false otherwise
     */
    public boolean isConstructorAutowireParsed() {
        return constructorAutowireParsed;
    }

    /**
     * Sets whether constructor autowiring has been parsed.
     * @param constructorAutowireParsed true if parsed
     */
    public void setConstructorAutowireParsed(boolean constructorAutowireParsed) {
        this.constructorAutowireParsed = constructorAutowireParsed;
    }

    /**
     * Returns whether field autowiring has been parsed.
     * @return true if parsed, false otherwise
     */
    public boolean isFieldAutowireParsed() {
        return fieldAutowireParsed;
    }

    /**
     * Sets whether field autowiring has been parsed.
     * @param fieldAutowireParsed true if parsed
     */
    public void setFieldAutowireParsed(boolean fieldAutowireParsed) {
        this.fieldAutowireParsed = fieldAutowireParsed;
    }

    /**
     * Returns whether method autowiring has been parsed.
     * @return true if parsed, false otherwise
     */
    public boolean isMethodAutowireParsed() {
        return methodAutowireParsed;
    }

    /**
     * Sets whether method autowiring has been parsed.
     * @param methodAutowireParsed true if parsed
     */
    public void setMethodAutowireParsed(boolean methodAutowireParsed) {
        this.methodAutowireParsed = methodAutowireParsed;
    }

    @Override
    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    @Override
    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

    @Override
    public BeanRule replicate() {
        return replicate(this);
    }

    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", id);
        if (!factoryOffered) {
            tsb.append("class", className);
            tsb.append("scope", scopeType);
            tsb.append("initMethod", initMethodName);
            tsb.append("destroyMethod", destroyMethodName);
            tsb.append("factoryMethod", factoryMethodName);
            tsb.append("initializableBean", initializableBean);
            tsb.append("disposableBean", disposableBean);
            tsb.append("factoryBean", factoryBean);
            tsb.append("lazyInit", lazyInit);
            tsb.append("lazyDestroy", lazyDestroy);
            tsb.append("important", important);
            tsb.append("proxied", proxied);
            if (argumentItemRuleMap != null) {
                tsb.append("constructorArguments", argumentItemRuleMap.keySet());
            }
            if (propertyItemRuleMap != null) {
                tsb.append("properties", propertyItemRuleMap.keySet());
            }
        } else {
            tsb.append("scope", scopeType);
            if (factoryBeanId != null) {
                tsb.append("factoryBean", factoryBeanId);
            } else if (factoryBeanClass != null) {
                tsb.append("factoryBean", CLASS_DIRECTIVE_PREFIX + factoryBeanClass.getName());
            }
            tsb.append("factoryMethod", factoryMethodName);
            tsb.append("initMethod", initMethodName);
            tsb.append("destroyMethod", destroyMethodName);
            tsb.append("lazyInit", lazyInit);
            tsb.append("lazyDestroy", lazyDestroy);
            tsb.append("important", important);
            tsb.append("proxied", proxied);
        }
        tsb.append("factoryOffered", factoryOffered);
        return tsb.toString();
    }

    /**
     * Creates a new instance of BeanRule.
     * @param id the bean ID
     * @param className the bean class name
     * @param scanPattern the package pattern to scan for components
     * @param maskPattern the pattern to exclude from scanning
     * @param initMethodName the name of the initialization method
     * @param destroyMethodName the name of the destruction method
     * @param factoryMethodName the name of the factory method
     * @param scope the bean scope
     * @param singleton whether the bean is a singleton
     * @param lazyInit whether to initialize lazily
     * @param lazyDestroy whether to destroy lazily
     * @param important whether this bean is important and cannot be overridden
     * @return a new BeanRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
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
            Boolean lazyDestroy,
            Boolean important) throws IllegalRuleException {
        if (className == null && scanPattern == null) {
            throw new IllegalRuleException("The 'bean' element requires a 'class' attribute");
        }

        ScopeType scopeType = ScopeType.resolve(scope);
        if (scope != null && scopeType == null) {
            throw new IllegalRuleException("No scope type for '" + scope + "'");
        }
        if (scopeType == null) {
            scopeType = (singleton == null || singleton ? ScopeType.SINGLETON : ScopeType.PROTOTYPE);
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
        beanRule.setLazyDestroy(lazyDestroy);
        beanRule.setImportant(important);
        return beanRule;
    }

    /**
     * Creates a new instance of BeanRule for a bean produced by a factory.
     * @param id the bean ID
     * @param factoryBeanId the ID of the factory bean
     * @param factoryMethodName the name of the factory method
     * @param initMethodName the name of the initialization method
     * @param destroyMethodName the name of the destruction method
     * @param scope the bean scope
     * @param singleton whether the bean is a singleton
     * @param lazyInit whether to initialize lazily
     * @param lazyDestroy whether to destroy lazily
     * @param important whether this bean is important
     * @return a new BeanRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static BeanRule newByFactoryMethod(
            String id,
            String factoryBeanId,
            String factoryMethodName,
            String initMethodName,
            String destroyMethodName,
            String scope,
            Boolean singleton,
            Boolean lazyInit,
            Boolean lazyDestroy,
            Boolean important) throws IllegalRuleException {
        if (factoryBeanId == null || factoryMethodName == null) {
            throw new IllegalRuleException("The 'bean' element requires both 'factoryBean' attribute and 'factoryMethod' attribute");
        }

        ScopeType scopeType = ScopeType.resolve(scope);
        if (scope != null && scopeType == null) {
            throw new IllegalRuleException("No scope type for '" + scope + "'");
        }
        if (scopeType == null) {
            scopeType = (singleton == null || singleton) ? ScopeType.SINGLETON : ScopeType.PROTOTYPE;
        }

        BeanRule beanRule = new BeanRule();
        beanRule.setId(id);
        beanRule.setScopeType(scopeType);
        beanRule.setSingleton(singleton);
        beanRule.setFactoryBeanId(factoryBeanId);
        beanRule.setFactoryMethodName(factoryMethodName);
        beanRule.setFactoryOffered(true);
        beanRule.setInitMethodName(initMethodName);
        beanRule.setDestroyMethodName(destroyMethodName);
        beanRule.setLazyInit(lazyInit);
        beanRule.setLazyDestroy(lazyDestroy);
        beanRule.setImportant(important);
        return beanRule;
    }

    /**
     * Creates a new instance of BeanRule for an inner bean.
     * @param className the bean class name
     * @param initMethodName the name of the initialization method
     * @param destroyMethodName the name of the destruction method
     * @param factoryMethodName the name of the factory method
     * @return a new BeanRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static BeanRule newInnerInstance(
            String className,
            String initMethodName,
            String destroyMethodName,
            String factoryMethodName) throws IllegalRuleException {
        if (StringUtils.hasText(destroyMethodName)) {
            throw new IllegalRuleException("Inner beans does not support destroy methods");
        }
        BeanRule beanRule = newInstance(null, className, null, null,
                initMethodName, destroyMethodName, factoryMethodName,
                null, false, null, null,null);
        beanRule.setInnerBean(true);
        return beanRule;
    }

    /**
     * Creates a new instance of BeanRule for an inner bean produced by a factory.
     * @param factoryBeanId the ID of the factory bean
     * @param factoryMethodName the name of the factory method
     * @param initMethodName the name of the initialization method
     * @param destroyMethodName the name of the destruction method
     * @return a new BeanRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static BeanRule newInnerByFactoryMethod(
            String factoryBeanId,
            String factoryMethodName,
            String initMethodName,
            String destroyMethodName) throws IllegalRuleException {
        if (StringUtils.hasText(destroyMethodName)) {
            throw new IllegalRuleException("Inner beans does not support destroy methods");
        }
        BeanRule beanRule = newByFactoryMethod(null, factoryBeanId, factoryMethodName,
                initMethodName, destroyMethodName, null, false, null, null,null);
        beanRule.setInnerBean(true);
        return beanRule;
    }

    /**
     * Creates a replica of the given BeanRule.
     * @param beanRule the bean rule to replicate
     * @return a new, replicated instance of BeanRule
     */
    @NonNull
    public static BeanRule replicate(@NonNull BeanRule beanRule) {
        BeanRule newBeanRule = new BeanRule();
        newBeanRule.setId(beanRule.getId());
        if (beanRule.getScanPattern() == null) {
            newBeanRule.setBeanClass(beanRule.getBeanClass());
        }
        newBeanRule.setScopeType(beanRule.getScopeType());
        newBeanRule.setSingleton(beanRule.getSingleton());
        newBeanRule.setFactoryBeanId(beanRule.getFactoryBeanId());
        newBeanRule.setFactoryMethodName(beanRule.getFactoryMethodName());
        newBeanRule.setInitMethodName(beanRule.getInitMethodName());
        newBeanRule.setDestroyMethodName(beanRule.getDestroyMethodName());
        newBeanRule.setArgumentItemRuleMap(beanRule.getArgumentItemRuleMap());
        newBeanRule.setPropertyItemRuleMap(beanRule.getPropertyItemRuleMap());
        newBeanRule.setLazyInit(beanRule.getLazyInit());
        newBeanRule.setLazyDestroy(beanRule.getLazyDestroy());
        newBeanRule.setImportant(beanRule.getImportant());
        newBeanRule.setDescriptionRule(beanRule.getDescriptionRule());
        newBeanRule.setInnerBean(beanRule.isInnerBean());
        newBeanRule.setProxied(beanRule.getProxied());
        return newBeanRule;
    }

}
