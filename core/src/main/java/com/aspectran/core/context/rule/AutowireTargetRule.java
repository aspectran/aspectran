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

import com.aspectran.core.context.asel.value.ValueExpression;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Array;

/**
 * Specifies the target for a single autowiring operation, including its type and qualifier.
 *
 * <p>Created: 2021/02/02</p>
 */
public class AutowireTargetRule {

    private Class<?> type;

    private String qualifier;

    private ValueExpression valueExpression;

    private boolean optional;

    private boolean innerBean;

    /**
     * Gets the type of the target to be autowired.
     * @return the target type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Sets the type of the target to be autowired.
     * @param type the target type
     */
    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * Gets the qualifier for the target bean.
     * @return the qualifier
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * Sets the qualifier for the target bean.
     * @param qualifier the qualifier
     */
    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    /**
     * Gets the value expression for the target.
     * @return the value expression
     */
    public ValueExpression getValueExpression() {
        return valueExpression;
    }

    /**
     * Sets the value expression for the target.
     * @param expression the expression string
     * @throws IllegalRuleException if the expression is invalid
     */
    public void setExpression(String expression) throws IllegalRuleException {
        this.valueExpression = new ValueExpression(expression);
    }

    /**
     * Returns whether this autowiring target is optional.
     * @return true if optional, false otherwise
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Sets whether this autowiring target is optional.
     * @param optional true if optional
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * Returns whether the target is an inner bean.
     * @return true if it is an inner bean, false otherwise
     */
    public boolean isInnerBean() {
        return innerBean;
    }

    /**
     * Sets whether the target is an inner bean.
     * @param innerBean true if it is an inner bean
     */
    public void setInnerBean(boolean innerBean) {
        this.innerBean = innerBean;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", type);
        tsb.append("qualifier", qualifier);
        if (valueExpression != null) {
            tsb.append("expression", valueExpression.getExpressionString());
        }
        return tsb.toString();
    }

    /**
     * Creates a new instance of AutowireTargetRule.
     * @return a new AutowireTargetRule instance
     */
    @NonNull
    public static AutowireTargetRule newInstance() {
        return new AutowireTargetRule();
    }

    /**
     * Creates a new array of AutowireTargetRule.
     * @param size the size of the array
     * @return a new array of AutowireTargetRule
     */
    public static AutowireTargetRule[] newArrayInstance(int size) {
        AutowireTargetRule[] arr = (AutowireTargetRule[])Array.newInstance(AutowireTargetRule.class, size);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new AutowireTargetRule();
        }
        return arr;
    }

}
