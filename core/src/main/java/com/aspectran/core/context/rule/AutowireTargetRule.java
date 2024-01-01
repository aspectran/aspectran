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

import com.aspectran.core.context.expr.ExpressionEvaluation;
import com.aspectran.utils.ToStringBuilder;

import java.lang.reflect.Array;

/**
 * The Class AutowireTargetRule.
 *
 * <p>Created: 2021/02/02</p>
 */
public class AutowireTargetRule {

    private Class<?> type;

    private String qualifier;

    private ExpressionEvaluation expressionEvaluation;

    private boolean innerBean;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public ExpressionEvaluation getExpressionEvaluation() {
        return expressionEvaluation;
    }

    public void setExpression(String expression) throws IllegalRuleException {
        this.expressionEvaluation = new ExpressionEvaluation(expression);
    }

    public boolean isInnerBean() {
        return innerBean;
    }

    public void setInnerBean(boolean innerBean) {
        this.innerBean = innerBean;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", type);
        tsb.append("qualifier", qualifier);
        if (expressionEvaluation != null) {
            tsb.append("expression", expressionEvaluation.getExpression());
        }
        return tsb.toString();
    }

    public static AutowireTargetRule newInstance() {
        return new AutowireTargetRule();
    }

    public static AutowireTargetRule[] newArrayInstance(int size) {
        AutowireTargetRule[] arr = (AutowireTargetRule[])Array.newInstance(AutowireTargetRule.class, size);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new AutowireTargetRule();
        }
        return arr;
    }

}
