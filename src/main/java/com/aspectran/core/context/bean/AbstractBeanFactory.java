/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.action.MethodAction;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.context.bean.annotation.Configuration;
import com.aspectran.core.context.bean.aware.ActivityContextAware;
import com.aspectran.core.context.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.context.bean.aware.Aware;
import com.aspectran.core.context.bean.aware.ClassLoaderAware;
import com.aspectran.core.context.bean.proxy.CglibDynamicBeanProxy;
import com.aspectran.core.context.bean.proxy.JavassistDynamicBeanProxy;
import com.aspectran.core.context.bean.proxy.JdkDynamicBeanProxy;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.TokenExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AutowireTargetRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ReflectionUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractBeanFactory.
 * 
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public abstract class AbstractBeanFactory implements BeanFactory {

    protected final Log log = LogFactory.getLog(getClass());

    protected final BeanRuleRegistry beanRuleRegistry;

    private final BeanProxifierType beanProxifierType;

    protected ActivityContext context;

    /** Flag that indicates whether this context is currently active */
    private final AtomicBoolean active = new AtomicBoolean();

    public AbstractBeanFactory(BeanRuleRegistry beanRuleRegistry, BeanProxifierType beanProxifierType) {
        this.beanRuleRegistry = beanRuleRegistry;
        this.beanProxifierType = (beanProxifierType == null ? BeanProxifierType.JAVASSIST : beanProxifierType);
    }

    protected Object createBean(BeanRule beanRule) {
        Activity activity = context.getCurrentActivity();
        if (activity == null) {
            throw new BeanException("Cannot create a bean because an active activity is not found");
        }
        return createBean(beanRule, activity);
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

    private Object createBean(BeanRule beanRule, Activity activity) {
        Object bean;
        if (!beanRule.isFactoryOffered()) {
            bean = createNormalBean(beanRule, activity);
        } else {
            bean = createOfferedFactoryBean(beanRule, activity);
        }
        return bean;
    }

    private Object createNormalBean(BeanRule beanRule, Activity activity) {
        try {
            Object bean;
            Object[] args;
            Class<?>[] argTypes;

            ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
            ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
            ItemEvaluator evaluator = null;

            if (constructorArgumentItemRuleMap != null) {
                evaluator = new ItemExpressionParser(activity);
                Map<String, Object> valueMap = evaluator.evaluate(constructorArgumentItemRuleMap);

                int parameterSize = constructorArgumentItemRuleMap.size();
                args = new Object[parameterSize];
                argTypes = new Class<?>[parameterSize];

                int i = 0;
                for (String name : constructorArgumentItemRuleMap.keySet()) {
                    Object o = valueMap.get(name);
                    args[i] = o;
                    argTypes[i] = o.getClass();
                    i++;
                }
            } else {
                args = MethodUtils.EMPTY_OBJECT_ARRAY;
                argTypes = MethodUtils.EMPTY_CLASS_PARAMETERS;
            }

            bean = createBeanInstance(beanRule, args, argTypes);

            if (beanRule.isSingleton()) {
                beanRule.setInstantiatedBean(new InstantiatedBean(bean));
            }

            invokeAwareMethods(bean);
            autowiring(beanRule, bean, activity);

            if (propertyItemRuleMap != null) {
                if (evaluator == null) {
                    evaluator = new ItemExpressionParser(activity);
                }
                Map<String, Object> valueMap = evaluator.evaluate(propertyItemRuleMap);
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    MethodUtils.invokeSetter(bean, entry.getKey(), entry.getValue());
                }
            }

            if (beanRule.isInitializableBean() || beanRule.isInitializableTransletBean()) {
                initializeBean(beanRule, bean, activity);
            } else if (beanRule.getInitMethod() != null) {
                invokeInitMethod(beanRule, bean, activity);
            }

            return bean;
        } catch (Exception e) {
            throw new BeanCreationException(beanRule, e);
        }
    }

    private Object createOfferedFactoryBean(BeanRule beanRule, Activity activity) {
        String factoryBeanId = beanRule.getFactoryBeanId();
        Class<?> factoryBeanClass = beanRule.getFactoryBeanClass();
        Object bean;

        try {
            if (factoryBeanClass != null) {
                if (factoryBeanClass.isAnnotationPresent(Configuration.class)) {
                    bean = activity.getConfigBean(factoryBeanClass);
                } else {
                    bean = activity.getBean(factoryBeanClass);
                }
            } else {
                bean = activity.getBean(factoryBeanId);
            }

            bean = invokeFactoryMethod(beanRule, bean, activity);
            if (bean == null) {
                throw new NullPointerException("Factory Method [" + beanRule.getFactoryMethod() + "] has returned null");
            }
        } catch (Exception e) {
            throw new BeanCreationException(
                    "An exception occurred during the execution of a factory method from the offered factory bean",
                    beanRule, e);
        }

        if (beanRule.isSingleton()) {
            beanRule.setInstantiatedBean(new InstantiatedBean(bean));
        }

        try {
            ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();

            if (propertyItemRuleMap != null) {
                ItemEvaluator evaluator = new ItemExpressionParser(activity);
                Map<String, Object> valueMap = evaluator.evaluate(propertyItemRuleMap);

                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    MethodUtils.invokeSetter(bean, entry.getKey(), entry.getValue());
                }
            }

            if (beanRule.getInitMethod() != null) {
                invokeInitMethod(beanRule, bean, activity);
            }

            return bean;
        } catch (Exception e) {
            throw new BeanCreationException(beanRule, e);
        }
    }

    private Object createBeanInstance(BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        Object bean;

        if (beanRule.isProxied()) {
            bean = createDynamicBeanProxy(beanRule, args, argTypes);
        } else {
            if (args != null && argTypes != null) {
                bean = newInstance(beanRule.getBeanClass(), args, argTypes);
            } else {
                bean = newInstance(beanRule.getBeanClass());
            }
        }

        return bean;
    }

    private Object createDynamicBeanProxy(BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        Object bean;

        if (beanProxifierType == BeanProxifierType.JAVASSIST) {
            if (log.isTraceEnabled()) {
                log.trace("create a javassist proxy bean " + beanRule);
            }
            bean = JavassistDynamicBeanProxy.newInstance(context, beanRule, args, argTypes);
        } else if (beanProxifierType == BeanProxifierType.CGLIB) {
            if (log.isTraceEnabled()) {
                log.trace("create a cglib proxy bean " + beanRule);
            }
            bean = CglibDynamicBeanProxy.newInstance(context, beanRule, args, argTypes);
        } else {
            if (argTypes != null && args != null) {
                bean = newInstance(beanRule.getBeanClass(), args, argTypes);
            } else {
                bean = newInstance(beanRule.getBeanClass());
            }
            if (log.isTraceEnabled()) {
                log.trace("create a jdk proxy bean " + beanRule);
            }
            bean = JdkDynamicBeanProxy.newInstance(context, beanRule, bean);
        }

        return bean;
    }

    private void autowiring(BeanRule beanRule, Object bean, Activity activity) {
        List<AutowireTargetRule> autowireTargetList = beanRule.getAutowireTargetRuleList();

        if (autowireTargetList != null) {
            for (AutowireTargetRule autowireTargetRule : autowireTargetList) {
                if (autowireTargetRule.getTargetType() == AutowireTargetType.FIELD) {
                    Field field = autowireTargetRule.getTarget();

                    Object value = activity.getBean(autowireTargetRule.getTypes()[0], autowireTargetRule.getQualifiers()[0]);

                    ReflectionUtils.setField(field, bean, value);
                } else if (autowireTargetRule.getTargetType() == AutowireTargetType.METHOD) {
                    Method method = autowireTargetRule.getTarget();

                    Class<?>[] types = autowireTargetRule.getTypes();
                    String[] qualifiers = autowireTargetRule.getQualifiers();
                    Object[] values = new Object[types.length];
                    for (int i = 0; i < types.length; i++) {
                        values[i] = activity.getBean(types[i], qualifiers[i]);
                    }

                    ReflectionUtils.invokeMethod(method, bean, values);
                } else if (autowireTargetRule.getTargetType() == AutowireTargetType.VALUE) {
                    Field field = autowireTargetRule.getTarget();

                    Token token = autowireTargetRule.getToken();
                    TokenEvaluator evaluator = new TokenExpressionParser(activity);
                    Object value = evaluator.evaluate(token);

                    ReflectionUtils.setField(field, bean, value);
                }
            }
        }
    }

    private void invokeAwareMethods(Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof ActivityContextAware) {
                ((ActivityContextAware)bean).setActivityContext(context);
            }
            if (bean instanceof ApplicationAdapterAware) {
                ((ApplicationAdapterAware)bean).setApplicationAdapter(context.getApplicationAdapter());
            }
            if (bean instanceof ClassLoaderAware) {
                ((ClassLoaderAware)bean).setClassLoader(context.getClassLoader());
            }
        }
    }

    private void initializeBean(BeanRule beanRule, Object bean, Activity activity) {
        try {
            if (beanRule.isInitializableBean()) {
                ((InitializableBean)bean).initialize();
            } else if (beanRule.isInitializableTransletBean()) {
                ((InitializableTransletBean)bean).initialize(activity.getTranslet());
            }
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while initialization of the bean", beanRule, e);
        }
    }

    private void invokeInitMethod(BeanRule beanRule, Object bean, Activity activity) {
        try {
            Method initMethod = beanRule.getInitMethod();
            boolean requiresTranslet = beanRule.isInitMethodRequiresTranslet();
            MethodAction.invokeMethod(activity, bean, initMethod, requiresTranslet);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while executing an initialization method of the bean", beanRule, e);
        }
    }

    private Object invokeFactoryMethod(BeanRule beanRule, Object bean, Activity activity) {
        try {
            Method factoryMethod = beanRule.getFactoryMethod();
            boolean requiresTranslet = beanRule.isFactoryMethodRequiresTranslet();
            return MethodAction.invokeMethod(activity, bean, factoryMethod, requiresTranslet);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while executing a factory method of the bean", beanRule, e);
        }
    }

    private Object invokeMethodOfFactoryBean(BeanRule beanRule, Object bean) {
        FactoryBean<?> factoryBean = (FactoryBean<?>)bean;
        Object exposedBean;

        try {
            exposedBean = factoryBean.getObject();
        } catch (Exception e) {
            throw new BeanCreationException("FactoryBean threw exception on object creation", beanRule, e);
        }

        if (exposedBean == null) {
            throw new FactoryBeanNotInitializedException(
                            "FactoryBean returned null object: " +
                            "probably not fully initialized (maybe due to circular bean reference)",
                            beanRule);
        }

        return exposedBean;
    }

    @Override
    public synchronized void initialize(ActivityContext context) {
        if (this.active.compareAndSet(false, true)) {
            this.context = context;

            if (log.isDebugEnabled()) {
                log.debug("Initializing singletons...");
            }

            instantiateSingletons();

            log.info("BeanFactory has been initialized");
        } else {
            log.warn("BeanFactory has already been initialized");
        }
    }

    @Override
    public void destroy() {
        if (this.active.compareAndSet(true, false)) {
            destroySingletons();

            log.info("BeanFactory has been destroyed");
        } else {
            log.warn("BeanFactory has already been destroyed");
        }
    }

    /**
     * Instantiate all singletons(non-lazy-init).
     */
    private void instantiateSingletons() {
        Activity activity = context.getDefaultActivity();

        for (BeanRule beanRule : beanRuleRegistry.getIdBasedBeanRuleMap().values()) {
            instantiateSingleton(beanRule, activity);
        }
        for (Set<BeanRule> beanRuleSet : beanRuleRegistry.getTypeBasedBeanRuleMap().values()) {
            for (BeanRule beanRule : beanRuleSet) {
                instantiateSingleton(beanRule, activity);
            }
        }
        for (BeanRule beanRule : beanRuleRegistry.getConfigBeanRuleMap().values()) {
            instantiateSingleton(beanRule, activity);
        }
    }

    private void instantiateSingleton(BeanRule beanRule, Activity activity) {
        if (beanRule.isSingleton()
                && beanRule.getInstantiatedBean() == null
                && !beanRule.isLazyInit()) {
            createBean(beanRule, activity);
        }
    }

    /**
     * Destroy all cached singletons.
     */
    private void destroySingletons() {
        if (log.isDebugEnabled()) {
            log.debug("Destroying singletons in " + this);
        }

        int failedDestroyes = 0;

        for (BeanRule beanRule : beanRuleRegistry.getIdBasedBeanRuleMap().values()) {
            failedDestroyes += doDestroySingleton(beanRule);
        }
        for (Set<BeanRule> beanRuleSet : beanRuleRegistry.getTypeBasedBeanRuleMap().values()) {
            for (BeanRule beanRule : beanRuleSet) {
                failedDestroyes += doDestroySingleton(beanRule);
            }
        }
        for (BeanRule beanRule : beanRuleRegistry.getConfigBeanRuleMap().values()) {
            failedDestroyes += doDestroySingleton(beanRule);
        }

        if (failedDestroyes > 0) {
            log.warn("Singletons has not been destroyed cleanly (Failure Count: " + failedDestroyes + ")");
        } else {
            log.info("Singletons has been destroyed");
        }
    }

    private int doDestroySingleton(BeanRule beanRule) {
        int failedCount = 0;

        if (beanRule.getInstantiatedBean() != null && beanRule.isSingleton()) {
            try {
                InstantiatedBean instantiatedBean = beanRule.getInstantiatedBean();
                Object bean = instantiatedBean.getBean();
                if (bean != null) {
                    if (beanRule.isDisposableBean()) {
                        ((DisposableBean)bean).destroy();
                    } else if (beanRule.getDestroyMethod() != null) {
                        Method destroyMethod = beanRule.getDestroyMethod();
                        destroyMethod.invoke(bean, MethodUtils.EMPTY_OBJECT_ARRAY);
                    }
                }
            } catch (Exception e) {
                failedCount++;
                log.error("Could not destroy singleton bean " + beanRule, e);
            }

            beanRule.setInstantiatedBean(null);
        }

        return failedCount;
    }

    private static Object newInstance(Class<?> beanClass, Object[] args, Class<?>[] argTypes) throws BeanInstantiationException {
        if (beanClass.isInterface())
            throw new BeanInstantiationException("Specified class is an interface", beanClass);

        Constructor<?> constructorToUse;

        try {
            constructorToUse = getMatchConstructor(beanClass, args);
            if (constructorToUse == null) {
                constructorToUse = beanClass.getDeclaredConstructor(argTypes);
            }
        } catch (NoSuchMethodException e) {
            throw new BeanInstantiationException("No default constructor found", beanClass, e);
        }

        return newInstance(constructorToUse, args);
    }

    private static Object newInstance(Class<?> beanClass) throws BeanInstantiationException {
        return newInstance(beanClass, MethodUtils.EMPTY_OBJECT_ARRAY, MethodUtils.EMPTY_CLASS_PARAMETERS);
    }

    private static Object newInstance(Constructor<?> ctor, Object[] args) throws BeanInstantiationException {
        try {
            if (!Modifier.isPublic(ctor.getModifiers())
                    || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) {
                ctor.setAccessible(true);
            }
            return ctor.newInstance(args);
        } catch (InstantiationException ex) {
            throw new BeanInstantiationException("Is it an abstract class?", ctor.getDeclaringClass(), ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException("Has the class definition changed? Is the constructor accessible?", ctor.getDeclaringClass(), ex);
        } catch (IllegalArgumentException ex) {
            throw new BeanInstantiationException("Illegal arguments for constructor", ctor.getDeclaringClass(), ex);
        } catch (InvocationTargetException ex) {
            throw new BeanInstantiationException("Constructor threw exception", ctor.getDeclaringClass(), ex.getTargetException());
        }
    }

    private static Constructor<?> getMatchConstructor(Class<?> clazz, Object[] args) {
        Constructor<?>[] candidates = clazz.getDeclaredConstructors();

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
