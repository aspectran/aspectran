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

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.annotation.jsr305.NonNull;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Javassist-based proxy that applies Aspectran AOP advice to concrete classes.
 */
public class JavassistBeanProxy extends AbstractBeanProxy implements MethodHandler {

    private final BeanRule beanRule;

    /**
     * Creates a new JavassistBeanProxy.
     * @param context the activity context
     * @param beanRule the bean rule for which the proxy is being created
     */
    private JavassistBeanProxy(@NonNull ActivityContext context, @NonNull BeanRule beanRule) {
        super(context);
        this.beanRule = beanRule;
    }

    @Override
    public Object invoke(Object self, Method overridden, Method proceed, Object[] args) throws Throwable {
        SuperInvoker superInvoker = () -> {
            try {
                return proceed.invoke(self, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        };
        return invoke(beanRule, overridden, args, superInvoker);
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
