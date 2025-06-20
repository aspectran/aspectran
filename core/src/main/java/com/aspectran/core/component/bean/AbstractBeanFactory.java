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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.action.AnnotatedMethodInvoker;
import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.bean.aware.Aware;
import com.aspectran.core.component.bean.aware.CurrentActivityAware;
import com.aspectran.core.component.bean.aware.EnvironmentAware;
import com.aspectran.core.component.bean.proxy.ProxyBeanFactory;
import com.aspectran.core.component.bean.scope.Scope;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.value.ValueEvaluator;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.AutowireTargetRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.ReflectionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final ProxyBeanFactory proxyBeanFactory;

    AbstractBeanFactory(ActivityContext context) {
        this.context = context;
        this.proxyBeanFactory = new ProxyBeanFactory(context);
    }

    protected ActivityContext getActivityContext() {
        return context;
    }

    protected Object createBean(BeanRule beanRule) {
        return createBean(beanRule, null);
    }

    protected Object createBean(@NonNull BeanRule beanRule, Scope scope) {
        Activity activity = context.getAvailableActivity();
        Object bean;
        if (beanRule.isFactoryOffered()) {
            bean = createOfferedFactoryBean(beanRule, scope, activity);
        } else {
            bean = createNormalBean(beanRule, scope, activity);
        }
        return bean;
    }

    protected Object getFactoryProducedObject(@NonNull BeanRule beanRule, Object bean) {
        if (beanRule.isFactoryBean()) {
            return invokeMethodOfFactoryBean(beanRule, bean);
        } else if (beanRule.getFactoryMethodName() != null) {
            Activity activity = context.getAvailableActivity();
            return invokeFactoryMethod(beanRule, bean, activity);
        } else {
            return null;
        }
    }

    private Object createNormalBean(@NonNull BeanRule beanRule, Scope scope, Activity activity) {
        try {
            Object[] args;
            Class<?>[] argTypes;

            ItemRuleMap ctorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
            if (ctorArgumentItemRuleMap != null && !ctorArgumentItemRuleMap.isEmpty()) {
                Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(ctorArgumentItemRuleMap);
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
                        ValueEvaluator valueEvaluator = autowireTargetRules[i].getValueExpression();
                        if (valueEvaluator != null) {
                            args[i] = valueEvaluator.evaluate(activity, null);
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
                                    logger.error("No bean found for autowiring target {} of {}",
                                            autowireTargetRules[i], ctorAutowireRule, e);
                                    throw new BeanCreationException("Could not autowire constructor: " +
                                            ctorAutowireRule, beanRule, e);
                                }
                            } else {
                                try {
                                    args[i] = activity.getBean(type, qualifier);
                                } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                    args[i] = null;
                                    logger.warn("No bean found for autowiring target {} of {}; Cause: {}",
                                            autowireTargetRules[i], ctorAutowireRule, e.toString());
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
                for (Map.Entry<String, ItemRule> entry : propertyItemRuleMap.entrySet()) {
                    Object value = activity.getItemEvaluator().evaluate(entry.getValue());
                    MethodUtils.invokeSetter(bean, entry.getKey(), value);
                }
            }

            if (beanRule.getInitMethod() != null) {
                invokeInitMethod(beanRule, bean, activity);
            }
            if (beanRule.isInitializableBean()) {
                initializeBean(beanRule, bean);
            }

            return bean;
        } catch (BeanCreationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException(beanRule, e);
        }
    }

    private Object createOfferedFactoryBean(@NonNull BeanRule beanRule, Scope scope, Activity activity) {
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
                for (Map.Entry<String, ItemRule> entry : propertyItemRuleMap.entrySet()) {
                    Object value = activity.getItemEvaluator().evaluate(entry.getValue());
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

    private Object instantiateBean(@NonNull BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        Object bean;
        if (beanRule.isProxied()) {
            bean = proxyBeanFactory.createProxy(beanRule, args, argTypes);
        } else if (args != null) {
            bean = BeanFactoryUtils.newInstance(beanRule.getBeanClass(), args, argTypes);
        } else {
            bean = BeanFactoryUtils.newInstance(beanRule.getBeanClass());
        }
        return bean;
    }

    private void autowiring(@NonNull BeanRule beanRule, Object bean, Activity activity) {
        if (beanRule.getAutowireRuleList() != null) {
            for (AutowireRule autowireRule : beanRule.getAutowireRuleList()) {
                if (autowireRule.getTargetType() == AutowireTargetType.FIELD) {
                    AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                    if (autowireTargetRule != null) {
                        Object value;
                        ValueEvaluator valueEvaluator = autowireTargetRule.getValueExpression();
                        if (valueEvaluator != null) {
                            value = valueEvaluator.evaluate(activity, null);
                        } else {
                            Class<?> type = autowireTargetRule.getType();
                            String qualifier = autowireTargetRule.getQualifier();
                            if (autowireRule.isRequired()) {
                                try {
                                    value = activity.getBean(type, qualifier);
                                } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                    logger.error("No bean found for autowiring target {} of {}",
                                            autowireTargetRule, autowireRule, e);
                                    throw new BeanCreationException("Could not autowire field: " +
                                            autowireRule, beanRule, e);
                                }
                            } else {
                                try {
                                    value = activity.getBean(type, qualifier);
                                } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                    value = null;
                                    logger.warn("No bean found for autowiring target {} of {}; Cause: {}",
                                            autowireTargetRule, autowireRule, e.toString());
                                }
                            }
                        }
                        ReflectionUtils.setField(autowireRule.getTarget(), bean, value);
                    }
                } else if (autowireRule.getTargetType() == AutowireTargetType.FIELD_VALUE) {
                    AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                    if (autowireTargetRule != null) {
                        ValueEvaluator valueEvaluator = autowireTargetRule.getValueExpression();
                        if (valueEvaluator != null) {
                            Object value = valueEvaluator.evaluate(activity, null);
                            ReflectionUtils.setField(autowireRule.getTarget(), bean, value);
                        }
                    }
                } else if (autowireRule.getTargetType() == AutowireTargetType.METHOD) {
                    AutowireTargetRule[] autowireTargetRules = autowireRule.getAutowireTargetRules();
                    if (autowireTargetRules != null) {
                        Object[] args = new Object[autowireTargetRules.length];
                        for (int i = 0; i < autowireTargetRules.length; i++) {
                            ValueEvaluator valueEvaluator = autowireTargetRules[i].getValueExpression();
                            if (valueEvaluator != null) {
                                args[i] = valueEvaluator.evaluate(activity, null);
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
                                        logger.error("No bean found for autowiring target {} of {}",
                                                autowireTargetRules[i], autowireRule, e);
                                        throw new BeanCreationException("Could not autowire method: " +
                                                autowireRule, beanRule, e);
                                    }
                                } else {
                                    try {
                                        args[i] = activity.getBean(type, qualifier);
                                    } catch (NoSuchBeanException | NoUniqueBeanException e) {
                                        args[i] = null;
                                        logger.warn("No bean found for autowiring target {} of {}; Cause: {}",
                                                autowireTargetRules[i], autowireRule, e.toString());
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
            if (bean instanceof CurrentActivityAware currentActivityAware) {
                if (context.hasCurrentActivity()) {
                    currentActivityAware.setCurrentActivity(context.getCurrentActivity());
                }
            }
            if (bean instanceof ActivityContextAware activityContextAware) {
                activityContextAware.setActivityContext(context);
            }
            if (bean instanceof ApplicationAdapterAware applicationAdapterAware) {
                applicationAdapterAware.setApplicationAdapter(context.getApplicationAdapter());
            }
            if (bean instanceof EnvironmentAware environmentAware) {
                environmentAware.setEnvironment(context.getEnvironment());
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
            AnnotatedMethodInvoker.invoke(activity, bean, initMethod, parameterBindingRules);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while executing an initialization method of bean",
                    beanRule, e);
        }
    }

    private Object invokeFactoryMethod(BeanRule beanRule, Object bean, Activity activity) {
        try {
            Method factoryMethod = beanRule.getFactoryMethod();
            ParameterBindingRule[] parameterBindingRules = beanRule.getFactoryMethodParameterBindingRules();
            return AnnotatedMethodInvoker.invoke(activity, bean, factoryMethod, parameterBindingRules);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while executing a factory method of bean",
                    beanRule, e);
        }
    }

    @NonNull
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

}
