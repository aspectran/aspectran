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
import com.aspectran.core.activity.aspect.AdviceConstraintViolationException;
import com.aspectran.core.activity.aspect.AdviceException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.component.aspect.AdviceRuleRegistry;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.RelevantAspectRuleHolder;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The Class AbstractBeanProxy.
 */
public abstract class AbstractBeanProxy {

    private final AspectRuleRegistry aspectRuleRegistry;

    public AbstractBeanProxy(AspectRuleRegistry aspectRuleRegistry) {
        this.aspectRuleRegistry = aspectRuleRegistry;
    }

    protected AdviceRuleRegistry getAdviceRuleRegistry(
            @NonNull Activity activity, String beanId, String className, String methodName)
            throws AdviceConstraintViolationException, AdviceException {
        String requestName;
        boolean literalPattern;
        if (activity.hasTranslet()) {
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

        AdviceRuleRegistry adviceRuleRegistry = holder.getAdviceRuleRegistry();
        if (adviceRuleRegistry != null && adviceRuleRegistry.getSettingsAdviceRuleList() != null) {
            for (SettingsAdviceRule sar : adviceRuleRegistry.getSettingsAdviceRuleList()) {
                activity.registerSettingsAdviceRule(sar);
            }
        }
        if (holder.getDynamicAspectRuleList() != null) {
            for (AspectRule aspectRule : holder.getDynamicAspectRuleList()) {
                // register dynamically
                activity.registerAdviceRule(aspectRule);
            }
        }
        return adviceRuleRegistry;
    }

    protected void beforeAdvice(List<AdviceRule> beforeAdviceRuleList, BeanRule beanRule, Activity activity)
            throws AdviceException {
        if (beforeAdviceRuleList != null) {
            for (AdviceRule adviceRule : beforeAdviceRuleList) {
                if (!isSameBean(beanRule, adviceRule)) {
                    activity.executeAdvice(adviceRule, true);
                }
            }
        }
    }

    protected void afterAdvice(List<AdviceRule> afterAdviceRuleList, BeanRule beanRule, Activity activity)
            throws AdviceException {
        if (afterAdviceRuleList != null) {
            for (AdviceRule adviceRule : afterAdviceRuleList) {
                if (!isSameBean(beanRule, adviceRule)) {
                    activity.executeAdvice(adviceRule, true);
                }
            }
        }
    }

    protected void finallyAdvice(List<AdviceRule> finallyAdviceRuleList, BeanRule beanRule, Activity activity)
            throws AdviceException {
        if (finallyAdviceRuleList != null) {
            for (AdviceRule adviceRule : finallyAdviceRuleList) {
                if (!isSameBean(beanRule, adviceRule)) {
                    activity.executeAdvice(adviceRule, false);
                }
            }
        }
    }

    protected boolean exceptionally(
            List<ExceptionRule> exceptionRuleList, Exception exception, @NonNull Activity activity)
            throws ActionExecutionException {
        activity.setRaisedException(exception);
        if (exceptionRuleList != null) {
            activity.handleException(exceptionRuleList);
            return activity.isResponseReserved();
        }
        return false;
    }

    protected boolean isAdvisableMethod(@NonNull Method method) {
        return method.isAnnotationPresent(Advisable.class);
    }

    private boolean isSameBean(@NonNull BeanRule beanRule, AdviceRule adviceRule) {
        if (beanRule.getId() != null && beanRule.getId().equals(adviceRule.getAdviceBeanId())) {
            return true;
        }
        if (beanRule.getBeanClass() != null && adviceRule.getAdviceBeanClass() != null) {
            return (beanRule.getBeanClass() == adviceRule.getAdviceBeanClass());
        }
        return false;
    }

}
