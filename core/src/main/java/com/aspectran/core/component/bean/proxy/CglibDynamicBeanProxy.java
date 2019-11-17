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
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The Class CglibDynamicBeanProxy.
 */
public class CglibDynamicBeanProxy extends AbstractDynamicBeanProxy implements MethodInterceptor {

    private static final Log log = LogFactory.getLog(CglibDynamicBeanProxy.class);

    private final ActivityContext context;

    private final BeanRule beanRule;

    private CglibDynamicBeanProxy(ActivityContext context, BeanRule beanRule) {
        super(context.getAspectRuleRegistry());

        this.context = context;
        this.beanRule = beanRule;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (isAvoidAdvice(method)) {
            return methodProxy.invokeSuper(proxy, args);
        }

        Activity activity = context.getCurrentActivity();
        String transletName = (activity.getTranslet() != null ? activity.getTranslet().getRequestName() : StringUtils.EMPTY);
        String beanId = StringUtils.nullToEmpty(beanRule.getId());
        String className = StringUtils.nullToEmpty(beanRule.getClassName());
        String methodName = method.getName();
        AspectAdviceRuleRegistry aarr = retrieveAspectAdviceRuleRegistry(activity, transletName, beanId, className, methodName);
        if (aarr == null) {
            return methodProxy.invokeSuper(proxy, args);
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

                Object result = methodProxy.invokeSuper(proxy, args);

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
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(context.getApplicationAdapter().getClassLoader());
        enhancer.setSuperclass(beanRule.getBeanClass());
        enhancer.setCallback(new CglibDynamicBeanProxy(context, beanRule));
        return enhancer.create(argTypes, args);
    }

}