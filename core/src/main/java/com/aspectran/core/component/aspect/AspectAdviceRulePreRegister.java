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
package com.aspectran.core.component.aspect;

import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.util.BeanDescriptor;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.List;
import java.util.Set;

/**
 * The Class AspectAdviceRulePreRegister.
 */
public class AspectAdviceRulePreRegister {

    private final Log log = LogFactory.getLog(AspectAdviceRulePreRegister.class);

    private AspectRuleRegistry aspectRuleRegistry;

    public AspectAdviceRulePreRegister(AspectRuleRegistry aspectRuleRegistry) {
        this.aspectRuleRegistry = aspectRuleRegistry;

        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            JoinpointTargetType joinpointTargetType = aspectRule.getJoinpointTargetType();
            if (joinpointTargetType == JoinpointTargetType.METHOD) {
                aspectRule.setBeanRelevant(true);
            } else if (joinpointTargetType == JoinpointTargetType.SESSION) {
                aspectRule.setBeanRelevant(false);
            } else {
                Pointcut pointcut = aspectRule.getPointcut();
                if (pointcut == null) {
                    aspectRule.setBeanRelevant(false);
                } else {
                    List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
                    boolean beanRelevant = false;
                    for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                        if (ppr.getBeanIdPattern() != null || ppr.getClassNamePattern() != null || ppr.getMethodNamePattern() != null) {
                            beanRelevant = true;
                            break;
                        }
                    }
                    aspectRule.setBeanRelevant(beanRelevant);
                }
            }

            if (log.isTraceEnabled()) {
                log.trace("preregistered aspectRule " + aspectRule);
            }
        }
    }

    public void register(BeanRuleRegistry beanRuleRegistry) {
        for (BeanRule beanRule : beanRuleRegistry.getConfigBeanRules()) {
            if (beanRule.isProxiable()) {
                determineProxyBean(beanRule);
            }
        }
        for (BeanRule beanRule : beanRuleRegistry.getIdBasedBeanRules()) {
            if (beanRule.isProxiable()) {
                determineProxyBean(beanRule);
            }
        }
        for (Set<BeanRule> set : beanRuleRegistry.getTypeBasedBeanRules()) {
            for (BeanRule beanRule : set) {
                if (beanRule.isProxiable()) {
                    determineProxyBean(beanRule);
                }
            }
        }
    }

    private void determineProxyBean(BeanRule beanRule) {
        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            if (aspectRule.isBeanRelevant()) {
                Pointcut pointcut = aspectRule.getPointcut();
                if (pointcut != null && pointcut.isExistsBeanMethodNamePattern()) {
                    if (existsMatchedBean(pointcut, beanRule)) {
                        beanRule.setProxied(true);

                        if (log.isTraceEnabled()) {
                            log.trace("applied aspectRule " + aspectRule + " to beanRule " + beanRule);
                        }

                        break;
                    }
                } else {
                    if (pointcut == null || existsMatchedBean(pointcut, beanRule.getId(), beanRule.getTargetBeanClassName())) {
                        beanRule.setProxied(true);

                        if (log.isTraceEnabled()) {
                            log.trace("applied aspectRule " + aspectRule + " to beanRule " + beanRule);
                        }

                        break;
                    }
                }
            }
        }
    }

    public void register(TransletRuleRegistry transletRuleRegistry) {
        for (TransletRule transletRule : transletRuleRegistry.getTransletRules()) {
            if (!transletRule.hasPathVariable()) {
                register(transletRule);
            }
        }
    }

    private void register(TransletRule transletRule) {
        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            JoinpointTargetType joinpointTargetType = aspectRule.getJoinpointTargetType();
            Pointcut pointcut = aspectRule.getPointcut();

            if (!aspectRule.isBeanRelevant() && joinpointTargetType != JoinpointTargetType.SESSION) {
                if (pointcut == null || pointcut.matches(transletRule.getName())) {
                    // register to the translet scope
                    transletRule.touchAspectAdviceRuleRegistry().register(aspectRule);

                    if (log.isTraceEnabled()) {
                        log.trace("applied aspectRule " + aspectRule + " to transletRule " + transletRule);
                    }
                }
            }
        }
    }

    private boolean existsMatchedBean(Pointcut pointcut, String beanId, String className) {
        List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();

        if (pointcutPatternRuleList != null) {
            for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                if (existsBean(pointcut, ppr, beanId, className, null)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean existsMatchedBean(Pointcut pointcut, BeanRule beanRule) {
        List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
        if (pointcutPatternRuleList != null) {
            BeanDescriptor cd = BeanDescriptor.getInstance(beanRule.getTargetBeanClass());

            String beanId = beanRule.getId();
            String className = beanRule.getTargetBeanClassName();
            String[] methodNames = cd.getDistinctMethodNames();

            for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                if (existsBean(pointcut, ppr, beanId, className, methodNames)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existsBean(Pointcut pointcut, PointcutPatternRule pointcutPatternRule, String beanId, String className, String[] methodNames) {
        boolean matched = true;
        if (beanId != null && pointcutPatternRule.getBeanIdPattern() != null) {
            matched = pointcut.patternMatches(pointcutPatternRule.getBeanIdPattern(), beanId, ActivityContext.ID_SEPARATOR_CHAR);
            if (matched) {
                pointcutPatternRule.increaseMatchedBeanCount();
            }
        }
        if (matched && className != null && pointcutPatternRule.getClassNamePattern() != null) {
            matched = pointcut.patternMatches(pointcutPatternRule.getClassNamePattern(), className, ActivityContext.ID_SEPARATOR_CHAR);
            if (matched) {
                pointcutPatternRule.increaseMatchedClassCount();
            }
        }
        if (matched && methodNames != null && pointcutPatternRule.getMethodNamePattern() != null) {
            matched = false;
            for (String methodName : methodNames) {
                boolean matched2 = pointcut.patternMatches(pointcutPatternRule.getMethodNamePattern(), methodName);
                if (matched2) {
                    matched = true;
                    pointcutPatternRule.increaseMatchedMethodCount();
                }
            }
        }
        return matched;
    }

}
