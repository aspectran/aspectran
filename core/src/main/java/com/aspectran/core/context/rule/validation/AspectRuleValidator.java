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
package com.aspectran.core.context.rule.validation;

import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.InvalidPointcutPatternException;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.component.bean.BeanRuleAnalyzer;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static com.aspectran.core.context.ActivityContext.ID_SEPARATOR_CHAR;

/**
 * The Class AspectAdviceRulePreRegister.
 */
public class AspectRuleValidator {

    private static final Logger logger = LoggerFactory.getLogger(AspectRuleValidator.class);

    public void validate(@NonNull ActivityRuleAssistant assistant) {
        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();
        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        boolean pointcutPatternVerifiable = assistant.isPointcutPatternVerifiable();
        int invalidPointcutPatterns = 0;
        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            Pointcut pointcut = aspectRule.getPointcut();
            if (pointcut != null && aspectRule.isBeanRelevant()) {
                List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
                if (pointcutPatternRuleList != null) {
                    for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                        boolean valid = false;
                        for (BeanRule beanRule : beanRuleRegistry.getConfigurableBeanRules()) {
                            if (matchesBean(pointcut, ppr, beanRule)) {
                                valid = true;
                                break;
                            }
                        }
                        if (!valid) {
                            for (BeanRule beanRule : beanRuleRegistry.getIdBasedBeanRules()) {
                                if (matchesBean(pointcut, ppr, beanRule)) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                        if (!valid) {
                            for (Set<BeanRule> beanRules : beanRuleRegistry.getTypeBasedBeanRules()) {
                                for (BeanRule beanRule : beanRules) {
                                    if (matchesBean(pointcut, ppr, beanRule)) {
                                        valid = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!valid) {
                            PointcutPattern pp = ppr.getPointcutPattern();
                            if (pp != null) {
                                if (pp.getBeanIdPattern() != null) {
                                    invalidPointcutPatterns++;
                                    String msg = "No beans matching to '" + pp.getBeanIdPattern() +
                                            "'; aspectRule " + aspectRule;
                                    if (pointcutPatternVerifiable) {
                                        logger.warn(msg);
                                    } else if (logger.isDebugEnabled()) {
                                        logger.debug(msg);
                                    }
                                }
                                if (pp.getClassNamePattern() != null) {
                                    invalidPointcutPatterns++;
                                    String msg = "No beans matching to '@class:" + pp.getClassNamePattern() +
                                            "'; aspectRule " + aspectRule;
                                    if (pointcutPatternVerifiable) {
                                        logger.warn(msg);
                                    } else if (logger.isDebugEnabled()) {
                                        logger.debug(msg);
                                    }
                                }
                                if (pp.getMethodNamePattern() != null) {
                                    invalidPointcutPatterns++;
                                    String msg = "No beans have methods matching to '^" + pp.getMethodNamePattern() +
                                            "'; aspectRule " + aspectRule;
                                    if (pointcutPatternVerifiable) {
                                        logger.warn(msg);
                                    } else if (logger.isDebugEnabled()) {
                                        logger.debug(msg);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (invalidPointcutPatterns > 0) {
            String msg = "Invalid pointcut detected: " + invalidPointcutPatterns +
                    "; Please check the logs for more information";
            if (pointcutPatternVerifiable) {
                logger.error(msg);
                throw new InvalidPointcutPatternException(msg);
            } else {
                logger.warn(msg);
            }
        }
    }

    private boolean matchesBean(
            Pointcut pointcut,
            @NonNull PointcutPatternRule pointcutPatternRule,
            @NonNull BeanRule beanRule) {
        String beanId = beanRule.getId();
        String className = beanRule.getTargetBeanClassName();
        List<Method> advisableMethods = BeanRuleAnalyzer.getAdvisableMethods(beanRule);
        boolean matched = true;
        PointcutPattern pp = pointcutPatternRule.getPointcutPattern();
        if (pp != null) {
            if (pp.getBeanIdPattern() != null) {
                matched = pointcut.patternMatches(pp.getBeanIdPattern(), beanId, ID_SEPARATOR_CHAR);
            }
            if (matched && pp.getClassNamePattern() != null) {
                matched = pointcut.patternMatches(pp.getClassNamePattern(), className, ID_SEPARATOR_CHAR);
            }
            if (matched && pp.getMethodNamePattern() != null) {
                matched = false;
                for (Method method : advisableMethods) {
                    boolean matched2 = pointcut.patternMatches(pp.getMethodNamePattern(), method.getName());
                    if (matched2) {
                        matched = true;
                        break;
                    }
                }
            }
        }
        return matched;
    }

}
