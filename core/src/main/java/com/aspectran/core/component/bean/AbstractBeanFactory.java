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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.action.AnnotatedAction;
import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.bean.aware.Aware;
import com.aspectran.core.component.bean.aware.ClassLoaderAware;
import com.aspectran.core.component.bean.aware.CurrentActivityAware;
import com.aspectran.core.component.bean.aware.EnvironmentAware;
import com.aspectran.core.component.bean.proxy.CglibDynamicProxyBean;
import com.aspectran.core.component.bean.proxy.JavassistDynamicProxyBean;
import com.aspectran.core.component.bean.proxy.JdkDynamicProxyBean;
import com.aspectran.core.component.bean.scope.Scope;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.ExpressionEvaluator;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.AutowireTargetRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.ReflectionUtils;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * The Class AbstractBeanFactory.
 *
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
abstract class AbstractBeanFactory extends AbstractComponent {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBeanFactory.class);

    private final ActivityContext context;

    private final BeanProxifierType beanProxifierType;

    AbstractBeanFactory(ActivityContext context, BeanProxifierType beanProxifierType) {
        this.context = context;
        this.beanProxifierType = (beanProxifierType != null ? beanProxifierType : BeanProxifierType.JAVASSIST);
    }

    protected ActivityContext getActivityContext() {
        return context;
    }

    protected Object createBean(BeanRule beanRule) {
        return createBean(beanRule, null);
    }

    protected Object createBean(BeanRule beanRule, Scope scope) {
        Activity activity = context.getAvailableActivity();
        Object bean;
        if (beanRule.isFactoryOffered()) {
            bean = createOfferedFactoryBean(beanRule, scope, activity);
        } else {
            bean = createNormalBean(beanRule, scope, activity);
        }
        return bean;
    }

    protected Object getFactoryProducedObject(BeanRule beanRule, Object bean) {
        if (beanRule.isFactoryBean()) {
            return invokeMethodOfFactoryBean(beanRule, bean);
        } else if (beanRule.getFactoryMethodName() != null) {
            Activity activity = context.getAvailableActivity();
            return invokeFactoryMethod(beanRule, bean, activity);
        } else {
            return null;
        }
    }

    private Object createNormalBean(BeanRule beanRule, Scope scope, Activity activity) {
        try {
            Object[] args;
            Class<?>[] argTypes;

            ItemEvaluator itemEvaluator = null;
            ItemRuleMap ctorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
            if (ctorArgumentItemRuleMap != null && !ctorArgumentItemRuleMap.isEmpty()) {
                itemEvaluator = new ItemEvaluation(activity);
                Map<String, Object> valueMap = itemEvaluator.evaluate(ctorArgumentItemRuleMap);
                args = new Object[ctorArgumentItemRuleMap.size()];
                argTypes = new Class<?>[ctorArgumentItemRuleMap.size()];
                int i = 0;
                for (Map.Entry<String, ItemRule> entry : ctorArgumentItemRuleMap.entrySet()) {
                    Object value = valueMap.get(entry.getKey());
                    args[i] = value;
                    argTypes[i] = ItemRuleUtils.getPrototypeClass(entry.getValue(), value);
                    i++;
                }
            } else {
                AutowireRule ctorAutowireRule = beanRule.getConstructorAutowireRule();
                AutowireTargetRule[] autowireTargetRules = AutowireRule.getAutowireTargetRules(ctorAutowireRule);
                if (autowireTargetRules != null) {
                    args = new Object[autowireTargetRules.length];
                    argTypes = new Class<?>[autowireTargetRules.length];
                    for (int i = 0; i < autowireTargetRules.length; i++) {
                        Class<?> type = autowireTargetRules[i].getType();
                        ExpressionEvaluator evaluator = autowireTargetRules[i].getExpressionEvaluation();
                        if (evaluator != null) {
                            args[i] = evaluator.evaluate(activity, null);
                            if (ctorAutowireRule.isRequired() && args[i] == null) {
                                throw new BeanCreationException("Could not autowire constructor: " +
                                        ctorAutowireRule, beanRule);
                            }
                        } else {
                            String qualifier = autowireTargetRules[i].getQualifier();
                            if (ctorAutowireRule.isRequired()) {
                                try {
                                    args[i] = activity.getBean(type, qualifier);
                                } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                    logger.error("No bean found for autowiring target " + autowireTargetRules[i] +
                                            " of " + ctorAutowireRule, e);
                                    throw new BeanCreationException("Could not autowire constructor: " +
                                            ctorAutowireRule, beanRule, e);
                                }
                            } else {
                                try {
                                    args[i] = activity.getBean(type, qualifier);
                                } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                    args[i] = null;
                                    logger.warn("No bean found for autowiring target " + autowireTargetRules[i] +
                                            " of " + ctorAutowireRule + "; Cause: " + e);
                                }
                            }
                        }
                        argTypes[i] = type;
                    }
                } else {
                    args = MethodUtils.EMPTY_OBJECT_ARRAY;
                    argTypes = MethodUtils.EMPTY_CLASS_PARAMETERS;
                }
            }

            Object bean = instantiateBean(beanRule, args, argTypes);

            if (scope != null) {
                scope.putBeanInstance(activity, beanRule, new BeanInstance(bean));
            }

            invokeAwareMethods(bean);
            autowiring(beanRule, bean, activity);

            ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
            if (propertyItemRuleMap != null && !propertyItemRuleMap.isEmpty()) {
                if (itemEvaluator == null) {
                    itemEvaluator = new ItemEvaluation(activity);
                }
                for (Map.Entry<String, ItemRule> entry : propertyItemRuleMap.entrySet()) {
                    Object value = itemEvaluator.evaluate(entry.getValue());
                    MethodUtils.invokeSetter(bean, entry.getKey(), value);
                }
            }

            if (beanRule.isInitializableBean()) {
                initializeBean(beanRule, bean);
            } else if (beanRule.getInitMethod() != null) {
                invokeInitMethod(beanRule, bean, activity);
            }

            return bean;
        } catch (BeanCreationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException(beanRule, e);
        }
    }

    private Object createOfferedFactoryBean(BeanRule beanRule, Scope scope, Activity activity) {
        String factoryBeanId = beanRule.getFactoryBeanId();
        Class<?> factoryBeanClass = beanRule.getFactoryBeanClass();
        Object bean;

        try {
            if (factoryBeanClass != null) {
                if (Modifier.isInterface(factoryBeanClass.getModifiers())) {
                    bean = null;
                } else {
                    bean = activity.getBean(factoryBeanClass);
                }
            } else {
                bean = activity.getBean(factoryBeanId);
            }
            bean = invokeFactoryMethod(beanRule, bean, activity);
            if (bean == null) {
                throw new NullPointerException("Factory Method [" + beanRule.getFactoryMethod() +
                        "] has returned null");
            }
        } catch (BeanCreationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException(
                    "An exception occurred while invoking a factory method from the offered factory bean",
                    beanRule, e);
        }

        if (scope != null) {
            scope.putBeanInstance(activity, beanRule, new BeanInstance(bean));
        }

        try {
            ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
            if (propertyItemRuleMap != null && !propertyItemRuleMap.isEmpty()) {
                ItemEvaluator evaluator = new ItemEvaluation(activity);
                for (Map.Entry<String, ItemRule> entry : propertyItemRuleMap.entrySet()) {
                    Object value = evaluator.evaluate(entry.getValue());
                    MethodUtils.invokeSetter(bean, entry.getKey(), value);
                }
            }
            if (beanRule.getInitMethod() != null) {
                invokeInitMethod(beanRule, bean, activity);
            }
            return bean;
        } catch (BeanCreationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException(beanRule, e);
        }
    }

    private Object instantiateBean(BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        Object bean;
        if (beanRule.isProxied()) {
            bean = instantiateDynamicBeanProxy(beanRule, args, argTypes);
        } else if (args != null) {
            bean = newInstance(beanRule.getBeanClass(), args, argTypes);
        } else {
            bean = newInstance(beanRule.getBeanClass());
        }
        return bean;
    }

    private Object instantiateDynamicBeanProxy(BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        Object bean;
        if (beanProxifierType == BeanProxifierType.JAVASSIST) {
            if (logger.isTraceEnabled()) {
                logger.trace("Create a dynamic proxy bean " + beanRule + " using Javassist");
            }
            bean = JavassistDynamicProxyBean.newInstance(context, beanRule, args, argTypes);
        } else if (beanProxifierType == BeanProxifierType.CGLIB) {
            if (logger.isTraceEnabled()) {
                logger.trace("Create a dynamic proxy bean " + beanRule + " using CGLIB");
            }
            bean = CglibDynamicProxyBean.newInstance(context, beanRule, args, argTypes);
        } else {
            if (argTypes != null && args != null) {
                bean = newInstance(beanRule.getBeanClass(), args, argTypes);
            } else {
                bean = newInstance(beanRule.getBeanClass());
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Create a dynamic proxy bean " + beanRule + " using JDK");
            }
            bean = JdkDynamicProxyBean.newInstance(context, beanRule, bean);
        }
        return bean;
    }

    private void autowiring(BeanRule beanRule, Object bean, Activity activity) {
        if (beanRule.getAutowireRuleList() != null) {
            for (AutowireRule autowireRule : beanRule.getAutowireRuleList()) {
                if (autowireRule.getTargetType() == AutowireTargetType.FIELD) {
                    AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                    if (autowireTargetRule != null) {
                        Object value;
                        ExpressionEvaluator evaluator = autowireTargetRule.getExpressionEvaluation();
                        if (evaluator != null) {
                            value = evaluator.evaluate(activity, null);
                        } else {
                            Class<?> type = autowireTargetRule.getType();
                            String qualifier = autowireTargetRule.getQualifier();
                            if (autowireRule.isRequired()) {
                                try {
                                    value = activity.getBean(type, qualifier);
                                } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                    logger.error("No bean found for autowiring target " + autowireTargetRule +
                                            " of " + autowireRule, e);
                                    throw new BeanCreationException("Could not autowire field: " +
                                            autowireRule, beanRule, e);
                                }
                            } else {
                                try {
                                    value = activity.getBean(type, qualifier);
                                } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                    value = null;
                                    logger.warn("No bean found for autowiring target " + autowireTargetRule  +
                                            " of " + autowireRule + "; Cause: " + e);
                                }
                            }
                        }
                        ReflectionUtils.setField(autowireRule.getTarget(), bean, value);
                    }
                } else if (autowireRule.getTargetType() == AutowireTargetType.FIELD_VALUE) {
                    AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                    if (autowireTargetRule != null) {
                        ExpressionEvaluator evaluator = autowireTargetRule.getExpressionEvaluation();
                        if (evaluator != null) {
                            Object value = evaluator.evaluate(activity, null);
                            ReflectionUtils.setField(autowireRule.getTarget(), bean, value);
                        }
                    }
                } else if (autowireRule.getTargetType() == AutowireTargetType.METHOD) {
                    AutowireTargetRule[] autowireTargetRules = autowireRule.getAutowireTargetRules();
                    if (autowireTargetRules != null) {
                        Object[] args = new Object[autowireTargetRules.length];
                        for (int i = 0; i < autowireTargetRules.length; i++) {
                            ExpressionEvaluator evaluator = autowireTargetRules[i].getExpressionEvaluation();
                            if (evaluator != null) {
                                args[i] = evaluator.evaluate(activity, null);
                                if (autowireRule.isRequired() && args[i] == null) {
                                    throw new BeanCreationException("Autowiring failed for method: " +
                                            autowireRule, beanRule);
                                }
                            } else {
                                Class<?> type = autowireTargetRules[i].getType();
                                String qualifier = autowireTargetRules[i].getQualifier();
                                if (autowireRule.isRequired()) {
                                    try {
                                        args[i] = activity.getBean(type, qualifier);
                                    } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                        logger.error("No bean found for autowiring target " +
                                                autowireTargetRules[i] + " of " + autowireRule, e);
                                        throw new BeanCreationException("Could not autowire method: " +
                                                autowireRule, beanRule, e);
                                    }
                                } else {
                                    try {
                                        args[i] = activity.getBean(type, qualifier);
                                    } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                        args[i] = null;
                                        logger.warn("No bean found for autowiring target " +
                                                autowireTargetRules[i] + " of " + autowireRule + "; Cause: " + e);
                                    }
                                }
                            }
                        }
                        ReflectionUtils.invokeMethod(autowireRule.getTarget(), bean, args);
                    }
                }
            }
        }
    }

    private void invokeAwareMethods(Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof CurrentActivityAware) {
                if (context.hasCurrentActivity()) {
                    ((CurrentActivityAware)bean).setCurrentActivity(context.getCurrentActivity());
                }
            }
            if (bean instanceof ActivityContextAware) {
                ((ActivityContextAware)bean).setActivityContext(context);
            }
            if (bean instanceof ApplicationAdapterAware) {
                ((ApplicationAdapterAware)bean).setApplicationAdapter(context.getApplicationAdapter());
            }
            if (bean instanceof ClassLoaderAware) {
                ((ClassLoaderAware)bean).setClassLoader(context.getApplicationAdapter().getClassLoader());
            }
            if (bean instanceof EnvironmentAware) {
                ((EnvironmentAware)bean).setEnvironment(context.getEnvironment());
            }
        }
    }

    private void initializeBean(BeanRule beanRule, Object bean) {
        try {
            ((InitializableBean)bean).initialize();
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while initialization of bean", beanRule, e);
        }
    }

    private void invokeInitMethod(BeanRule beanRule, Object bean, Activity activity) {
        try {
            Method initMethod = beanRule.getInitMethod();
            ParameterBindingRule[] parameterBindingRules = beanRule.getInitMethodParameterBindingRules();
            AnnotatedAction.invokeMethod(activity, bean, initMethod, parameterBindingRules);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while executing an initialization method of bean",
                    beanRule, e);
        }
    }

    private Object invokeFactoryMethod(BeanRule beanRule, Object bean, Activity activity) {
        try {
            Method factoryMethod = beanRule.getFactoryMethod();
            ParameterBindingRule[] parameterBindingRules = beanRule.getFactoryMethodParameterBindingRules();
            return AnnotatedAction.invokeMethod(activity, bean, factoryMethod, parameterBindingRules);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while executing a factory method of bean",
                    beanRule, e);
        }
    }

    private Object invokeMethodOfFactoryBean(BeanRule beanRule, Object bean) {
        Object resultBean;
        try {
            resultBean = ((FactoryBean<?>)bean).getObject();
        } catch (Exception e) {
            throw new BeanCreationException("FactoryBean threw exception on object creation", beanRule, e);
        }
        if (resultBean == null) {
            throw new FactoryBeanNotInitializedException(
                            "FactoryBean returned null object: " +
                            "probably not fully initialized (maybe due to circular bean reference)",
                            beanRule);
        }
        return resultBean;
    }

    private static Object newInstance(Class<?> beanClass, Object[] args, Class<?>[] argTypes) {
        if (beanClass.isInterface()) {
            throw new BeanInstantiationException(beanClass, "Specified class is an interface");
        }
        Constructor<?> constructorToUse;
        try {
            constructorToUse = getMatchConstructor(beanClass, args);
            if (constructorToUse == null) {
                constructorToUse = ClassUtils.findConstructor(beanClass, argTypes);
            }
        } catch (NoSuchMethodException e) {
            throw new BeanInstantiationException(beanClass, "No default constructor found", e);
        }
        return newInstance(constructorToUse, args);
    }

    private static Object newInstance(Class<?> beanClass) {
        return newInstance(beanClass, MethodUtils.EMPTY_OBJECT_ARRAY, MethodUtils.EMPTY_CLASS_PARAMETERS);
    }

    private static Object newInstance(Constructor<?> ctor, Object[] args) {
        try {
            return ctor.newInstance(args);
        } catch (InstantiationException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Is it an abstract class?", e);
        } catch (IllegalAccessException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Has the class definition changed? Is the constructor accessible?", e);
        } catch (IllegalArgumentException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Illegal arguments for constructor", e);
        } catch (InvocationTargetException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Constructor threw exception", e.getTargetException());
        }
    }

    private static Constructor<?> getMatchConstructor(Class<?> beanClass, Object[] args) {
        Constructor<?>[] candidates = beanClass.getDeclaredConstructors();
        Constructor<?> constructorToUse = null;
        float bestMatchWeight = Float.MAX_VALUE;
        float matchWeight;
        for (Constructor<?> candidate : candidates) {
            matchWeight = ReflectionUtils.getTypeDifferenceWeight(candidate.getParameterTypes(), args);
            if (matchWeight < bestMatchWeight) {
                constructorToUse = candidate;
                bestMatchWeight = matchWeight;
            }
        }
        return constructorToUse;
    }

}
