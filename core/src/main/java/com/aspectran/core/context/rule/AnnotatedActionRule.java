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

import com.aspectran.utils.ToStringBuilder;

import java.lang.reflect.Method;

/**
 * Represents an action that is defined via annotations rather than XML.
 * This rule holds metadata about the annotated method, such as its ID and parameter bindings.
 *
 * <p>Created: 2016. 2. 10.</p>
 *
 * @since 2.0.0
 */
public class AnnotatedActionRule {

    private String actionId;

    private Class<?> beanClass;

    private Method method;

    private ParameterBindingRule[] parameterBindingRules;

    /**
     * Gets the action id.
     * @return the action id
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Sets the action id.
     * @param actionId the new action id
     */
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * Gets the bean class.
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the bean class.
     * @param beanClass the new bean class
     */
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Gets the action method.
     * @return the action method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Sets the action method.
     * @param method the new action method
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Gets the method name.
     * @return the method name
     */
    public String getMethodName() {
        return (method != null ? method.getName() : null);
    }

    /**
     * Gets the parameter binding rules.
     * @return the parameter binding rules
     */
    public ParameterBindingRule[] getParameterBindingRules() {
        return parameterBindingRules;
    }

    /**
     * Sets the parameter binding rules.
     * @param parameterBindingRules the new parameter binding rules
     */
    public void setParameterBindingRules(ParameterBindingRule[] parameterBindingRules) {
        this.parameterBindingRules = parameterBindingRules;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", actionId);
        tsb.append("method", method);
        return tsb.toString();
    }

}
