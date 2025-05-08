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
package com.aspectran.core.component.aspect;

import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Class AspectAdviceRulePreRegister.
 */
public class AspectAdviceRulePreRegister {

    private final AspectRuleRegistry aspectRuleRegistry;

    private boolean pointcutPatternVerifiable;

    public AspectAdviceRulePreRegister(@NonNull AspectRuleRegistry aspectRuleRegistry) {
        this.aspectRuleRegistry = aspectRuleRegistry;
    }

    public void setPointcutPatternVerifiable(boolean pointcutPatternVerifiable) {
        this.pointcutPatternVerifiable = pointcutPatternVerifiable;
    }

    public void checkProxiable(@NonNull BeanRuleRegistry beanRuleRegistry) {
        for (BeanRule beanRule : beanRuleRegistry.getConfigurableBeanRules()) {
            if (beanRule.getProxied() == null && !beanRule.isFactoryable()) {
                determineProxyBean(beanRule);
            }
        }
        for (BeanRule beanRule : beanRuleRegistry.getIdBasedBeanRules()) {
            if (beanRule.getProxied() == null && !beanRule.isFactoryable()) {
                determineProxyBean(beanRule);
            }
        }
        for (Set<BeanRule> beanRules : beanRuleRegistry.getTypeBasedBeanRules()) {
            for (BeanRule beanRule : beanRules) {
                if (beanRule.getProxied() == null && !beanRule.isFactoryable()) {
                    determineProxyBean(beanRule);
                }
            }
        }
    }

    private void determineProxyBean(@NonNull BeanRule beanRule) {
        List<Method> adviceMethods = new ArrayList<>();
        Class<?> currentClass = beanRule.getTargetBeanClass();
        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Advisable.class)) {
                    adviceMethods.add(method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        if (!adviceMethods.isEmpty()) {
            beanRule.setProxied(true);
            for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
                if (aspectRule.isBeanRelevant()) {
                    Pointcut pointcut = aspectRule.getPointcut();
                    if (pointcut != null) {
                        if (existsMatchedBean(pointcut, beanRule, adviceMethods)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void register(@NonNull TransletRuleRegistry transletRuleRegistry) {
        for (TransletRule transletRule : transletRuleRegistry.getTransletRules()) {
            if (!transletRule.hasPathVariables()) {
                register(transletRule);
            }
        }
    }

    private void register(TransletRule transletRule) {
        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            Pointcut pointcut = aspectRule.getPointcut();
            if (!aspectRule.isBeanRelevant()) {
                if (pointcut == null || pointcut.matches(transletRule.getName())) {
                    // register to the translet scope
                    transletRule.touchAspectAdviceRuleRegistry().register(aspectRule);
                }
            }
        }
    }

    private boolean existsMatchedBean(@NonNull Pointcut pointcut, BeanRule beanRule, List<Method> adviceMethods) {
        boolean exists = false;
        List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
        if (pointcutPatternRuleList != null) {
            String beanId = beanRule.getId();
            String className = beanRule.getTargetBeanClassName();
            for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                if (matchesBean(pointcut, ppr, beanId, className, adviceMethods)) {
                    exists = true;
                    if (!pointcutPatternVerifiable) {
                        break;
                    }
                }
            }
        }
        return exists;
    }

    private boolean matchesBean(
            Pointcut pointcut, @NonNull PointcutPatternRule pointcutPatternRule,
            String beanId, String className, List<Method> adviceMethods) {
        boolean matched = true;
        PointcutPattern pp = pointcutPatternRule.getPointcutPattern();
        if (pp != null) {
            if (pp.getBeanIdPattern() != null) {
                matched = pointcut.patternMatches(pp.getBeanIdPattern(), beanId, ActivityContext.ID_SEPARATOR_CHAR);
                if (matched) {
                    pointcutPatternRule.increaseMatchedBeanIdCount();
                }
            }
            if (matched && pp.getClassNamePattern() != null) {
                matched = pointcut.patternMatches(pp.getClassNamePattern(), className, ActivityContext.ID_SEPARATOR_CHAR);
                if (matched) {
                    pointcutPatternRule.increaseMatchedClassNameCount();
                }
            }
            if (matched && pp.getMethodNamePattern() != null) {
                matched = false;
                for (Method method : adviceMethods) {
                    boolean matched2 = pointcut.patternMatches(pp.getMethodNamePattern(), method.getName());
                    if (matched2) {
                        pointcutPatternRule.increaseMatchedMethodNameCount();
                        matched = true;
                        break;
                    }
                }
            }
        }
        return matched;
    }

}
