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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a pointcut that identifies join points where an aspect's advice should be executed.
 * A pointcut consists of one or more patterns that match against translet names, bean IDs, or method names.
 *
 * <p>Created: 2016. 02. 13.</p>
 */
public class PointcutRule {

    private final PointcutType pointcutType;

    private List<PointcutPatternRule> pointcutPatternRuleList;

    /**
     * Instantiates a new PointcutRule.
     * @param pointcutType the type of pointcut (e.g., wildcard, regexp)
     */
    public PointcutRule(@Nullable PointcutType pointcutType) {
        this.pointcutType = pointcutType;
    }

    /**
     * Gets the pointcut type.
     * @return the pointcut type
     */
    public PointcutType getPointcutType() {
        return pointcutType;
    }

    /**
     * Gets the list of pointcut pattern rules.
     * @return the list of pointcut pattern rules
     */
    public List<PointcutPatternRule> getPointcutPatternRuleList() {
        return pointcutPatternRuleList;
    }

    /**
     * Adds a pointcut pattern rule.
     * @param pointcutPatternRule the pointcut pattern rule to add
     */
    public void addPointcutPatternRule(PointcutPatternRule pointcutPatternRule) {
        if (pointcutPatternRule == null) {
            throw new IllegalArgumentException("pointcutPatternRule must not be null");
        }
        pointcutPatternRule.setPointcutType(pointcutType);
        touchPointcutPatternRuleList().add(pointcutPatternRule);
    }

    /**
     * Gets the list of pointcut pattern rules, creating it if it does not exist.
     * @return the list of pointcut pattern rules
     */
    public List<PointcutPatternRule> touchPointcutPatternRuleList() {
        if (pointcutPatternRuleList == null) {
            pointcutPatternRuleList = new ArrayList<>();
        }
        return pointcutPatternRuleList;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", pointcutType);
        tsb.append("patterns", pointcutPatternRuleList);
        return tsb.toString();
    }

    /**
     * Creates a new instance of PointcutRule.
     * @param type the pointcut type as a string
     * @return a new PointcutRule instance
     * @throws IllegalRuleException if the type is unrecognized
     */
    @NonNull
    public static PointcutRule newInstance(String type) throws IllegalRuleException {
        PointcutType pointcutType = null;
        if (type != null) {
            pointcutType = PointcutType.resolve(type);
            if (pointcutType == null) {
                throw new IllegalRuleException("Unrecognized pointcut type '" + type +
                        "'; Pointcut type for Translet must be 'wildcard' or 'regexp'");
            }
        }
        return new PointcutRule(pointcutType);
    }

    /**
     * Creates a new instance of PointcutRule from an array of pattern strings.
     * @param patterns an array of pattern strings
     * @return a new PointcutRule instance, or null if patterns are empty
     * @throws IllegalRuleException if a pattern is invalid
     */
    public static PointcutRule newInstance(String[] patterns) throws IllegalRuleException {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        PointcutRule pointcutRule = new PointcutRule(PointcutType.WILDCARD);
        List<PointcutPatternRule> pointcutPatternRuleList = new ArrayList<>(patterns.length);
        List<PointcutPatternRule> excludePointcutPatternRuleList = new ArrayList<>(patterns.length);
        for (String pattern : patterns) {
            if (pattern != null) {
                pattern = pattern.trim();
                if (pattern.startsWith("-:")) {
                    pattern = pattern.substring(2).trim();
                    PointcutPatternRule pointcutPatternRule = PointcutPatternRule.newInstance(pattern);
                    excludePointcutPatternRuleList.add(pointcutPatternRule);
                } else if (pattern.startsWith("+:")) {
                    pattern = pattern.substring(2).trim();
                    PointcutPatternRule pointcutPatternRule = PointcutPatternRule.newInstance(pattern);
                    pointcutPatternRuleList.add(pointcutPatternRule);
                } else {
                    throw new IllegalRuleException("Invalid pointcut pattern: " + pattern);
                }
            }
        }
        if (pointcutPatternRuleList.isEmpty() && excludePointcutPatternRuleList.isEmpty()) {
            return null;
        }
        if (pointcutPatternRuleList.isEmpty()) {
            PointcutPatternRule pointcutPatternRule = new PointcutPatternRule();
            pointcutPatternRule.setExcludePatternRuleList(excludePointcutPatternRuleList);
            pointcutRule.addPointcutPatternRule(pointcutPatternRule);
        } else {
            for (PointcutPatternRule pointcutPatternRule : pointcutPatternRuleList) {
                pointcutPatternRule.setPointcutType(pointcutRule.getPointcutType());
                pointcutPatternRule.setExcludePatternRuleList(excludePointcutPatternRuleList);
                pointcutRule.addPointcutPatternRule(pointcutPatternRule);
            }
        }
        return pointcutRule;
    }

}
