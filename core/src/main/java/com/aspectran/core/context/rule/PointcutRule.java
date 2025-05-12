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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Pointcuts are expressions that match join points and determine whether
 * to execute advice. Pointcuts use a variety of expressions that match
 * join points, and Aspectran uses wildcard pattern matching expressions.
 */
public class PointcutRule {

    private final PointcutType pointcutType;

    private List<PointcutPatternRule> pointcutPatternRuleList;

    public PointcutRule(@Nullable PointcutType pointcutType) {
        this.pointcutType = pointcutType;
    }

    public PointcutType getPointcutType() {
        return pointcutType;
    }

    public List<PointcutPatternRule> getPointcutPatternRuleList() {
        return pointcutPatternRuleList;
    }

    public void addPointcutPatternRule(PointcutPatternRule pointcutPatternRule) {
        if (pointcutPatternRule == null) {
            throw new IllegalArgumentException("pointcutPatternRule must not be null");
        }
        pointcutPatternRule.setPointcutType(pointcutType);
        touchPointcutPatternRuleList().add(pointcutPatternRule);
    }

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
