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

import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.type.PointcutType;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * A factory for creating concrete {@link Pointcut} implementations.
 *
 * <p>This factory instantiates the correct type of pointcut (e.g.,
 * {@link RegexpPointcut} or {@link WildcardPointcut}) based on the
 * {@link PointcutType} specified in a {@link PointcutRule}.
 * </p>
 */
public class PointcutFactory {

    /**
     * Creates a new {@link Pointcut} instance based on the provided {@link PointcutRule}.
     * The type of pointcut created (e.g., regular expression or wildcard) depends on the rule's configuration.
     * @param pointcutRule the rule defining the pointcut
     * @return a concrete {@link Pointcut} implementation
     * @throws IllegalArgumentException if the pointcutRule is null
     */
    public static Pointcut createPointcut(PointcutRule pointcutRule) {
        if (pointcutRule == null) {
            throw new IllegalArgumentException("pointcutRule must not be null");
        }
        if (pointcutRule.getPointcutType() == PointcutType.REGEXP) {
            return createRegexpPointcut(pointcutRule.getPointcutPatternRuleList());
        } else {
            return createWildcardPointcut(pointcutRule.getPointcutPatternRuleList());
        }
    }

    /**
     * Creates a new {@link WildcardPointcut} instance.
     * @param pointcutPatternRuleList the list of pointcut pattern rules for the wildcard pointcut
     * @return a new {@link WildcardPointcut}
     */
    @NonNull
    private static Pointcut createWildcardPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        return new WildcardPointcut(pointcutPatternRuleList);
    }

    /**
     * Creates a new {@link RegexpPointcut} instance.
     * @param pointcutPatternRuleList the list of pointcut pattern rules for the regular expression pointcut
     * @return a new {@link RegexpPointcut}
     */
    @NonNull
    private static Pointcut createRegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        return new RegexpPointcut(pointcutPatternRuleList);
    }

}
