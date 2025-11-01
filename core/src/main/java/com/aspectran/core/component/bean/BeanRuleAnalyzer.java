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
package com.aspectran.core.component.bean;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.core.component.bean.annotation.Async;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.cache.Cache;
import com.aspectran.utils.cache.ConcurrentReferenceCache;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Analyzes {@link com.aspectran.core.context.rule.BeanRule} metadata to
 * determine target bean classes, lifecycle methods, and proxying hints.
 * <p>
 * Resolves factory targets, init/destroy methods, and collects
 * {@link com.aspectran.core.component.bean.annotation.Advisable} methods
 * with lightweight caching to aid container setup.
 * </p>
 */
public class BeanRuleAnalyzer {

    public static final Class<?>[] TRANSLET_ACTION_PARAMETER_TYPES = { Translet.class };

    private static final Cache<Class<?>, List<Method>> advisableMethodsCache =
            new ConcurrentReferenceCache<>(BeanRuleAnalyzer::resolveAdvisableMethods);

    @Nullable
    static Class<?> resolveBeanClass(@NonNull BeanRule beanRule) throws BeanRuleException {
        Class<?> targetBeanClass;
        if (beanRule.isFactoryOffered()) {
            targetBeanClass = beanRule.getFactoryBeanClass();
            if (targetBeanClass == null) {
                // (will be post-processing)
                return null;
            }
            targetBeanClass = resolveFactoryMethodTargetBeanClass(beanRule, targetBeanClass);
        } else {
            targetBeanClass = beanRule.getBeanClass();
        }
        if (targetBeanClass == null) {
            throw new BeanRuleException(beanRule);
        }
        if (beanRule.getInitMethodName() != null) {
            resolveInitMethod(beanRule, targetBeanClass);
        }
        if (beanRule.getDestroyMethodName() != null) {
            resolveDestroyMethod(beanRule, targetBeanClass);
        }
        if (!beanRule.isFactoryOffered()) {
            if (beanRule.isFactoryBean()) {
                targetBeanClass = resolveTargetBeanClassForFactoryBean(beanRule, targetBeanClass);
            } else if (beanRule.getFactoryMethodName() != null) {
                targetBeanClass = resolveFactoryMethodTargetBeanClass(beanRule, targetBeanClass);
            }
        }
        return targetBeanClass;
    }

    @NonNull
    private static Class<?> resolveTargetBeanClassForFactoryBean(BeanRule beanRule, Class<?> beanClass)
            throws BeanRuleException {
        try {
            Method m = MethodUtils.getAccessibleMethod(beanClass, FactoryBean.FACTORY_METHOD_NAME);
            Class<?> targetBeanClass = m.getReturnType();
            beanRule.setTargetBeanClass(targetBeanClass);
            return targetBeanClass;
        } catch (Exception e) {
            throw new BeanRuleException(beanRule, e);
        }
    }

    @NonNull
    static Class<?> resolveFactoryMethodTargetBeanClass(@NonNull BeanRule beanRule, @NonNull Class<?> factoryBeanClass)
            throws BeanRuleException {
        Class<?> targetBeanClass;
        String factoryMethodName;
        if (beanRule.getFactoryMethod() != null) {
            Method factoryMethod = beanRule.getFactoryMethod();
            targetBeanClass = factoryMethod.getReturnType();
            factoryMethodName = factoryMethod.getName();
        } else {
            factoryMethodName = beanRule.getFactoryMethodName();
            Method m1 = MethodUtils.getAccessibleMethod(factoryBeanClass, factoryMethodName, TRANSLET_ACTION_PARAMETER_TYPES);
            if (m1 != null) {
                beanRule.setFactoryMethod(m1);
                beanRule.setFactoryMethodParameterBindingRules(AnnotatedConfigParser.createParameterBindingRules(m1));
                targetBeanClass = m1.getReturnType();
            } else {
                Method m2 = MethodUtils.getAccessibleMethod(factoryBeanClass, factoryMethodName);
                if (m2 == null) {
                    throw new BeanRuleException("No such factory method " + factoryMethodName +
                            "() on bean class: " + factoryBeanClass.getName(), beanRule);
                }
                beanRule.setFactoryMethod(m2);
                beanRule.setFactoryMethodParameterBindingRules(AnnotatedConfigParser.createParameterBindingRules(m2));
                targetBeanClass = m2.getReturnType();
            }
        }
        if (targetBeanClass == void.class || targetBeanClass == Void.class) {
            throw new BeanRuleException("Factory method '" + factoryMethodName + "' on bean class [" +
                    factoryBeanClass.getName() + "] must not have a void return type", beanRule);
        }
        beanRule.setTargetBeanClass(targetBeanClass);
        return targetBeanClass;
    }

