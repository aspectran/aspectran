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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.List;

/**
 * Abstract base class for {@link Pointcut} implementations.
 *
 * <p>This class manages a list of {@link PointcutPatternRule}s and implements
 * the core matching logic. It supports both inclusion and exclusion patterns.
 * Subclasses must provide the specific pattern matching implementation by
 * overriding the {@code patternMatches} methods.
 * </p>
 */
public abstract class AbstractPointcut implements Pointcut {

    private final List<PointcutPatternRule> pointcutPatternRuleList;

    private final boolean existsMethodNamePattern;

    /**
     * Creates a new AbstractPointcut with the given list of pattern rules.
     * @param pointcutPatternRuleList the list of pointcut pattern rules
     */
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

    /**
     * {@inheritDoc}
     * <p>This implementation iterates through all pattern rules. A match is found if
     * an inclusion pattern matches and none of its corresponding exclusion patterns match.
     */
    @Override
    public boolean matches(String transletName, String beanId, String className, String methodName) {
        if (pointcutPatternRuleList != null) {
            for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                if (exists(ppr, transletName, beanId, className, methodName)) {
                    List<PointcutPatternRule> excludePatternRuleList = ppr.getExcludePatternRuleList();
                    if (excludePatternRuleList != null) {
                        for (PointcutPatternRule excludePatternRule : excludePatternRuleList) {
                            if (exists(excludePatternRule, transletName, beanId, className, methodName)) {
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

    /**
     * {@inheritDoc}
     * <p>This implementation checks if any inclusion pattern could potentially match.
     * It does not evaluate exclusion patterns.
     */
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
     * Checks if the given join point attributes match a single pointcut pattern rule.
     * A null attribute is considered a match for a non-null pattern (wildcard behavior).
     * @param pointcutPatternRule the rule containing the patterns to check against
     * @param transletName the translet name to match
     * @param beanId the bean ID to match
     * @param className the class name to match
     * @param methodName the method name to match
     * @return true if all non-null patterns in the rule match the corresponding attributes
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
