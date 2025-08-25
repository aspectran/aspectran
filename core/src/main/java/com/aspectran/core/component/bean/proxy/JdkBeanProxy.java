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

import com.aspectran.core.component.bean.BeanFactoryUtils;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK dynamic-proxy implementation that applies Aspectran AOP advice
 * to beans exposing interfaces.
 */
public class JdkBeanProxy extends AbstractBeanProxy implements InvocationHandler {

    private final BeanRule beanRule;

    private final Object bean;

    /**
     * Creates a new JdkBeanProxy.
     * @param context the activity context
     * @param beanRule the bean rule for which the proxy is being created
     * @param bean the target bean instance to be proxied
     */
    private JdkBeanProxy(@NonNull ActivityContext context, @NonNull BeanRule beanRule, @NonNull Object bean) {
        super(context);
        this.beanRule = beanRule;
        this.bean = bean;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        SuperInvoker superInvoker = () -> {
            try {
                return method.invoke(bean, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        };
        return invoke(beanRule, method, args, superInvoker);
    }

    /**
     * Creates a new proxy instance for a bean that implements interfaces.
     * @param context the activity context
     * @param beanRule the bean rule for the bean to be proxied
     * @param args the constructor arguments for the bean instance, may be {@code null}
     * @param argTypes the parameter types for the bean instance's constructor, may be {@code null}
     * @return a new proxy object that implements the bean's interfaces
     * @throws BeanProxyException if an error occurs during bean instantiation or proxy creation
     */
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
