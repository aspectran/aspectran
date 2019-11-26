/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The Class JavassistDynamicBeanProxy.
 *
 * @since 1.1.0
 */
public class JavassistDynamicProxyBean extends AbstractDynamicProxyBean implements MethodHandler  {

    private static final Log log = LogFactory.getLog(JavassistDynamicProxyBean.class);

    private final ActivityContext context;

    private final BeanRule beanRule;

    private JavassistDynamicProxyBean(ActivityContext context, BeanRule beanRule) {
        super(context.getAspectRuleRegistry());

        this.context = context;
        this.beanRule = beanRule;
    }

    @Override
    public Object invoke(Object self, Method overridden, Method proceed, Object[] args) throws Throwable {
        if (isAvoidAdvice(overridden)) {
            return proceed.invoke(self, args);
        }

        Activity activity = context.getCurrentActivity();
        String transletName = (activity.getTranslet() != null ? activity.getTranslet().getRequestName() : null);
        String beanId = beanRule.getId();
        String className = beanRule.getClassName();
        String methodName = overridden.getName();

        AspectAdviceRuleRegistry aarr = getAspectAdviceRuleRegistry(activity, transletName, beanId, className, methodName);
        if (aarr == null) {
            return proceed.invoke(self, args);
        }

        try {
            try {
                if (aarr.getBeforeAdviceRuleList() != null) {
                    for (AspectAdviceRule aspectAdviceRule : aarr.getBeforeAdviceRuleList()) {
                        if (!isSameBean(beanRule, aspectAdviceRule)) {
                            activity.executeAdvice(aspectAdviceRule, true);
                        }
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("Invoke a proxy method " + methodName + "() on the bean " + beanRule);
                }

                Object result = proceed.invoke(self, args);

                if (aarr.getAfterAdviceRuleList() != null) {
                    for (AspectAdviceRule aspectAdviceRule : aarr.getAfterAdviceRuleList()) {
                        if (!isSameBean(beanRule, aspectAdviceRule)) {
                            activity.executeAdvice(aspectAdviceRule, true);
                        }
                    }
                }

                return result;
            } finally {
                if (aarr.getFinallyAdviceRuleList() != null) {
                    for (AspectAdviceRule aspectAdviceRule : aarr.getFinallyAdviceRuleList()) {
                        if (!isSameBean(beanRule, aspectAdviceRule)) {
                            activity.executeAdvice(aspectAdviceRule, false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            activity.setRaisedException(e);

            List<ExceptionRule> exceptionRuleList = aarr.getExceptionRuleList();
            if (exceptionRuleList != null) {
                activity.handleException(exceptionRuleList);
                if (activity.isResponseReserved()) {
                    return null;
                }
            }

            throw e;
        }
    }

    /**
     * Creates a proxy class of bean and returns an instance of that class.
     *
     * @param context the activity context
     * @param beanRule the bean rule
     * @param args the arguments passed to a constructor
     * @param argTypes the parameter types for a constructor
     * @return a new proxy bean object
     */
    public static Object newInstance(ActivityContext context, BeanRule beanRule, Object[] args, Class<?>[] argTypes) {
        try {
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setSuperclass(beanRule.getBeanClass());
            MethodHandler methodHandler = new JavassistDynamicProxyBean(context, beanRule);
            return proxyFactory.create(argTypes, args, methodHandler);
        } catch (Exception e) {
            throw new ProxyBeanInstantiationException(beanRule, e);
        }
    }

}