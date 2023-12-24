/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.utils.MethodUtils;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * The Class BeanRuleAnalyzer.
 */
public class BeanRuleAnalyzer {

    public static final Class<?>[] TRANSLET_ACTION_PARAMETER_TYPES = { Translet.class };

    static Class<?> determineBeanClass(BeanRule beanRule) throws BeanRuleException {
        Class<?> targetBeanClass;
        if (beanRule.isFactoryOffered()) {
            targetBeanClass = beanRule.getFactoryBeanClass();
            if (targetBeanClass == null) {
                // (will be post-processing)
                return null;
            }
            targetBeanClass = determineFactoryMethodTargetBeanClass(targetBeanClass, beanRule);
        } else {
            targetBeanClass = beanRule.getBeanClass();
        }
        if (targetBeanClass == null) {
            throw new BeanRuleException(beanRule);
        }
        if (beanRule.getInitMethodName() != null) {
            determineInitMethod(targetBeanClass, beanRule);
        }
        if (beanRule.getDestroyMethodName() != null) {
            determineDestroyMethod(targetBeanClass, beanRule);
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

    private static Class<?> determineTargetBeanClassForFactoryBean(Class<?> beanClass, BeanRule beanRule)
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

    static Class<?> determineFactoryMethodTargetBeanClass(Class<?> beanClass, BeanRule beanRule)
            throws BeanRuleException {
        if (beanRule.getFactoryMethod() != null) {
            Class<?> targetBeanClass = beanRule.getFactoryMethod().getReturnType();
            beanRule.setTargetBeanClass(targetBeanClass);
            return targetBeanClass;
        } else {
            String factoryMethodName = beanRule.getFactoryMethodName();
            Method m1 = MethodUtils.getAccessibleMethod(beanClass, factoryMethodName, TRANSLET_ACTION_PARAMETER_TYPES);
            Class<?> targetBeanClass;
            if (m1 != null) {
                beanRule.setFactoryMethod(m1);
                beanRule.setFactoryMethodParameterBindingRules(AnnotatedConfigParser.createParameterBindingRules(m1));
                targetBeanClass = m1.getReturnType();
            } else {
                Method m2 = MethodUtils.getAccessibleMethod(beanClass, factoryMethodName);
                if (m2 == null) {
                    throw new BeanRuleException("No such factory method " + factoryMethodName +
                            "() on bean class: " + beanClass.getName(), beanRule);
                }
                beanRule.setFactoryMethod(m2);
                beanRule.setFactoryMethodParameterBindingRules(AnnotatedConfigParser.createParameterBindingRules(m2));
                targetBeanClass = m2.getReturnType();
            }
            beanRule.setTargetBeanClass(targetBeanClass);
            return targetBeanClass;
        }
    }

    static void determineInitMethod(Class<?> beanClass, BeanRule beanRule) throws BeanRuleException {
        if (beanRule.isInitializableBean()) {
            throw new BeanRuleException("Bean initialization method is duplicated; " +
                    "Already implemented the InitializableBean", beanRule);
        }

        String initMethodName = beanRule.getInitMethodName();
        Method m1 = MethodUtils.getAccessibleMethod(beanClass, initMethodName, TRANSLET_ACTION_PARAMETER_TYPES);
        if (m1 != null) {
            beanRule.setInitMethod(m1);
            beanRule.setInitMethodParameterBindingRules(AnnotatedConfigParser.createParameterBindingRules(m1));
        } else {
            Method m2 = MethodUtils.getAccessibleMethod(beanClass, initMethodName);
            if (m2 == null) {
                throw new BeanRuleException("No such initialization method " +
                        initMethodName + "() on bean class: " + beanClass.getName(), beanRule);
            }
            beanRule.setInitMethod(m2);
            beanRule.setInitMethodParameterBindingRules(AnnotatedConfigParser.createParameterBindingRules(m2));
        }
    }

    static void determineDestroyMethod(Class<?> beanClass, BeanRule beanRule) throws BeanRuleException {
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

    static void checkRequiredProperty(BeanRule beanRule, Method method) throws BeanRuleException {
        String propertyName = dropCase(method.getName());
        ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
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
