/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.lang.NonNull;

import java.util.Objects;

public class PointcutPattern {

    private static final char POINTCUT_BEAN_NAME_DELIMITER = '@';

    private static final char POINTCUT_METHOD_NAME_DELIMITER = '^';

    private final String transletNamePattern;

    private final String beanIdPattern;

    private final String classNamePattern;

    private final String methodNamePattern;

    private volatile int hashCode;

    public PointcutPattern(String transletNamePattern, String beanIdPattern,
                           String classNamePattern, String methodNamePattern) {
        this.transletNamePattern = transletNamePattern;
        this.beanIdPattern = beanIdPattern;
        this.classNamePattern = classNamePattern;
        this.methodNamePattern = methodNamePattern;
    }

    public String getTransletNamePattern() {
        return transletNamePattern;
    }

    public String getBeanIdPattern() {
        return beanIdPattern;
    }

    public String getClassNamePattern() {
        return classNamePattern;
    }

    public String getMethodNamePattern() {
        return methodNamePattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PointcutPattern)) {
            return false;
        }
        PointcutPattern pp = (PointcutPattern)o;
        return (Objects.equals(transletNamePattern, pp.getTransletNamePattern()) &&
                Objects.equals(beanIdPattern, pp.getBeanIdPattern()) &&
                Objects.equals(classNamePattern, pp.getClassNamePattern()) &&
                Objects.equals(methodNamePattern, pp.getMethodNamePattern()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = hashCode;
        if (result == 0) {
            result = 11;
            result = prime * result + (transletNamePattern != null ? transletNamePattern.hashCode() : 0);
            result = prime * result + (beanIdPattern != null ? beanIdPattern.hashCode() : 0);
            result = prime * result + (classNamePattern != null ? classNamePattern.hashCode() : 0);
            result = prime * result + (methodNamePattern != null ? methodNamePattern.hashCode() : 0);
            hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return combinePattern(this);
    }

    public static String combinePattern(@NonNull PointcutPattern pointcutPattern) {
        return combinePattern(pointcutPattern.getTransletNamePattern(), pointcutPattern.getBeanIdPattern(),
                pointcutPattern.getClassNamePattern(), pointcutPattern.getMethodNamePattern());
    }

    public static String combinePattern(String transletName, String beanId, String className, String methodName) {
        int len = 0;
        if (transletName != null && !transletName.isEmpty()) {
            len += transletName.length();
        }
        if (beanId != null && !beanId.isEmpty()) {
            len += beanId.length() + 1;
        } else if (className != null && !className.isEmpty()) {
            len += className.length() + 7;
        }
        if (methodName != null && !methodName.isEmpty()) {
            len += methodName.length() + 1;
        }

        StringBuilder sb = new StringBuilder(len);
        if (transletName != null && !transletName.isEmpty()) {
            sb.append(transletName);
        }
        if (beanId != null && !beanId.isEmpty()) {
            sb.append(POINTCUT_BEAN_NAME_DELIMITER);
            sb.append(beanId);
        } else if (className != null && !className.isEmpty()) {
            sb.append(POINTCUT_BEAN_NAME_DELIMITER);
            sb.append(BeanRule.CLASS_DIRECTIVE_PREFIX);
            sb.append(className);
        }
        if (methodName != null && !methodName.isEmpty()) {
            sb.append(POINTCUT_METHOD_NAME_DELIMITER);
            sb.append(methodName);
        }
        return sb.toString();
    }

    public static PointcutPattern parsePattern(String patternString) {
        String transletNamePattern = null;
        String beanNamePattern = null;
        String methodNamePattern = null;
        int beanNameDelimiterIndex = patternString.indexOf(POINTCUT_BEAN_NAME_DELIMITER);
        if (beanNameDelimiterIndex == -1) {
            transletNamePattern = patternString;
        } else if (beanNameDelimiterIndex == 0) {
            beanNamePattern = patternString.substring(1);
        } else {
            transletNamePattern = patternString.substring(0, beanNameDelimiterIndex);
            beanNamePattern = patternString.substring(beanNameDelimiterIndex + 1);
        }
        if (beanNamePattern != null) {
            int methodNameDelimiterIndex = beanNamePattern.indexOf(POINTCUT_METHOD_NAME_DELIMITER);
            if (methodNameDelimiterIndex == 0) {
                methodNamePattern = beanNamePattern.substring(1);
                beanNamePattern = null;
            } else if (methodNameDelimiterIndex > 0) {
                methodNamePattern = beanNamePattern.substring(methodNameDelimiterIndex + 1);
                beanNamePattern = beanNamePattern.substring(0, methodNameDelimiterIndex);
            }
        }
        String beanIdPattern = null;
        String classNamePattern = null;
        if (beanNamePattern != null) {
            if (beanNamePattern.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
                classNamePattern = beanNamePattern.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
            } else {
                beanIdPattern = beanNamePattern;
            }
        }
        return new PointcutPattern(transletNamePattern, beanIdPattern, classNamePattern, methodNamePattern);
    }

}
