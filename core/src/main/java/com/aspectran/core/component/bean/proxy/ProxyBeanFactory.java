package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.lang.reflect.Proxy;

public class ProxyBeanFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProxyBeanFactory.class);

    private final ActivityContext context;

    public ProxyBeanFactory(ActivityContext context) {
        this.context = context;
    }

    public Object createProxy(@NonNull BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        Class<?> superClass = beanRule.getBeanClass();
        Object bean;
        if (superClass.isInterface() || Proxy.isProxyClass(superClass) || ClassUtils.isLambdaClass(superClass)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Create a proxied bean " + beanRule + " using JDK");
            }
            bean = JdkBeanProxy.create(context, beanRule, args, argTypes);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Create a proxied bean " + beanRule + " using Javassist");
            }
            bean = JavassistBeanProxy.create(context, beanRule, args, argTypes);
        }
        return bean;
    }

}
