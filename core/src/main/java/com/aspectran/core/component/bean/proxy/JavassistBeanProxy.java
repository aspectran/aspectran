/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The Class JavassistBeanProxy.
 *
 * @since 1.1.0
 */
public class JavassistBeanProxy extends AbstractBeanProxy implements MethodHandler {

    private final ActivityContext context;

    private final BeanRule beanRule;

    private JavassistBeanProxy(@NonNull ActivityContext context, @NonNull BeanRule beanRule) {
        super(context.getAspectRuleRegistry());
        this.context = context;
        this.beanRule = beanRule;
    }

    @Override
    public Object invoke(Object self, Method overridden, Method proceed, Object[] args) throws Throwable {
        if (isAvoidAdvice(overridden)) {
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

    @Nullable
    private Object invoke(Object self, @NonNull Method overridden, Method proceed, Object[] args, Activity activity)
            throws Throwable {
        String beanId = beanRule.getId();
        String className = beanRule.getClassName();
        String methodName = overridden.getName();
        AspectAdviceRuleRegistry aarr = getAspectAdviceRuleRegistry(activity, beanId, className, methodName);
        if (aarr == null) {
            return invokeSuper(self, proceed, args);
        }
        try {
            try {
                beforeAdvice(aarr.getBeforeAdviceRuleList(), beanRule, activity);
                Object result = invokeSuper(self, proceed, args);
                afterAdvice(aarr.getAfterAdviceRuleList(), beanRule, activity);
                return result;
            } finally {
                finallyAdvice(aarr.getFinallyAdviceRuleList(), beanRule, activity);
            }
        } catch (Exception e) {
            if (exceptionally(aarr.getExceptionRuleList(), e, activity)) {
                return null;
            }
            throw e;
        }
    }

    private Object invokeSuper(Object self, @NonNull Method proceed, Object[] args) throws Throwable {
        try {
            return proceed.invoke(self, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Creates a proxy class of bean and returns an instance of that class.
     * @param context the activity context
     * @param beanRule the bean rule
     * @param args the arguments passed to a constructor
     * @param argTypes the parameter types for a constructor
     * @return a new proxy bean object
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
