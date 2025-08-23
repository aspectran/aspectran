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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivityException;
import com.aspectran.core.activity.InstantProxyActivity;
import com.aspectran.core.component.aspect.AdviceRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Javassist-based proxy that applies Aspectran AOP advice to concrete classes.
 * <p>
 * Generates a subclass at runtime and delegates interception logic to
 * {@link AbstractBeanProxy} while invoking the original implementation
 * via Javassist method handling.
 * </p>
 */
public class JavassistBeanProxy extends AbstractBeanProxy implements MethodHandler {

    private final ActivityContext context;

    private final BeanRule beanRule;

    /**
     * Creates a new JavassistBeanProxy.
     * @param context the activity context
     * @param beanRule the bean rule for which the proxy is being created
     */
    private JavassistBeanProxy(@NonNull ActivityContext context, @NonNull BeanRule beanRule) {
        super(context.getAspectRuleRegistry());
        this.context = context;
        this.beanRule = beanRule;
    }

    /**
     * {@inheritDoc}
     * <p>This method is the entry point for method interception by Javassist.
     * It checks if the method is advisable and then orchestrates the execution
     * of advice (before, after, finally) and exception handling.
     * </p>
     */
    @Override
    public Object invoke(Object self, Method overridden, Method proceed, Object[] args) throws Throwable {
        if (!isAdvisableMethod(overridden)) {
            return proceed.invoke(self, args);
        }
        if (context.hasCurrentActivity()) {
            Activity activity = context.getCurrentActivity();
            return invoke(self, overridden, proceed, args, activity);
        } else {
            try {
                Activity activity = new InstantProxyActivity(context);
                return activity.perform(() -> invoke(self, overridden, proceed, args, activity));
            } catch (Exception e) {
                throw new InstantActivityException(e);
            }
        }
    }

    /**
     * Internal method to execute advice and the target method within an activity context.
     * @param self the proxy instance
     * @param overridden the method being intercepted
     * @param proceed the method to call the original implementation
     * @param args the arguments for the method call
     * @param activity the current activity
     * @return the result of the method invocation
     * @throws Throwable if an error occurs during advice or method execution
     */
    @Nullable
    private Object invoke(Object self, @NonNull Method overridden, Method proceed, Object[] args, Activity activity)
            throws Throwable {
        String beanId = beanRule.getId();
        String className = beanRule.getClassName();
        String methodName = overridden.getName();

        AdviceRuleRegistry adviceRuleRegistry = getAdviceRuleRegistry(activity, beanId, className, methodName);
        if (adviceRuleRegistry == null) {
            return invokeSuper(self, proceed, args);
        }

        try {
            try {
                executeAdvice(adviceRuleRegistry.getBeforeAdviceRuleList(), beanRule, activity);
                Object result = invokeSuper(self, proceed, args);
                executeAdvice(adviceRuleRegistry.getAfterAdviceRuleList(), beanRule, activity);
                return result;
            } catch (Exception e) {
                activity.setRaisedException(e);
                throw e;
            } finally {
                executeAdvice(adviceRuleRegistry.getFinallyAdviceRuleList(), beanRule, activity);
            }
        } catch (Exception e) {
            activity.setRaisedException(e);
            if (handleException(adviceRuleRegistry.getExceptionRuleList(), activity)) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Invokes the original (super) method implementation.
     * @param self the proxy instance
     * @param proceed the method to call the original implementation
     * @param args the arguments for the method call
     * @return the result of the original method invocation
     * @throws Throwable if an error occurs during the original method invocation
     */
    private Object invokeSuper(Object self, @NonNull Method proceed, Object[] args) throws Throwable {
        try {
            return proceed.invoke(self, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Creates a Javassist-based proxy instance for the given bean rule.
     * @param context the activity context
     * @param beanRule the bean rule for which to create the proxy
     * @param args the arguments passed to the bean's constructor, may be {@code null}
     * @param argTypes the parameter types for the bean's constructor, may be {@code null}
     * @return a new proxy bean object
     * @throws BeanProxyException if an error occurs during proxy creation
     */
    public static Object create(ActivityContext context, BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        try {
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setSuperclass(beanRule.getBeanClass());
            MethodHandler methodHandler = new JavassistBeanProxy(context, beanRule);
            return proxyFactory.create(argTypes, args, methodHandler);
        } catch (Exception e) {
            throw new BeanProxyException(beanRule, e);
        }
    }

}
