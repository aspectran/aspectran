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

import com.aspectran.core.context.rule.BeanRule;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * Represents a single pattern within a pointcut, defining criteria for matching
 * translet names, bean IDs, class names, and method names.
 *
 * <p>This class is used to specify a precise target for aspect advice.
 * The pointcut pattern string has the following format, where each part is optional:
 * {@code transletNamePattern[@beanOrClassPattern][^methodNamePattern]}
 * </p>
 */
public class PointcutPattern {

    /** The delimiter character used to separate the translet name pattern from the bean/class pattern. */
    private static final char POINTCUT_BEAN_NAME_DELIMITER = '@';

    /** The delimiter character used to separate the bean/class name pattern from the method name pattern. */
    private static final char POINTCUT_METHOD_NAME_DELIMITER = '^';

    /** The pattern for matching translet names. */
    private final String transletNamePattern;

    /** The pattern for matching bean IDs. */
    private final String beanIdPattern;

    /** The pattern for matching class names. */
    private final String classNamePattern;

    /** The pattern for matching method names. */
    private final String methodNamePattern;

    private volatile int hashCode;

    /**
     * Creates a new PointcutPattern with the specified matching criteria.
     * @param transletNamePattern the pattern for translet names
     * @param beanIdPattern the pattern for bean IDs
     * @param classNamePattern the pattern for class names
     * @param methodNamePattern the pattern for method names
     */
    public PointcutPattern(String transletNamePattern, String beanIdPattern,
                           String classNamePattern, String methodNamePattern) {
        this.transletNamePattern = transletNamePattern;
        this.beanIdPattern = beanIdPattern;
        this.classNamePattern = classNamePattern;
        this.methodNamePattern = methodNamePattern;
    }

    /**
     * Returns the pattern for matching translet names.
     * @return the translet name pattern
     */
    public String getTransletNamePattern() {
        return transletNamePattern;
    }

    /**
     * Returns the pattern for matching bean IDs.
     * @return the bean ID pattern
     */
    public String getBeanIdPattern() {
        return beanIdPattern;
    }

    /**
     * Returns the pattern for matching class names.
     * @return the class name pattern
     */
    public String getClassNamePattern() {
        return classNamePattern;
    }

    /**
     * Returns the pattern for matching method names.
     * @return the method name pattern
     */
    public String getMethodNamePattern() {
        return methodNamePattern;
    }

    /**
     * {@inheritDoc}
     * <p>Compares this PointcutPattern to the specified object. The result is true if and only if
     * the argument is not null and is a PointcutPattern object that has the same pattern values.
     * </p>
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PointcutPattern that)) {
            return false;
        }
        return (Objects.equals(transletNamePattern, that.getTransletNamePattern()) &&
                Objects.equals(beanIdPattern, that.getBeanIdPattern()) &&
                Objects.equals(classNamePattern, that.getClassNamePattern()) &&
                Objects.equals(methodNamePattern, that.getMethodNamePattern()));
    }

    /**
     * {@inheritDoc}
     * <p>Returns a hash code for this PointcutPattern. The hash code is computed based on the pattern values.</p>
     */
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(transletNamePattern, beanIdPattern, classNamePattern, methodNamePattern);
            hashCode = result;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>Returns a string representation of this PointcutPattern by combining its individual patterns.</p>
     */
    @Override
    public String toString() {
        return combinePattern(this);
    }

    /**
     * Combines the patterns from a {@link PointcutPattern} object into a single string.
     * @param pointcutPattern the pointcut pattern object
     * @return the combined pattern string
     */
    @NonNull
    public static String combinePattern(@NonNull PointcutPattern pointcutPattern) {
        return combinePattern(pointcutPattern.getTransletNamePattern(), pointcutPattern.getBeanIdPattern(),
                pointcutPattern.getClassNamePattern(), pointcutPattern.getMethodNamePattern());
    }

    /**
     * Combines individual pattern strings into a single pointcut pattern string.
     * The format is typically `transletName@beanId^methodName` or `transletName@class:className^methodName`.
     * @param transletName the translet name pattern
     * @param beanId the bean ID pattern
     * @param className the class name pattern
     * @param methodName the method name pattern
     * @return the combined pattern string
     */
    @NonNull
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

    /**
     * Parses a combined pointcut pattern string into a {@link PointcutPattern} object.
     * <p>
     * The parser interprets the pattern based on the presence of '{@code @}' and '{@code ^}' delimiters.
     * <ul>
     *   <li>If '{@code @}' is absent, the entire string is treated as a translet name pattern.</li>
     *   <li>If '{@code @}' is present, it separates the translet name pattern from the bean/class pattern.</li>
     *   <li>If '{@code ^}' is present, it separates the bean/class pattern from the method name pattern.</li>
     * </ul>
     * This leads to the following valid formats:
     * <ul>
     *   <li>{@code transletPattern} (e.g., "/a/b/c")</li>
     *   <li>{@code transletPattern@beanPattern} (e.g., "/a/b@myBean")</li>
     *   <li>{@code transletPattern@beanPattern^methodPattern} (e.g., "/a/b@myBean^myMethod")</li>
     *   <li>{@code @beanPattern} (targets a bean in any translet)</li>
     *   <li>{@code @beanPattern^methodPattern} (targets a method of a bean in any translet)</li>
     *   <li>{@code @^methodPattern} (targets a method in any bean in any translet)</li>
     * </ul>
     * The bean pattern can also specify a class directly using the "class:" prefix (e.g., "@class:com.example.MyClass^myMethod").
     * </p>
     * @param patternString the combined pattern string to parse
     * @return a new {@link PointcutPattern} object
     */
    @NonNull
    public static PointcutPattern parsePattern(@NonNull String patternString) {
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
