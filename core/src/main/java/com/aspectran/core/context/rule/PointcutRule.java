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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.core.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class PointcutRule.
 */
public class PointcutRule {

    private final PointcutType pointcutType;

    private List<PointcutPatternRule> pointcutPatternRuleList;

    public PointcutRule(PointcutType pointcutType) {
        this.pointcutType = pointcutType;
    }

    public PointcutType getPointcutType() {
        return pointcutType;
    }

    public List<PointcutPatternRule> getPointcutPatternRuleList() {
        return pointcutPatternRuleList;
    }

    public void setPointcutPatternRuleList(List<PointcutPatternRule> pointcutPatternRuleList) {
        this.pointcutPatternRuleList = pointcutPatternRuleList;
    }

    public void addPointcutPatternRule(PointcutPatternRule pointcutPatternRule,
                                       List<PointcutPatternRule> excludePointcutPatternRuleList) {
        pointcutPatternRule.setPointcutType(pointcutType);
        if (excludePointcutPatternRuleList != null) {
            pointcutPatternRule.setExcludePointcutPatternRuleList(excludePointcutPatternRuleList);
        }
        touchPointcutPatternRuleList().add(pointcutPatternRule);
    }

    public List<PointcutPatternRule> touchPointcutPatternRuleList() {
        if (pointcutPatternRuleList == null) {
            pointcutPatternRuleList = newPointcutPatternRuleList();
        }
        return pointcutPatternRuleList;
    }

    public List<PointcutPatternRule> newPointcutPatternRuleList() {
        return new ArrayList<>();
    }

    public List<PointcutPatternRule> newPointcutPatternRuleList(int initialCapacity) {
        return new ArrayList<>(initialCapacity);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", pointcutType);
        tsb.append("patterns", pointcutPatternRuleList);
        return tsb.toString();
    }

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

    public static PointcutRule newInstance(String[] patterns) {
        PointcutRule pointcutRule = new PointcutRule(PointcutType.WILDCARD);
        List<PointcutPatternRule> pointcutPatternRuleList = pointcutRule.newPointcutPatternRuleList();
        List<PointcutPatternRule> excludePointcutPatternRuleList = pointcutRule.newPointcutPatternRuleList();
        for (String pattern : patterns) {
            if (pattern != null){
                if (pattern.startsWith("-")) {
                    PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePattern(pattern);
                    excludePointcutPatternRuleList.add(pointcutPatternRule);
                } else {
                    if (pattern.startsWith("+")) {
                        pattern = pattern.substring(1);
                    }
                    PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePattern(pattern);
                    pointcutPatternRuleList.add(pointcutPatternRule);
                }
            }
        }
        for (PointcutPatternRule pointcutPatternRule : pointcutPatternRuleList) {
            pointcutPatternRule.setPointcutType(pointcutRule.getPointcutType());
            if (!excludePointcutPatternRuleList.isEmpty()) {
                pointcutPatternRule.setExcludePointcutPatternRuleList(excludePointcutPatternRuleList);
            }
        }
        pointcutRule.setPointcutPatternRuleList(pointcutPatternRuleList);
        return pointcutRule;
    }

}
