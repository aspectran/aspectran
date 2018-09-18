/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.util.MethodUtils;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * The Class BeanRuleAnalyzer.
 */
public class BeanRuleAnalyzer {

    public static final Class<?>[] TRANSLET_ACTION_PARAMETER_TYPES = { Translet.class };

    public static Class<?> determineBeanClass(BeanRule beanRule) {
        Class<?> targetBeanClass;

        if (beanRule.isFactoryOffered()) {
            targetBeanClass = beanRule.getFactoryBeanClass();
            if (targetBeanClass == null) {
                // (will be post processing)
                return null;
            }
            targetBeanClass = determineFactoryMethodTargetBeanClass(targetBeanClass, beanRule);
        } else {
            targetBeanClass = beanRule.getBeanClass();
        }

        if (targetBeanClass == null) {
            throw new BeanRuleException("Invalid BeanRule", beanRule);
        }

        if (beanRule.getInitMethodName() != null) {
            checkInitMethod(targetBeanClass, beanRule);
        }

        if (beanRule.getDestroyMethodName() != null) {
            checkDestroyMethod(targetBeanClass, beanRule);
        }

        if (!beanRule.isFactoryOffered()) {
            if (beanRule.isFactoryBean()) {
                targetBeanClass = determineTargetBeanClassForFactoryBean(targetBeanClass, beanRule);
            } else if (beanRule.getFactoryMethodName() != null) {
                targetBeanClass = determineFactoryMethodTargetBeanClass(targetBeanClass, beanRule);
            }
        }

        return targetBeanClass;
    }

    public static Class<?> determineTargetBeanClassForFactoryBean(Class<?> beanClass, BeanRule beanRule) {
        try {
            Method m = MethodUtils.getAccessibleMethod(beanClass, FactoryBean.FACTORY_METHOD_NAME);
            Class<?> targetBeanClass = m.getReturnType();
            beanRule.setTargetBeanClass(targetBeanClass);
            return targetBeanClass;
        } catch (Exception e) {
            throw new BeanRuleException("Invalid BeanRule", beanRule);
        }
    }

    protected static Class<?> determineFactoryMethodTargetBeanClass(Class<?> beanClass, BeanRule beanRule) {
        String factoryMethodName = beanRule.getFactoryMethodName();

        Method m1 = MethodUtils.getAccessibleMethod(beanClass, factoryMethodName, TRANSLET_ACTION_PARAMETER_TYPES);
        Class<?> targetBeanClass;

        if (m1 != null) {
            beanRule.setFactoryMethod(m1);
            beanRule.setFactoryMethodRequiresTranslet(true);
            targetBeanClass = m1.getReturnType();
        } else {
            Method m2 = MethodUtils.getAccessibleMethod(beanClass, factoryMethodName);
            if (m2 == null) {
                throw new BeanRuleException("No such factory method " + factoryMethodName +
                        "() on bean class: " + beanClass.getName(), beanRule);
            }
            beanRule.setFactoryMethod(m2);
            targetBeanClass = m2.getReturnType();
        }

        beanRule.setTargetBeanClass(targetBeanClass);

        return targetBeanClass;
    }

    public static void checkInitMethod(Class<?> beanClass, BeanRule beanRule) {
        if (beanRule.isInitializableBean()) {
            throw new BeanRuleException("Bean initialization method is duplicated; " +
                    "Already implemented the InitializableBean", beanRule);
        }
        if (beanRule.isInitializableTransletBean()) {
            throw new BeanRuleException("Bean initialization method is duplicated; " +
                    "Already implemented the InitializableTransletBean", beanRule);
        }

        String initMethodName = beanRule.getInitMethodName();
        Method m1 = MethodUtils.getAccessibleMethod(beanClass, initMethodName, TRANSLET_ACTION_PARAMETER_TYPES);

        if (m1 != null) {
            beanRule.setInitMethod(m1);
            beanRule.setInitMethodRequiresTranslet(true);
        } else {
            Method m2 = MethodUtils.getAccessibleMethod(beanClass, initMethodName);
            if (m2 == null) {
                throw new BeanRuleException("No such initialization method " +
                        initMethodName + "() on bean class: " + beanClass.getName(), beanRule);
            }
            beanRule.setInitMethod(m2);
        }
    }

    public static void checkDestroyMethod(Class<?> beanClass, BeanRule beanRule) {
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
        beanRule.setDestroyMethod(m);
    }

    public static void checkRequiredProperty(BeanRule beanRule, Method method) {
        ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
        String propertyName = dropCase(method.getName());
        if (propertyItemRuleMap != null) {
            if (propertyItemRuleMap.containsKey(propertyName)) {
                return;
            }
        }
        throw new BeanRuleException("Property '" + propertyName + "' is required for bean ", beanRule);
    }

    private static String dropCase(String name) {
        if (name.startsWith("set")) {
            name = name.substring(3);
        }
        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
        }
        return name;
    }

}
