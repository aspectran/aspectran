/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.PointcutPatternRule;

import java.util.List;

/**
 * The Class AbstractPointcut.
 */
public abstract class AbstractPointcut implements Pointcut {

    protected final List<PointcutPatternRule> pointcutPatternRuleList;

    protected final boolean existsBeanMethodNamePattern;

    public AbstractPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        this.pointcutPatternRuleList = pointcutPatternRuleList;

        if (pointcutPatternRuleList != null) {
            boolean existsBeanMethodNamePattern = false;
            for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                if (ppr.getMethodNamePattern() != null) {
                    existsBeanMethodNamePattern = true;
                    break;
                }
            }
            this.existsBeanMethodNamePattern = existsBeanMethodNamePattern;
        } else {
            this.existsBeanMethodNamePattern = false;
        }
    }

    @Override
    public List<PointcutPatternRule> getPointcutPatternRuleList() {
        return pointcutPatternRuleList;
    }

    @Override
    public boolean isExistsBeanMethodNamePattern() {
        return existsBeanMethodNamePattern;
    }

    @Override
    public boolean matches(String transletName) {
        return matches(transletName, null, null, null);
    }

    @Override
    public boolean matches(String transletName, String beanId, String className) {
        return matches(transletName, beanId, className, null);
    }

    @Override
    public boolean matches(String transletName, String beanId, String className, String methodName) {
        if (pointcutPatternRuleList != null) {
            for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                if (matches(ppr, transletName, beanId, className, methodName)) {
                    List<PointcutPatternRule> epprList = ppr.getExcludePointcutPatternRuleList();
                    if (epprList != null) {
                        for (PointcutPatternRule eppr : epprList) {
                            if (matches(eppr, transletName, beanId, className, methodName)) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether or not corresponding to the point cut pattern rules.
     * It is recognized to {@code true} if the operands are {@code null}.
     *
     * @param pointcutPatternRule the pointcut pattern
     * @param transletName the translet name
     * @param beanId the bean id
     * @param className the bean class name
     * @param methodName the name of the method that is executed in the bean
     * @return true, if exists matched
     */
    protected boolean matches(PointcutPatternRule pointcutPatternRule, String transletName, String beanId, String className, String methodName) {
        if ((transletName == null && pointcutPatternRule.getTransletNamePattern() != null)
                || (beanId == null && pointcutPatternRule.getBeanIdPattern() != null)
                || (className == null && pointcutPatternRule.getClassNamePattern() != null)
                || (methodName == null && pointcutPatternRule.getMethodNamePattern() != null)) {
            return false;
        } else {
            return exists(pointcutPatternRule, transletName, beanId, className, methodName);
        }
    }

    @Override
    public boolean exists(String transletName) {
        return exists(transletName, null, null, null);
    }

    @Override
    public boolean exists(String transletName, String beanId, String className) {
        return exists(transletName, beanId, className, null);
    }

    @Override
    public boolean exists(String transletName, String beanId, String className, String methodName) {
        if (pointcutPatternRuleList != null) {
            for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                if (exists(ppr, transletName, beanId, className, methodName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether or not corresponding to the point cut pattern rules.
     * It is recognized to {@code true} if the operands are {@code null}.
     *
     * @param pointcutPatternRule the pointcut pattern
     * @param transletName the translet name
     * @param beanId the bean id
     * @param className the bean class name
     * @param methodName the name of the method that is executed in the bean
     * @return true if exists matched; false otherwise
     */
    protected boolean exists(PointcutPatternRule pointcutPatternRule, String transletName, String beanId, String className, String methodName) {
        boolean matched = true;
        if (transletName != null && pointcutPatternRule.getTransletNamePattern() != null) {
            matched = patternMatches(pointcutPatternRule.getTransletNamePattern(), transletName, ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR);
        }
        if (matched && beanId != null && pointcutPatternRule.getBeanIdPattern() != null) {
            matched = patternMatches(pointcutPatternRule.getBeanIdPattern(), beanId, ActivityContext.ID_SEPARATOR_CHAR);
        }
        if (matched && className != null && pointcutPatternRule.getClassNamePattern() != null) {
            matched = patternMatches(pointcutPatternRule.getClassNamePattern(), className, ActivityContext.ID_SEPARATOR_CHAR);
        }
        if (matched && methodName != null && pointcutPatternRule.getMethodNamePattern() != null) {
            matched = patternMatches(pointcutPatternRule.getMethodNamePattern(), methodName);
        }
        return matched;
    }

}