    public static void resolveInitMethod(@NonNull BeanRule beanRule, @NonNull Class<?> beanClass) throws BeanRuleException {
        if (beanRule.isInitializableBean()) {
            throw new BeanRuleException("Bean initialization method is duplicated; " +
                    "Already implemented the InitializableBean", beanRule);
        }

        String initMethodName = beanRule.getInitMethodName();
        Method initMethod = MethodUtils.getAccessibleMethod(beanClass, initMethodName, TRANSLET_ACTION_PARAMETER_TYPES);
        if (initMethod == null) {
            initMethod = MethodUtils.getAccessibleMethod(beanClass, initMethodName);
        }
        if (initMethod == null) {
            throw new BeanRuleException("No such initialization method " +
                    initMethodName + "() on bean class: " + beanClass.getName(), beanRule);
        }

        if (Modifier.isStatic(initMethod.getModifiers())) {
            throw new BeanRuleException("Initialization method '" + initMethodName + "' on bean class [" +
                    beanClass.getName() + "] must not be static", beanRule);
        }

        beanRule.setInitMethod(initMethod);
        beanRule.setInitMethodParameterBindingRules(AnnotatedConfigParser.createParameterBindingRules(initMethod));
    }

    public static void resolveDestroyMethod(@NonNull BeanRule beanRule, @NonNull Class<?> beanClass)
            throws BeanRuleException {
        if (beanRule.isDisposableBean()) {
            throw new BeanRuleException("Bean destroy method is duplicated; " +
                    "Already implemented the DisposableBean", beanRule);
        }

        String destroyMethodName = beanRule.getDestroyMethodName();
        Method m = MethodUtils.getAccessibleMethod(beanClass, destroyMethodName);
        if (m == null) {
            throw new BeanRuleException("No such destroy method " +
                    destroyMethodName + "() on bean class: " + beanClass.getName(), beanRule);
        }

        if (Modifier.isStatic(m.getModifiers())) {
            throw new BeanRuleException("Destroy method '" + destroyMethodName + "' on bean class [" +
                    beanClass.getName() + "] must not be static", beanRule);
        }

        beanRule.setDestroyMethod(m);
    }

    static void checkRequiredProperty(@NonNull BeanRule beanRule, @NonNull Method method) throws BeanRuleException {
        String propertyName = dropCase(method.getName());
        ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
        if (propertyItemRuleMap != null) {
            if (propertyItemRuleMap.containsKey(propertyName)) {
                return;
            }
        }
        throw new BeanRuleException("Property '" + propertyName + "' is required for bean ", beanRule);
    }

    @NonNull
    private static String dropCase(@NonNull String name) {
        if (name.startsWith("set")) {
            name = name.substring(3);
        }
        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
        }
        return name;
    }

    /**
     * Determines whether the bean should be proxied.
     * @param beanRule the bean rule
     */
    public static void determineProxyBean(@NonNull BeanRule beanRule) {
        if (beanRule.getProxied() == null) {
            if (!beanRule.isFactoryable() && !getAdvisableMethods(beanRule).isEmpty()) {
                beanRule.setProxied(true);
                return;
            }
            for (Method method : beanRule.getTargetBeanClass().getMethods()) {
                if (method.isAnnotationPresent(Async.class)) {
                    beanRule.setProxied(true);
                    return;
                }
            }
        }
    }

    /**
     * Returns a list of advisable methods for the given bean rule.
     * @param beanRule the bean rule
     * @return a list of advisable methods
     */
    public static List<Method> getAdvisableMethods(@NonNull BeanRule beanRule) {
        return advisableMethodsCache.get(beanRule.getTargetBeanClass());
    }

    @NonNull
    private static List<Method> resolveAdvisableMethods(@NonNull Class<?> beanClass) {
        List<Method> advisableMethods = new ArrayList<>();
        Class<?> currentClass = beanClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Advisable.class)) {
                    advisableMethods.add(method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        if (advisableMethods.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(advisableMethods);
        }
    }

    /**
     * Clears the advisable methods cache.
     */
    public static void clearAdvisableMethodsCache() {
        advisableMethodsCache.clear();
    }

}
