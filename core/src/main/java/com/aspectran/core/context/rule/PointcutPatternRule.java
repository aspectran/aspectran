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

import java.util.List;

/**
 * The pattern rule for identifying pointcut targets
 */
public class PointcutPatternRule {

    private static final char POINTCUT_BEAN_CLASS_DELIMITER = '@';

    private static final char POINTCUT_METHOD_NAME_DELIMITER = '^';

    private PointcutType pointcutType;

    private String patternString;

    private String transletNamePattern;

    private String beanIdPattern;

    private String classNamePattern;

    private String methodNamePattern;

    private int matchedBeanCount;

    private int matchedClassCount;

    private int matchedMethodCount;

    private List<PointcutPatternRule> excludePointcutPatternRuleList;

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

    public String getTransletNamePattern() {
        return transletNamePattern;
    }

    public void setTransletNamePattern(String transletNamePattern) {
        this.transletNamePattern = transletNamePattern;
    }

    public String getBeanIdPattern() {
        return beanIdPattern;
    }

    public void setBeanIdPattern(String beanIdPattern) {
        this.beanIdPattern = beanIdPattern;
    }

    public String getClassNamePattern() {
        return classNamePattern;
    }

    public void setClassNamePattern(String classNamePattern) {
        this.classNamePattern = classNamePattern;
    }

    public String getMethodNamePattern() {
        return methodNamePattern;
    }

    public void setMethodNamePattern(String methodNamePattern) {
        this.methodNamePattern = methodNamePattern;
    }

    public List<PointcutPatternRule> getExcludePointcutPatternRuleList() {
        return excludePointcutPatternRuleList;
    }

    public void setExcludePointcutPatternRuleList(List<PointcutPatternRule> excludePointcutPatternRuleList) {
        this.excludePointcutPatternRuleList = excludePointcutPatternRuleList;
    }

    public int getMatchedBeanCount() {
        return matchedBeanCount;
    }

    public void increaseMatchedBeanCount() {
        matchedBeanCount++;
    }

    public int getMatchedClassCount() {
        return matchedClassCount;
    }

    public void increaseMatchedClassCount() {
        matchedClassCount++;
    }

    public int getMatchedMethodCount() {
        return matchedMethodCount;
    }

    public void increaseMatchedMethodCount() {
        matchedMethodCount++;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("translet", transletNamePattern);
        tsb.append("bean", beanIdPattern);
        tsb.append("class", classNamePattern);
        tsb.append("method", methodNamePattern);
        tsb.append("exclude", excludePointcutPatternRuleList);
        return tsb.toString();
    }

    public static String combinePattern(String transletName, String beanId, String className, String methodName) {
        int len = 0;
        if (transletName != null) {
            len += transletName.length();
        }
        if (beanId != null) {
            len += beanId.length() + 1;
        } else if (className != null) {
            len += className.length() + 7;
        }
        if (methodName != null) {
            len += methodName.length() + 1;
        }

        StringBuilder sb = new StringBuilder(len);
        if (transletName != null) {
            sb.append(transletName);
        }
        if (beanId != null) {
            sb.append(POINTCUT_BEAN_CLASS_DELIMITER);
            sb.append(beanId);
        } else if (className != null) {
            sb.append(POINTCUT_BEAN_CLASS_DELIMITER);
            sb.append(BeanRule.CLASS_DIRECTIVE_PREFIX);
            sb.append(className);
        }
        if (methodName != null) {
            sb.append(POINTCUT_METHOD_NAME_DELIMITER);
            sb.append(methodName);
        }
        return sb.toString();
    }

    public static PointcutPatternRule parsePattern(String patternString) {
        PointcutPatternRule ppr = new PointcutPatternRule();
        ppr.setPatternString(patternString);

        String transletNamePattern = null;
        String beanClassPattern = null;
        String methodNamePattern = null;

        int beanClassDelimiterIndex = patternString.indexOf(POINTCUT_BEAN_CLASS_DELIMITER);

        if (beanClassDelimiterIndex == -1) {
            transletNamePattern = patternString;
        } else if (beanClassDelimiterIndex == 0) {
            beanClassPattern = patternString.substring(1);
        } else {
            transletNamePattern = patternString.substring(0, beanClassDelimiterIndex);
            beanClassPattern = patternString.substring(beanClassDelimiterIndex + 1);
        }

        if (beanClassPattern != null) {
            int methodNameDelimiterIndex = beanClassPattern.indexOf(POINTCUT_METHOD_NAME_DELIMITER);

            if (methodNameDelimiterIndex == 0) {
                methodNamePattern = beanClassPattern.substring(1);
                beanClassPattern = null;
            } else if (methodNameDelimiterIndex > 0) {
                methodNamePattern = beanClassPattern.substring(methodNameDelimiterIndex + 1);
                beanClassPattern = beanClassPattern.substring(0, methodNameDelimiterIndex);
            }
        }

        if (transletNamePattern != null && !transletNamePattern.isEmpty()) {
            ppr.setTransletNamePattern(transletNamePattern);
        }

        if (beanClassPattern != null) {
            if (beanClassPattern.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
                String className = beanClassPattern.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
                if (!className.isEmpty()) {
                    ppr.setClassNamePattern(className);
                }
            } else {
                if (!beanClassPattern.isEmpty()) {
                    ppr.setBeanIdPattern(beanClassPattern);
                }
            }
        }

        if (methodNamePattern != null && !methodNamePattern.isEmpty()) {
            ppr.setMethodNamePattern(methodNamePattern);
        }

        return ppr;
    }

    public static PointcutPatternRule newInstance(String translet, String bean, String method) {
        PointcutPatternRule ppr = new PointcutPatternRule();

        if (translet != null && !translet.isEmpty()) {
            ppr.setTransletNamePattern(translet);
        }

        if (bean != null && !bean.isEmpty()) {
            if (bean.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
                String className = bean.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
                if (!className.isEmpty()) {
                    ppr.setClassNamePattern(className);
                }
            } else {
                ppr.setBeanIdPattern(bean);
            }
        }

        if (method != null && !method.isEmpty()) {
            ppr.setMethodNamePattern(method);
        }

        return ppr;
    }

}
