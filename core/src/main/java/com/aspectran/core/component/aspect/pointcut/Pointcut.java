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

import java.util.List;

/**
 * Represents a pointcut that determines if advice should be applied to a given join point.
 *
 * <p>A pointcut is composed of one or more patterns that match against join point
 * characteristics such as translet name, bean ID, class name, and method name.
 * </p>
 */
public interface Pointcut {

    /**
     * Returns the list of pointcut pattern rules that define this pointcut.
     * @return the list of pointcut pattern rules
     */
    List<PointcutPatternRule> getPointcutPatternRuleList();

    /**
     * Checks if any of the pointcut patterns include a method name pattern.
     * @return true if a method name pattern exists, false otherwise
     */
    boolean hasMethodNamePattern();

    /**
     * Performs a full match, including exclusions, against the given translet name.
     * @param transletName the name of the translet to match
     * @return true if it matches, false otherwise
     */
    boolean matches(String transletName);

    /**
     * Performs a full match, including exclusions, against the given join point attributes.
     * @param transletName the name of the translet to match
     * @param beanId the ID of the bean to match
     * @param className the name of the class to match
     * @return true if it matches, false otherwise
     */
    boolean matches(String transletName, String beanId, String className);

    /**
     * Performs a full match, including exclusions, against the given join point attributes.
     * @param transletName the name of the translet to match
     * @param beanId the ID of the bean to match
     * @param className the name of the class to match
     * @param methodName the name of the method to match
     * @return true if it matches, false otherwise
     */
    boolean matches(String transletName, String beanId, String className, String methodName);

    /**
     * Performs a full match, including exclusions, against the given pointcut pattern.
     * @param pointcutPattern the pointcut pattern to match
     * @return true if it matches, false otherwise
     */
    boolean matches(PointcutPattern pointcutPattern);

    /**
     * Checks if any inclusion pattern in this pointcut could potentially match the
     * given translet name. This check does not consider exclusion patterns.
     * @param transletName the name of the translet
     * @return true if a potential match exists, false otherwise
     */
    boolean exists(String transletName);

    /**
     * Checks if any inclusion pattern in this pointcut could potentially match the
     * given join point attributes. This check does not consider exclusion patterns.
     * @param transletName the name of the translet
     * @param beanId the ID of the bean
     * @param className the name of the class
     * @return true if a potential match exists, false otherwise
     */
    boolean exists(String transletName, String beanId, String className);

    /**
     * Checks if any inclusion pattern in this pointcut could potentially match the
     * given join point attributes. This check does not consider exclusion patterns.
     * @param transletName the name of the translet
     * @param beanId the ID of the bean
     * @param className the name of the class
     * @param methodName the name of the method
     * @return true if a potential match exists, false otherwise
     */
    boolean exists(String transletName, String beanId, String className, String methodName);

    /**
     * Checks if any inclusion pattern in this pointcut could potentially match the
     * given pointcut pattern. This check does not consider exclusion patterns.
     * @param pointcutPattern the pointcut pattern to check
     * @return true if a potential match exists, false otherwise
     */
    boolean exists(PointcutPattern pointcutPattern);

    /**
     * A utility method to check if a given string matches a pattern.
     * @param patternString the pattern to match against
     * @param compareString the string to check
     * @return true if the string matches the pattern, false otherwise
     */
    boolean patternMatches(String patternString, String compareString);

    /**
     * A utility method to check if a given string matches a pattern, using a specific separator.
     * @param patternString the pattern to match against
     * @param compareString the string to check
     * @param separator the separator character for the pattern
     * @return true if the string matches the pattern, false otherwise
     */
    boolean patternMatches(String patternString, String compareString, char separator);

    /**
     * Clears the cached pointcut patterns from this pointcut.
     */
    void clear();

}
