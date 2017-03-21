/*
 * Copyright 2008-2017 Juho Jeong
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

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class PointcutRule.
 */
public class PointcutRule {

    private final PointcutType pointcutType;

    private List<PointcutPatternRule> pointcutPatternRuleList;

    private List<String> plusPatternStringList;

    private List<String> minusPatternStringList;

    private List<Parameters> includeTargetParametersList;

    private List<Parameters> execludeTargetParametersList;


    public PointcutRule(PointcutType pointcutType) {
        this.pointcutType = pointcutType;
    }

    public PointcutType getPointcutType() {
        return pointcutType;
    }

    public List<PointcutPatternRule> getPointcutPatternRuleList() {
        return pointcutPatternRuleList;
    }

    public void addPointcutPatternRule(PointcutPatternRule pointcutPatternRule, List<PointcutPatternRule> excludePointcutPatternRuleList) {
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
        return new ArrayList<PointcutPatternRule>();
    }

    public List<PointcutPatternRule> newPointcutPatternRuleList(int initialCapacity) {
        return new ArrayList<PointcutPatternRule>(initialCapacity);
    }

    public List<String> getPlusPatternStringList() {
        return plusPatternStringList;
    }

    public void setPlusPatternStringList(List<String> plusPatternStringList) {
        this.plusPatternStringList = plusPatternStringList;
    }

    public List<String> getMinusPatternStringList() {
        return minusPatternStringList;
    }

    public void setMinusPatternStringList(List<String> minusPatternStringList) {
        this.minusPatternStringList = minusPatternStringList;
    }

    public List<Parameters> getIncludeTargetParametersList() {
        return includeTargetParametersList;
    }

    public void setIncludeTargetParametersList(List<Parameters> includeTargetParametersList) {
        this.includeTargetParametersList = includeTargetParametersList;
    }

    public List<Parameters> getExecludeTargetParametersList() {
        return execludeTargetParametersList;
    }

    public void setExecludeTargetParametersList(List<Parameters> execludeTargetParametersList) {
        this.execludeTargetParametersList = execludeTargetParametersList;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("pointcutType", pointcutType);
        tsb.append("pointcutPatternRule", pointcutPatternRuleList);
        return tsb.toString();
    }

    public static PointcutRule newInstance(String type) {
        PointcutType pointcutType = null;

        if (type != null) {
            pointcutType = PointcutType.resolve(type);
            if (pointcutType == null) {
                throw new IllegalArgumentException("Unknown pointcut type '" + type + "'. Pointcut type for Translet must be 'wildcard' or 'regexp'.");
            }
        }

        return new PointcutRule(pointcutType);
    }

}
