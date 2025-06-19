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

import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.List;

/**
 * The pattern rule for identifying pointcut targets.
 */
public class PointcutPatternRule {

    private PointcutType pointcutType;

    private String patternString;

    private PointcutPattern pointcutPattern;

    private List<PointcutPatternRule> excludePatternRuleList;

    public PointcutPatternRule() {
    }

    public PointcutType getPointcutType() {
        return pointcutType;
    }

    protected void setPointcutType(PointcutType pointcutType) {
        this.pointcutType = pointcutType;
    }

    public String getPatternString() {
        return patternString;
    }

    public void setPatternString(String patternString) {
        this.patternString = patternString;
    }

    public PointcutPattern getPointcutPattern() {
        return pointcutPattern;
    }

    public void setPointcutPattern(PointcutPattern pointcutPattern) {
        this.pointcutPattern = pointcutPattern;
    }

    public List<PointcutPatternRule> getExcludePatternRuleList() {
        return excludePatternRuleList;
    }

    public void setExcludePatternRuleList(List<PointcutPatternRule> excludePatternRuleList) {
        this.excludePatternRuleList = excludePatternRuleList;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", pointcutType);
        tsb.append("pattern", pointcutPattern);
        tsb.append("excludes", excludePatternRuleList);
        return tsb.toString();
    }

    @NonNull
    public static PointcutPatternRule newInstance(String patternString) {
        PointcutPatternRule ppr = new PointcutPatternRule();
        ppr.setPatternString(patternString);
        ppr.setPointcutPattern(PointcutPattern.parsePattern(patternString));
        return ppr;
    }

    @NonNull
    public static PointcutPatternRule newInstance(String translet, String bean, String method) {
        String transletNamePattern = null;
        String beanIdPattern = null;
        String classNamePattern = null;
        String methodNamePattern = null;

        if (translet != null) {
            transletNamePattern = translet;
        }
        if (bean != null) {
            if (bean.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
                classNamePattern = bean.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
            } else {
                beanIdPattern = bean;
            }
        }
        if (method != null) {
            methodNamePattern = method;
        }
        PointcutPatternRule ppr = new PointcutPatternRule();
        PointcutPattern pointcutPattern = new PointcutPattern(
                transletNamePattern, beanIdPattern, classNamePattern, methodNamePattern);
        ppr.setPointcutPattern(pointcutPattern);
        return ppr;
    }

}
