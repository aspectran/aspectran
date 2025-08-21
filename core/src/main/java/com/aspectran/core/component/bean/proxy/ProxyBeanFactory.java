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
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * Factory that creates proxy instances for beans requiring AOP advice.
 * <p>
 * Chooses between JDK dynamic proxies (for interface-based beans) and
 * Javassist proxies (for concrete classes) and delegates creation to the
 * corresponding proxy implementation.
 * </p>
 */
public class ProxyBeanFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProxyBeanFactory.class);

    private final ActivityContext context;

    public ProxyBeanFactory(ActivityContext context) {
        this.context = context;
    }

    /**
     * Creates a proxy instance for the given bean rule.
     * <p>The factory determines whether to use a JDK dynamic proxy or a
     * Javassist-based proxy based on the bean's class (interface vs. concrete class).</p>
     * @param beanRule the rule for the bean to be proxied
     * @param args the constructor arguments for the bean instance, may be {@code null}
     * @param argTypes the constructor argument types for the bean instance, may be {@code null}
     * @return a new proxy object for the bean
     */
    public Object createProxy(@NonNull BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        Class<?> superClass = beanRule.getBeanClass();
        Object bean;
        if (superClass.isInterface() || Proxy.isProxyClass(superClass) || ClassUtils.isLambdaClass(superClass)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Create a proxied bean {} using JDK", beanRule);
            }
            bean = JdkBeanProxy.create(context, beanRule, args, argTypes);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Create a proxied bean {} using Javassist", beanRule);
            }
            bean = JavassistBeanProxy.create(context, beanRule, args, argTypes);
        }
        return bean;
    }

}
