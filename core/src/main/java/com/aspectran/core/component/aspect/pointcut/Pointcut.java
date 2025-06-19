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
 * The Interface Pointcut.
 */
public interface Pointcut {

    List<PointcutPatternRule> getPointcutPatternRuleList();

    boolean hasMethodNamePattern();

    boolean matches(String transletName);

    boolean matches(String transletName, String beanId, String className);

    boolean matches(String transletName, String beanId, String className, String methodName);

    boolean matches(PointcutPattern pointcutPattern);

    boolean exists(String transletName);

    boolean exists(String transletName, String beanId, String className);

    boolean exists(String transletName, String beanId, String className, String methodName);

    boolean exists(PointcutPattern pointcutPattern);

    boolean patternMatches(String patternString, String compareString);

    boolean patternMatches(String patternString, String compareString, char separator);

    void clear();

}
