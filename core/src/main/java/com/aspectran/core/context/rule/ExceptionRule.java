/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ExceptionRule.
 *
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public class ExceptionRule {

    private final List<ExceptionThrownRule> exceptionThrownRuleList = new ArrayList<>();

    private final Map<String, ExceptionThrownRule> exceptionThrownRuleMap = new LinkedHashMap<>();

    private ExceptionThrownRule defaultExceptionThrownRule;

    private DescriptionRule descriptionRule;

    public List<ExceptionThrownRule> getExceptionThrownRuleList() {
        return exceptionThrownRuleList;
    }

    public Map<String, ExceptionThrownRule> getExceptionThrownRuleMap() {
        return exceptionThrownRuleMap;
    }

    public ExceptionThrownRule getDefaultExceptionThrownRule() {
        return defaultExceptionThrownRule;
    }

    /**
     * Puts the exception thrown rule.
     * @param exceptionThrownRule the exception thrown rule
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
     * Gets the exception thrown rule as specified exception.
     * @param ex the exception
     * @return the exception thrown rule
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

    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

}
