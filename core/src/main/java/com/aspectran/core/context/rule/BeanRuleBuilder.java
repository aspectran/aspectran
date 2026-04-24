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

import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;

/**
 * Builder for {@link BeanRule}.
 *
 * <p>Created: 2026. 04. 24</p>
 */
public final class BeanRuleBuilder {

    private String id;

    private String className;

    private String scanPattern;

    private String maskPattern;

    private String initMethodName;

    private String destroyMethodName;

    private String factoryBeanId;

    private String factoryMethodName;

    private String scope;

    private Boolean singleton;

    private Boolean lazyInit;

    private Boolean lazyDestroy;

    private Boolean important;

    private String[] dependsOn;

    private boolean innerBean;

    public BeanRuleBuilder() {
    }

    /**
     * Sets the bean ID.
     * @param id the bean ID
     * @return the builder instance
     */
    public BeanRuleBuilder id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the bean class name.
     * @param className the bean class name
     * @return the builder instance
     */
    public BeanRuleBuilder className(String className) {
        this.className = className;
        return this;
    }

    /**
     * Sets the scan pattern.
     * @param scanPattern the scan pattern
     * @return the builder instance
     */
    public BeanRuleBuilder scanPattern(String scanPattern) {
        this.scanPattern = scanPattern;
        return this;
    }

    /**
     * Sets the mask pattern.
     * @param maskPattern the mask pattern
     * @return the builder instance
     */
    public BeanRuleBuilder maskPattern(String maskPattern) {
        this.maskPattern = maskPattern;
        return this;
    }

    /**
     * Sets the initialization method name.
     * @param initMethodName the initialization method name
     * @return the builder instance
     */
    public BeanRuleBuilder initMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
        return this;
    }

    /**
     * Sets the destruction method name.
     * @param destroyMethodName the destruction method name
     * @return the builder instance
     */
    public BeanRuleBuilder destroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
        return this;
    }

    /**
     * Sets the factory bean ID.
     * @param factoryBeanId the factory bean ID
     * @return the builder instance
     */
    public BeanRuleBuilder factoryBeanId(String factoryBeanId) {
        this.factoryBeanId = factoryBeanId;
        return this;
    }

    /**
     * Sets the factory method name.
     * @param factoryMethodName the factory method name
     * @return the builder instance
     */
    public BeanRuleBuilder factoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
        return this;
    }

    /**
     * Sets the bean scope.
     * @param scope the bean scope
     * @return the builder instance
     */
    public BeanRuleBuilder scope(String scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Sets whether the bean is a singleton.
     * @param singleton whether the bean is a singleton
     * @return the builder instance
     */
    public BeanRuleBuilder singleton(Boolean singleton) {
        this.singleton = singleton;
        return this;
    }

    /**
     * Sets whether to initialize lazily.
     * @param lazyInit whether to initialize lazily
     * @return the builder instance
     */
    public BeanRuleBuilder lazyInit(Boolean lazyInit) {
        this.lazyInit = lazyInit;
        return this;
    }

    /**
     * Sets whether to destroy lazily.
     * @param lazyDestroy whether to destroy lazily
     * @return the builder instance
     */
    public BeanRuleBuilder lazyDestroy(Boolean lazyDestroy) {
        this.lazyDestroy = lazyDestroy;
        return this;
    }

    /**
     * Sets whether this bean is important.
     * @param important whether this bean is important
     * @return the builder instance
     */
    public BeanRuleBuilder important(Boolean important) {
        this.important = important;
        return this;
    }

    /**
     * Sets the names of the beans that this bean depends on.
     * @param dependsOn the names of the beans that this bean depends on
     * @return the builder instance
     */
    public BeanRuleBuilder dependsOn(String[] dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }

    /**
     * Sets the names of the beans that this bean depends on.
     * @param dependsOn a comma-delimited string of bean names
     * @return the builder instance
     */
    public BeanRuleBuilder dependsOn(String dependsOn) {
        if (StringUtils.hasText(dependsOn)) {
            this.dependsOn = StringUtils.splitWithComma(dependsOn);
        } else {
            this.dependsOn = null;
        }
        return this;
    }

    /**
     * Sets whether the bean is an inner bean.
     * @param innerBean whether the bean is an inner bean
     * @return the builder instance
     */
    public BeanRuleBuilder innerBean(boolean innerBean) {
        this.innerBean = innerBean;
        return this;
    }

    /**
     * Builds a new BeanRule instance.
     * @return the new BeanRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public BeanRule build() throws IllegalRuleException {
        ScopeType scopeType = ScopeType.resolve(scope);
        if (scope != null && scopeType == null) {
            throw new IllegalRuleException("No scope type for '" + scope + "'");
        }
        if (scopeType == null) {
            scopeType = (singleton == null || singleton ? ScopeType.SINGLETON : ScopeType.PROTOTYPE);
        }

        if (className == null && scanPattern == null && factoryBeanId == null) {
            throw new IllegalRuleException("A bean definition must specify 'class', 'scan', or 'factoryBean'");
        }
        if (factoryBeanId != null && factoryMethodName == null) {
            throw new IllegalRuleException("A 'factoryMethod' is required when 'factoryBean' is specified");
        }
        if (innerBean) {
            if (StringUtils.hasText(destroyMethodName)) {
                throw new IllegalRuleException("Inner beans does not support destroy methods");
            }
            if (id != null) {
                throw new IllegalRuleException("Inner beans cannot have an ID");
            }
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
        beanRule.setFactoryBeanId(factoryBeanId);
        beanRule.setFactoryMethodName(factoryMethodName);
        beanRule.setLazyInit(lazyInit);
        beanRule.setLazyDestroy(lazyDestroy);
        beanRule.setImportant(important);
        beanRule.setDependsOn(dependsOn);
        beanRule.setInnerBean(innerBean);

        if (className == null && scanPattern == null && factoryBeanId != null) {
            beanRule.setFactoryOffered(true);
        }
        return beanRule;
    }

}
