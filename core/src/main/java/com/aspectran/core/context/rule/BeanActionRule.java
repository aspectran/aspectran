/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.lang.reflect.Method;

/**
 * The Class BeanActionRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:50:35</p>
 */
public class BeanActionRule implements BeanReferenceInspectable {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.BEAN_ACTION_RULE;

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
     *
     * @return the action id
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Sets the action id.
     *
     * @param actionId the new action id
     */
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * Gets bean id.
     *
     * @return the bean id
     */
    public String getBeanId() {
        return beanId;
    }

    /**
     * Sets bean id.
     *
     * @param beanId the bean id
     */
    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Gets the action method name.
     *
     * @return the action method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the action method name.
     *
     * @param methodName the new action method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public boolean isRequiresTranslet() {
        return requiresTranslet;
    }

    public void setRequiresTranslet(boolean requiresTranslet) {
        this.requiresTranslet = requiresTranslet;
    }

    /**
     * Returns whether to hide result of the action.
     *
     * @return true, if this action is hidden
     */
    public Boolean getHidden() {
        return hidden;
    }

    /**
     * Returns whether to hide result of the action.
     *
     * @return true, if this action is hidden
     */
    public boolean isHidden() {
        return BooleanUtils.toBoolean(hidden);
    }

    /**
     * Sets whether to hide result of the action.
     *
     * @param hidden whether to hide result of the action
     */
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Gets the argument item rule map.
     *
     * @return the argument item rule map
     */
    public ItemRuleMap getArgumentItemRuleMap() {
        return argumentItemRuleMap;
    }

    /**
     * Sets the argument item rule map.
     *
     * @param argumentItemRuleMap the new argument item rule map
     */
    public void setArgumentItemRuleMap(ItemRuleMap argumentItemRuleMap) {
        this.argumentItemRuleMap = argumentItemRuleMap;
    }

    /**
     * Adds a new argument rule with the specified name and returns it.
     *
     * @param argumentName the argument name
     * @return the argument item rule
     */
    public ItemRule newArgumentItemRule(String argumentName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(argumentName);
        addArgumentItemRule(itemRule);
        return itemRule;
    }

    /**
     * Adds the argument item rule.
     *
     * @param argumentItemRule the new argument item rule
     */
    public void addArgumentItemRule(ItemRule argumentItemRule) {
        if (argumentItemRuleMap == null) {
            argumentItemRuleMap = new ItemRuleMap();
        }
        argumentItemRuleMap.putItemRule(argumentItemRule);
    }

    /**
     * Gets the property item rule map.
     *
     * @return the property item rule map
     */
    public ItemRuleMap getPropertyItemRuleMap() {
        return propertyItemRuleMap;
    }

    /**
     * Sets the property item rule map.
     *
     * @param propertyItemRuleMap the new property item rule map
     */
    public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        this.propertyItemRuleMap = propertyItemRuleMap;
    }

    /**
     * Adds a new property rule with the specified name and returns it.
     *
     * @param propertyName the property name
     * @return the property item rule
     */
    public ItemRule newPropertyItemRule(String propertyName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(propertyName);
        addPropertyItemRule(itemRule);
        return itemRule;
    }

    /**
     * Adds the property item rule.
     *
     * @param propertyItemRule the new property item rule
     */
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
        tsb.append("method", methodName);
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
     * Returns a new instance of BeanActionRule.
     *
     * @param id the action id
     * @param beanId the bean id
     * @param methodName the method name
     * @param hidden true if hiding the result of the action; false otherwise
     * @return the bean action rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static BeanActionRule newInstance(String id, String beanId, String methodName, Boolean hidden)
            throws IllegalRuleException {
        if (methodName == null) {
            throw new IllegalRuleException("The 'action' element requires an 'method' attribute");
        }

        BeanActionRule beanActionRule = new BeanActionRule();
        beanActionRule.setActionId(id);
        beanActionRule.setBeanId(beanId);
        beanActionRule.setMethodName(methodName);
        beanActionRule.setHidden(hidden);
        return beanActionRule;
    }

}
