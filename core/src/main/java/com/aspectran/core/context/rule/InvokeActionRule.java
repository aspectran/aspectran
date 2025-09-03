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

import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.ability.HasArguments;
import com.aspectran.core.context.rule.ability.HasProperties;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Method;

/**
 * Rule for an action that invokes a method on a bean.
 * This is the most common type of action, used to execute business logic.
 *
 * <p>Created: 2008. 03. 22 PM 5:50:35</p>
 */
public class InvokeActionRule implements BeanReferenceable, HasArguments, HasProperties {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.BEAN_METHOD_ACTION_RULE;

    private String actionId;

    private String beanId;

    private Class<?> beanClass;

    private String methodName;

    private Method method;

    private boolean requiresTranslet;

    private ItemRuleMap argumentItemRuleMap;

    private ItemRuleMap propertyItemRuleMap;

    private Boolean hidden;

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
     * Gets the bean id or class name.
     * @return the bean id or class name
     */
    public String getBeanId() {
        return beanId;
    }

    /**
     * Sets the bean id or class name.
     * @param beanId the bean id or class name
     */
    public void setBeanId(String beanId) {
        this.beanId = beanId;
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
     * @param beanClass the bean class
     */
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Gets the method name to invoke.
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name to invoke.
     * @param methodName the new method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Gets the resolved method to invoke.
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Sets the resolved method to invoke.
     * @param method the method
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Returns whether the action method requires a {@code Translet} as a parameter.
     * @return true if the method requires a Translet, false otherwise
     */
    public boolean isRequiresTranslet() {
        return requiresTranslet;
    }

    /**
     * Sets whether the action method requires a {@code Translet} as a parameter.
     * @param requiresTranslet true if the method requires a Translet
     */
    public void setRequiresTranslet(boolean requiresTranslet) {
        this.requiresTranslet = requiresTranslet;
    }

    /**
     * Returns whether to hide the result of the action.
     * @return true, if this action is hidden
     */
    public Boolean getHidden() {
        return hidden;
    }

    /**
     * Returns whether to hide the result of the action.
     * @return true, if this action is hidden
     */
    public boolean isHidden() {
        return BooleanUtils.toBoolean(hidden);
    }

    /**
     * Sets whether to hide the result of the action.
     * @param hidden whether to hide the result of the action
     */
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public ItemRuleMap getArgumentItemRuleMap() {
        return argumentItemRuleMap;
    }

    @Override
    public void setArgumentItemRuleMap(ItemRuleMap argumentItemRuleMap) {
        this.argumentItemRuleMap = argumentItemRuleMap;
    }

    @Override
    public void addArgumentItemRule(ItemRule argumentItemRule) {
        if (argumentItemRuleMap == null) {
            argumentItemRuleMap = new ItemRuleMap();
        }
        argumentItemRuleMap.putItemRule(argumentItemRule);
    }

    @Override
    public ItemRuleMap getPropertyItemRuleMap() {
        return propertyItemRuleMap;
    }

    @Override
    public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        this.propertyItemRuleMap = propertyItemRuleMap;
    }

    @Override
    public void addPropertyItemRule(ItemRule propertyItemRule) {
        if (propertyItemRuleMap == null) {
            propertyItemRuleMap = new ItemRuleMap();
        }
        propertyItemRuleMap.putItemRule(propertyItemRule);
    }

    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", actionId);
        tsb.append("bean", beanId);
        if (method != null) {
            tsb.append("method", method);
        } else {
            tsb.append("method", methodName);
        }
        if (argumentItemRuleMap != null) {
            tsb.append("arguments", argumentItemRuleMap.keySet());
        }
        if (propertyItemRuleMap != null) {
            tsb.append("properties", propertyItemRuleMap.keySet());
        }
        tsb.append("hidden", hidden);
        return tsb.toString();
    }

    /**
     * Creates a new instance of InvokeActionRule.
     * @param id the action id
     * @param beanId the bean id
     * @param methodName the method name
     * @param hidden true if hiding the result of the action; false otherwise
     * @return the new invoke action rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    @NonNull
    public static InvokeActionRule newInstance(String id, String beanId, String methodName, Boolean hidden)
            throws IllegalRuleException {
        if (beanId == null) {
            throw new IllegalRuleException("The 'action' element requires a 'bean' attribute");
        }
        if (methodName == null) {
            throw new IllegalRuleException("The 'action' element requires a 'method' attribute");
        }

        InvokeActionRule invokeActionRule = new InvokeActionRule();
        invokeActionRule.setActionId(id);
        invokeActionRule.setBeanId(beanId);
        invokeActionRule.setMethodName(methodName);
        invokeActionRule.setHidden(hidden);
        return invokeActionRule;
    }

    /**
     * Creates a new instance of InvokeActionRule for an advice bean.
     * @param methodName the method name
     * @param hidden true if hiding the result of the action; false otherwise
     * @return the new invoke action rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    @NonNull
    public static InvokeActionRule newInstance(String methodName, Boolean hidden)
            throws IllegalRuleException {
        if (methodName == null) {
            throw new IllegalRuleException("The 'action' element requires a 'method' attribute");
        }

        InvokeActionRule invokeActionRule = new InvokeActionRule();
        invokeActionRule.setMethodName(methodName);
        invokeActionRule.setHidden(hidden);
        return invokeActionRule;
    }

}
