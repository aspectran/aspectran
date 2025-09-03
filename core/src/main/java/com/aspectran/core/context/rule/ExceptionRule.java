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

import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a set of rules for handling exceptions.
 * It contains a list of {@link ExceptionThrownRule}s that map specific exception types
 * to corresponding actions or responses.
 *
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public class ExceptionRule implements Describable {

    private final List<ExceptionThrownRule> exceptionThrownRuleList = new ArrayList<>();

    private final Map<String, ExceptionThrownRule> exceptionThrownRuleMap = new LinkedHashMap<>();

    private ExceptionThrownRule defaultExceptionThrownRule;

    private DescriptionRule descriptionRule;

    /**
     * Gets the list of all exception thrown rules.
     * @return the list of exception thrown rules
     */
    public List<ExceptionThrownRule> getExceptionThrownRuleList() {
        return exceptionThrownRuleList;
    }

    /**
     * Gets the map of exception types to their corresponding thrown rules.
     * @return the map of exception thrown rules
     */
    public Map<String, ExceptionThrownRule> getExceptionThrownRuleMap() {
        return exceptionThrownRuleMap;
    }

    /**
     * Gets the default exception thrown rule, which is used when no specific type matches.
     * @return the default exception thrown rule
     */
    public ExceptionThrownRule getDefaultExceptionThrownRule() {
        return defaultExceptionThrownRule;
    }

    /**
     * Adds an exception thrown rule.
     * @param exceptionThrownRule the exception thrown rule to add
     */
    public void putExceptionThrownRule(ExceptionThrownRule exceptionThrownRule) {
        exceptionThrownRuleList.add(exceptionThrownRule);

        String[] exceptionTypes = exceptionThrownRule.getExceptionTypes();
        if (exceptionTypes != null) {
            for (String exceptionType : exceptionTypes) {
                if (exceptionType != null) {
                    exceptionThrownRuleMap.putIfAbsent(exceptionType, exceptionThrownRule);
                }
            }
        } else {
            defaultExceptionThrownRule = exceptionThrownRule;
        }
    }

    /**
     * Finds the best matching {@link ExceptionThrownRule} for a given exception.
     * It traverses the exception cause chain to find the most specific match.
     * @param ex the exception to find a handler for
     * @return the matching exception thrown rule, or the default rule if no specific match is found
     */
    public ExceptionThrownRule getExceptionThrownRule(Throwable ex) {
        ExceptionThrownRule exceptionThrownRule = null;
        int deepest = Integer.MAX_VALUE;
        for (Map.Entry<String, ExceptionThrownRule> entry : exceptionThrownRuleMap.entrySet()) {
            int depth = getMatchedDepth(entry.getKey(), ex);
            if (depth >= 0 && depth < deepest) {
                deepest = depth;
                exceptionThrownRule = entry.getValue();
            }
        }
        return (exceptionThrownRule != null ? exceptionThrownRule : defaultExceptionThrownRule);
    }

    /**
     * Returns the matched depth.
     * @param exceptionType the exception type
     * @param ex the throwable exception
     * @return the matched depth
     */
    private int getMatchedDepth(String exceptionType, @NonNull Throwable ex) {
        Throwable t = ex.getCause();
        if (t != null) {
            return getMatchedDepth(exceptionType, t);
        } else {
            return getMatchedDepth(exceptionType, ex.getClass(), 0);
        }
    }

    /**
     * Returns the matched depth.
     * @param exceptionType the exception type
     * @param exceptionClass the exception class
     * @param depth the depth
     * @return the matched depth
     */
    private int getMatchedDepth(String exceptionType, @NonNull Class<?> exceptionClass, int depth) {
        if (exceptionClass.getName().equals(exceptionType)) {
            return depth;
        }
        if (exceptionClass.equals(Throwable.class)) {
            return -1;
        }
        return getMatchedDepth(exceptionType, exceptionClass.getSuperclass(), depth + 1);
    }

    @Override
    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    @Override
    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

}
