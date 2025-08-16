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
import com.aspectran.core.component.bean.BeanFactoryUtils;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK dynamic-proxy implementation that applies Aspectran AOP advice
 * to beans exposing interfaces.
 * <p>
 * Delegates interception to {@link AbstractBeanProxy} facilities while
 * creating a standard JDK proxy to wrap the target bean instance.
 * </p>
 */
public class JdkBeanProxy extends AbstractBeanProxy implements InvocationHandler {

    private final ActivityContext context;

    private final BeanRule beanRule;

    private final Object bean;

    private JdkBeanProxy(@NonNull ActivityContext context, @NonNull BeanRule beanRule, @NonNull Object bean) {
        super(context.getAspectRuleRegistry());

        this.context = context;
        this.beanRule = beanRule;
        this.bean = bean;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!isAdvisableMethod(method)) {
            return method.invoke(bean, args);
        }
        if (context.hasCurrentActivity()) {
            Activity activity = context.getCurrentActivity();
            return invoke(method, args, activity);
        } else {
            try {
                Activity activity = new InstantProxyActivity(context);
                return activity.perform(() -> invoke(method, args, activity));
            } catch (Exception e) {
                throw new InstantActivityException(e);
            }
        }
    }

    @Nullable
    private Object invoke(@NonNull Method method, Object[] args, Activity activity) throws Throwable {
        String beanId = beanRule.getId();
        String className = beanRule.getClassName();
        String methodName = method.getName();

        AdviceRuleRegistry adviceRuleRegistry = getAdviceRuleRegistry(activity, beanId, className, methodName);
        if (adviceRuleRegistry == null) {
            return invokeSuper(method, args);
        }

        try {
            try {
                executeAdvice(adviceRuleRegistry.getBeforeAdviceRuleList(), beanRule, activity);
                Object result = invokeSuper(method, args);
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

    private Object invokeSuper(@NonNull Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(bean, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    @NonNull
    public static Object create(ActivityContext context, BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        Object bean;
        if (argTypes != null && args != null) {
            bean = BeanFactoryUtils.newInstance(beanRule.getBeanClass(), args, argTypes);
        } else {
            bean = BeanFactoryUtils.newInstance(beanRule.getBeanClass());
        }
        JdkBeanProxy proxy = new JdkBeanProxy(context, beanRule, bean);
        return Proxy.newProxyInstance(context.getAvailableActivity().getClassLoader(),
                beanRule.getBeanClass().getInterfaces(), proxy);
    }

}
