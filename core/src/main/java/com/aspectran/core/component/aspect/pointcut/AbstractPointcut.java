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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.List;

/**
 * The Class AbstractPointcut.
 */
public abstract class AbstractPointcut implements Pointcut {

    private final List<PointcutPatternRule> pointcutPatternRuleList;

    private final boolean existsMethodNamePattern;

    public AbstractPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        this.pointcutPatternRuleList = pointcutPatternRuleList;

        if (pointcutPatternRuleList != null) {
            boolean existsMethodNamePattern = false;
            for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                PointcutPattern pp = ppr.getPointcutPattern();
                if (pp != null && pp.getMethodNamePattern() != null) {
                    existsMethodNamePattern = true;
                    break;
                }
            }
            this.existsMethodNamePattern = existsMethodNamePattern;
        } else {
            this.existsMethodNamePattern = false;
        }
    }

    @Override
    public List<PointcutPatternRule> getPointcutPatternRuleList() {
        return pointcutPatternRuleList;
    }

    @Override
    public boolean hasMethodNamePattern() {
        return existsMethodNamePattern;
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
                if (exists(ppr, transletName, beanId, className, methodName)) {
                    List<PointcutPatternRule> epprList = ppr.getExcludePointcutPatternRuleList();
                    if (epprList != null) {
                        for (PointcutPatternRule eppr : epprList) {
                            if (exists(eppr, transletName, beanId, className, methodName)) {
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

    @Override
    public boolean matches(@NonNull PointcutPattern pointcutPattern) {
        return matches(pointcutPattern.getTransletNamePattern(), pointcutPattern.getBeanIdPattern(),
                pointcutPattern.getClassNamePattern(), pointcutPattern.getMethodNamePattern());
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

    @Override
    public boolean exists(PointcutPattern pointcutPattern) {
        if (pointcutPattern == null) {
            throw new IllegalArgumentException("pointcutPattern must not be null");
        }
        return exists(pointcutPattern.getTransletNamePattern(), pointcutPattern.getBeanIdPattern(),
                pointcutPattern.getClassNamePattern(), pointcutPattern.getMethodNamePattern());
    }

    /**
     * Returns whether corresponding to the point-cut-pattern rules.
     * It is recognized to {@code true} if the operands are {@code null}.
     * @param pointcutPatternRule the pointcut pattern
     * @param transletName the translet name
     * @param beanId the bean id
     * @param className the bean class name
     * @param methodName the name of the method that is executed in the bean
     * @return true if exists matched; false otherwise
     */
    protected boolean exists(PointcutPatternRule pointcutPatternRule, String transletName,
                             String beanId, String className, String methodName) {
        if (pointcutPatternRule == null) {
            throw new IllegalArgumentException("pointcutPatternRule must not be null");
        }
        boolean matched = true;
        PointcutPattern pp = pointcutPatternRule.getPointcutPattern();
        if (pp != null && pp.getTransletNamePattern() != null) {
            matched = patternMatches(pp.getTransletNamePattern(), transletName, ActivityContext.NAME_SEPARATOR_CHAR);
        }
        if (matched && pp != null && pp.getBeanIdPattern() != null) {
            matched = patternMatches(pp.getBeanIdPattern(), beanId, ActivityContext.ID_SEPARATOR_CHAR);
        }
        if (matched && pp != null && pp.getClassNamePattern() != null) {
            matched = patternMatches(pp.getClassNamePattern(), className, ActivityContext.ID_SEPARATOR_CHAR);
        }
        if (matched && pp != null && pp.getMethodNamePattern() != null) {
            matched = patternMatches(pp.getMethodNamePattern(), methodName);
        }
        return matched;
    }

}
