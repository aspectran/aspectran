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

import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.type.PointcutType;

import java.util.List;

/**
 * A factory for creating Pointcut objects.
 */
public class PointcutFactory {

    /**
     * Creates a new Pointcut instance.
     *
     * @param pointcutRule the pointcut rule
     * @return the pointcut
     */
    public static Pointcut createPointcut(PointcutRule pointcutRule) {
        if (pointcutRule.getPointcutType() == PointcutType.REGEXP) {
            return createRegexpPointcut(pointcutRule.getPointcutPatternRuleList());
        } else {
            return createWildcardPointcut(pointcutRule.getPointcutPatternRuleList());
        }
    }

    /**
     * Creates a new WildcardPointcut instance.
     *
     * @param pointcutPatternRuleList the pointcut pattern rule list
     * @return the pointcut
     */
    private static Pointcut createWildcardPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        return new WildcardPointcut(pointcutPatternRuleList);
    }

    /**
     * Creates a new RegexpPointcut instance.
     *
     * @param pointcutPatternRuleList the pointcut pattern rule list
     * @return the pointcut
     */
    private static Pointcut createRegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        return new RegexpPointcut(pointcutPatternRuleList);
    }

}
