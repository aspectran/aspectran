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
 * Base support class for bean proxies that apply Aspectran AOP advice.
 * <p>
 * Coordinates retrieval of applicable advice rules for a given invocation,
 * executes before/after/finally advice, and delegates exception handling.
 * Subclasses implement the actual proxy mechanism (JDK dynamic proxy, Javassist, etc.).
 * </p>
 */
public abstract class AbstractBeanProxy {

    private final AspectRuleRegistry aspectRuleRegistry;

    /**
     * Creates a new AbstractBeanProxy.
     * @param aspectRuleRegistry the registry of aspect rules
     */
    public AbstractBeanProxy(AspectRuleRegistry aspectRuleRegistry) {
        this.aspectRuleRegistry = aspectRuleRegistry;
    }

    /**
     * Retrieves the {@link AdviceRuleRegistry} containing all advice rules relevant to the current join point.
     * This method determines which aspects apply based on the translet name, bean ID, class name, and method name.
     * It utilizes caching for performance optimization.
     * @param activity the current activity
     * @param beanId the ID of the bean being advised
     * @param className the class name of the bean being advised
     * @param methodName the name of the method being invoked
     * @return an {@link AdviceRuleRegistry} containing relevant advice rules, or {@code null} if none are found
     * @throws AdviceConstraintViolationException if an advice constraint is violated
     * @throws AdviceException if an error occurs during advice processing
     */
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
            holder = aspectRuleRegistry.retrieveFromSoftCache(pointcutPattern);
        } else {
            holder = aspectRuleRegistry.retrieveFromWeakCache(pointcutPattern);
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

    /**
     * Executes a list of advice rules.
     * @param adviceRuleList the list of advice rules to execute
     * @param beanRule the bean rule associated with the advised bean
     * @param activity the current activity
     * @throws AdviceException if an error occurs during advice execution
     */
    protected void executeAdvice(List<AdviceRule> adviceRuleList, BeanRule beanRule, Activity activity)
            throws AdviceException {
        if (adviceRuleList != null) {
            for (AdviceRule adviceRule : adviceRuleList) {
                if (!isSameBean(beanRule, adviceRule)) {
                    activity.executeAdvice(adviceRule);
                }
            }
        }
    }

    /**
     * Handles exceptions based on a list of exception rules.
     * @param exceptionRuleList the list of exception rules to apply
     * @param activity the current activity
     * @return true if a response is reserved after exception handling, false otherwise
     * @throws ActionExecutionException if an error occurs during exception handling
     */
    protected boolean handleException(List<ExceptionRule> exceptionRuleList, @NonNull Activity activity)
            throws ActionExecutionException {
        if (exceptionRuleList != null) {
            activity.handleException(exceptionRuleList);
            return activity.isResponseReserved();
        }
        return false;
    }

    /**
     * Checks if the given method is marked as advisable (i.e., has the {@link Advisable} annotation).
     * @param method the method to check
     * @return true if the method is advisable, false otherwise
     */
    protected boolean isAdvisableMethod(@NonNull Method method) {
        return method.isAnnotationPresent(Advisable.class);
    }

    /**
     * Checks if the given bean rule is the same as the bean associated with the advice rule.
     * This is used to prevent an advice from advising itself if it's defined as a bean.
     * @param beanRule the bean rule of the advised bean
     * @param adviceRule the advice rule being considered
     * @return true if they refer to the same bean, false otherwise
     */
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
