/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.core.activity.aspect.AdviceConstraintViolationException;
import com.aspectran.core.activity.aspect.AspectAdviceException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.RelevantAspectRuleHolder;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The Class AbstractDynamicBeanProxy.
 */
public abstract class AbstractDynamicProxyBean {

    private final AspectRuleRegistry aspectRuleRegistry;

    public AbstractDynamicProxyBean(AspectRuleRegistry aspectRuleRegistry) {
        this.aspectRuleRegistry = aspectRuleRegistry;
    }

    protected boolean isAvoidAdvice(@NonNull Method method) {
        return (Object.class == method.getDeclaringClass() ||
                method.getDeclaringClass().isAnnotationPresent(AvoidAdvice.class) ||
                method.isAnnotationPresent(AvoidAdvice.class));
    }

    protected AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(@NonNull Activity activity,
            String beanId, String className, String methodName)
            throws AdviceConstraintViolationException, AspectAdviceException {
        String requestName;
        boolean literalPattern;
        if (activity.getTranslet() != null) {
            requestName = activity.getTranslet().getRequestName();
            literalPattern = !activity.getTranslet().hasPathVariables();
        } else {
            requestName = null;
            literalPattern = true;
        }

        PointcutPattern pointcutPattern = new PointcutPattern(requestName, beanId, className, methodName);
        RelevantAspectRuleHolder holder;
        if (literalPattern) {
            holder = aspectRuleRegistry.getRelevantAspectRuleHolderFromSoftCache(pointcutPattern);
        } else {
            holder = aspectRuleRegistry.getRelevantAspectRuleHolderFromWeakCache(pointcutPattern);
        }

        AspectAdviceRuleRegistry aarr = holder.getAspectAdviceRuleRegistry();
        if (aarr != null && aarr.getSettingsAdviceRuleList() != null) {
            for (SettingsAdviceRule sar : aarr.getSettingsAdviceRuleList()) {
                activity.registerSettingsAdviceRule(sar);
            }
        }
        if (holder.getDynamicAspectRuleList() != null) {
            for (AspectRule aspectRule : holder.getDynamicAspectRuleList()) {
                // register dynamically
                activity.registerAspectAdviceRule(aspectRule);
            }
        }
        return aarr;
    }

    protected void beforeAdvice(List<AspectAdviceRule> beforeAdviceRuleList, BeanRule beanRule, Activity activity)
            throws AspectAdviceException {
        if (beforeAdviceRuleList != null) {
            for (AspectAdviceRule aspectAdviceRule : beforeAdviceRuleList) {
                if (!isSameBean(beanRule, aspectAdviceRule)) {
                    activity.executeAdvice(aspectAdviceRule, true);
                }
            }
        }
    }

    protected void afterAdvice(List<AspectAdviceRule> afterAdviceRuleList, BeanRule beanRule, Activity activity)
            throws AspectAdviceException {
        if (afterAdviceRuleList != null) {
            for (AspectAdviceRule aspectAdviceRule : afterAdviceRuleList) {
                if (!isSameBean(beanRule, aspectAdviceRule)) {
                    activity.executeAdvice(aspectAdviceRule, true);
                }
            }
        }
    }

    protected void finallyAdvice(List<AspectAdviceRule> finallyAdviceRuleList, BeanRule beanRule, Activity activity)
            throws AspectAdviceException {
        if (finallyAdviceRuleList != null) {
            for (AspectAdviceRule aspectAdviceRule : finallyAdviceRuleList) {
                if (!isSameBean(beanRule, aspectAdviceRule)) {
                    activity.executeAdvice(aspectAdviceRule, false);
                }
            }
        }
    }

    protected boolean exceptionally(List<ExceptionRule> exceptionRuleList, Exception exception, @NonNull Activity activity)
            throws ActionExecutionException {
        activity.setRaisedException(exception);
        if (exceptionRuleList != null) {
            activity.handleException(exceptionRuleList);
            return activity.isResponseReserved();
        }
        return false;
    }

    private boolean isSameBean(@NonNull BeanRule beanRule, AspectAdviceRule aspectAdviceRule) {
        if (beanRule.getId() != null && beanRule.getId().equals(aspectAdviceRule.getAdviceBeanId())) {
            return true;
        }
        if (beanRule.getBeanClass() != null && aspectAdviceRule.getAdviceBeanClass() != null) {
            return (beanRule.getBeanClass() == aspectAdviceRule.getAdviceBeanClass());
        }
        return false;
    }

}
