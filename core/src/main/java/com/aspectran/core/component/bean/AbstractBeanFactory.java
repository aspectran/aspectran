/*
 * Copyright (c) 2008-2020 The Aspectran Project
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
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpression;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.TokenExpression;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ReflectionUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
        Activity activity = context.getCurrentActivity();
        return createBean(beanRule, null, activity);
    }

    protected Object createBean(BeanRule beanRule, Scope scope) {
        Activity activity = context.getCurrentActivity();
        return createBean(beanRule, scope, activity);
    }

    protected Object getFactoryProducedObject(BeanRule beanRule, Object bean) {
        if (beanRule.isFactoryBean()) {
            return invokeMethodOfFactoryBean(beanRule, bean);
        } else if (beanRule.getFactoryMethodName() != null) {
            Activity activity = context.getCurrentActivity();
            return invokeFactoryMethod(beanRule, bean, activity);
        } else {
            return null;
        }
    }

    protected Object createBean(BeanRule beanRule, Scope scope, Activity activity) {
        Object bean;
        if (beanRule.isFactoryOffered()) {
            bean = createOfferedFactoryBean(beanRule, scope, activity);
        } else {
            bean = createNormalBean(beanRule, scope, activity);
        }
        return bean;
    }

    private Object createNormalBean(BeanRule beanRule, Scope scope, Activity activity) {
        try {
            Object[] args;
            Class<?>[] argTypes;

            ItemEvaluator evaluator = null;
            ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
            if (constructorArgumentItemRuleMap != null && !constructorArgumentItemRuleMap.isEmpty()) {
                evaluator = new ItemExpression(activity);
                Map<String, Object> valueMap = evaluator.evaluate(constructorArgumentItemRuleMap);
                args = new Object[constructorArgumentItemRuleMap.size()];
                argTypes = new Class<?>[constructorArgumentItemRuleMap.size()];
                int i = 0;
                for (Map.Entry<String, ItemRule> entry : constructorArgumentItemRuleMap.entrySet()) {
                    Object value = valueMap.get(entry.getKey());
                    args[i] = value;
                    argTypes[i] = ItemRuleUtils.getPrototypeClass(entry.getValue(), value);
                    i++;
                }
            } else {
                AutowireRule ctorAutowireRule = beanRule.getConstructorAutowireRule();
                if (ctorAutowireRule != null) {
                    Class<?>[] types = ctorAutowireRule.getTypes();
                    String[] qualifiers = ctorAutowireRule.getQualifiers();
                    args = new Object[types.length];
                    argTypes = new Class<?>[types.length];
                    for (int i = 0; i < types.length; i++) {
                        if (ctorAutowireRule.isRequired()) {
                            try {
                                args[i] = activity.getBean(types[i], qualifiers[i]);
                            } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                throw new BeanCreationException("Could not autowire constructor: " +
                                        ctorAutowireRule, beanRule);
                            }
                        } else {
                            try {
                                args[i] = activity.getBean(types[i], qualifiers[i]);
                            } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                args[i] = null;
                                logger.warn(e.getMessage());
                            }
                        }
                        argTypes[i] = types[i];
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
                if (evaluator == null) {
                    evaluator = new ItemExpression(activity);
                }
                for (Map.Entry<String, ItemRule> entry : propertyItemRuleMap.entrySet()) {
                    Object value = evaluator.evaluate(entry.getValue());
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
                ItemEvaluator evaluator = new ItemExpression(activity);
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
                    Field field = autowireRule.getTarget();
                    Class<?>[] types = autowireRule.getTypes();
                    String[] qualifiers = autowireRule.getQualifiers();

                    Object value;
                    if (autowireRule.isRequired()) {
                        try {
                            value = activity.getBean(types[0], qualifiers[0]);
                        } catch (NoSuchBeanException | NoUniqueBeanException e) {
                            throw new BeanCreationException("Could not autowire field: " +
                                    autowireRule, beanRule);
                        }
                    } else {
                        try {
                            value = activity.getBean(types[0], qualifiers[0]);
                        } catch (NoSuchBeanException | NoUniqueBeanException e) {
                            value = null;
                            logger.warn(e.getMessage());
                        }
                    }

                    ReflectionUtils.setField(field, bean, value);
                } else if (autowireRule.getTargetType() == AutowireTargetType.FIELD_VALUE) {
                    Field field = autowireRule.getTarget();
                    Token token = autowireRule.getToken();

                    TokenEvaluator evaluator = new TokenExpression(activity);
                    Object value = evaluator.evaluate(token);

                    ReflectionUtils.setField(field, bean, value);
                } else if (autowireRule.getTargetType() == AutowireTargetType.METHOD) {
                    Method method = autowireRule.getTarget();
                    Class<?>[] types = autowireRule.getTypes();
                    String[] qualifiers = autowireRule.getQualifiers();

                    Object[] args = new Object[types.length];
                    for (int i = 0; i < types.length; i++) {
                        if (autowireRule.isRequired()) {
                            try {
                                args[i] = activity.getBean(types[i], qualifiers[i]);
                            } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                throw new BeanCreationException("Could not autowire method: " +
                                        autowireRule, beanRule);
                            }
                        } else {
                            try {
                                args[i] = activity.getBean(types[i], qualifiers[i]);
                            } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                args[i] = null;
                                logger.warn(e.getMessage());
                            }
                        }
                    }

                    ReflectionUtils.invokeMethod(method, bean, args);
                }
            }
        }
    }

    private void invokeAwareMethods(Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof CurrentActivityAware) {
                ((CurrentActivityAware)bean).setCurrentActivity(context.getCurrentActivity());
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
