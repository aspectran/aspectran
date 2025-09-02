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

import com.aspectran.core.context.rule.ability.HasAttributes;
import com.aspectran.core.context.rule.ability.HasParameters;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * The Class IncludeActionRule.
 *
 * <p>Created: 2008. 06. 05 PM 9:25:40</p>
 */
public class IncludeActionRule implements HasParameters, HasAttributes {

    private String actionId;

    private String transletName;

    private MethodType methodType;

    private ItemRuleMap parameterItemRuleMap;

    private ItemRuleMap attributeItemRuleMap;

    private Boolean hidden;

    /**
     * Gets the action id.
     * @return the action id
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Sets the id.
     * @param actionId the new id
     */
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * Gets the translet name.
     * @return the translet name
     */
    public String getTransletName() {
        return transletName;
    }

    /**
     * Sets the translet name.
     * @param transletName the new translet name
     */
    public void setTransletName(String transletName) {
        this.transletName = transletName;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(MethodType methodType) {
        this.methodType = methodType;
    }

    /**
     * Gets the parameter item rule map.
     * @return the parameter item rule map
     */
    public ItemRuleMap getParameterItemRuleMap() {
        return parameterItemRuleMap;
    }

    /**
     * Sets the attribute item rule map.
     * @param parameterItemRuleMap the new attribute item rule map
     */
    public void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap) {
        this.parameterItemRuleMap = parameterItemRuleMap;
    }

    /**
     * Adds the parameter item rule.
     * @param parameterItemRule the parameter item rule
     */
    public void addParameterItemRule(ItemRule parameterItemRule) {
        if (parameterItemRuleMap == null) {
            parameterItemRuleMap = new ItemRuleMap();
        }
        parameterItemRuleMap.putItemRule(parameterItemRule);
    }

    @Override
    public ItemRuleMap getAttributeItemRuleMap() {
        return attributeItemRuleMap;
    }

    @Override
    public void setAttributeItemRuleMap(ItemRuleMap attributeItemRuleMap) {
        this.attributeItemRuleMap = attributeItemRuleMap;
    }

    @Override
    public void addAttributeItemRule(ItemRule attributeItemRule) {
        if (attributeItemRuleMap == null) {
            attributeItemRuleMap = new ItemRuleMap();
        }
        attributeItemRuleMap.putItemRule(attributeItemRule);
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
    public Boolean isHidden() {
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
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", actionId);
        tsb.append("translet", transletName);
        tsb.append("method", methodType);
        if (parameterItemRuleMap != null) {
            tsb.append("parameters", parameterItemRuleMap.keySet());
        }
        if (attributeItemRuleMap != null) {
            tsb.append("attributes", attributeItemRuleMap.keySet());
        }
        return tsb.toString();
    }

    /**
     * Returns a new instance of IncludeActionRule.
     * @param id the action id
     * @param transletName the translet name
     * @param method the request method type
     * @param hidden whether to hide the result of the action
     * @return the include action rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    @NonNull
    public static IncludeActionRule newInstance(String id, String transletName, String method, Boolean hidden)
            throws IllegalRuleException {
        if (transletName == null) {
            throw new IllegalRuleException("The 'include' element requires a 'translet' attribute");
        }

        MethodType methodType = MethodType.resolve(method);
        if (method != null && methodType == null) {
            throw new IllegalRuleException("No request method type for '" + method + "'");
        }

        IncludeActionRule includeActionRule = new IncludeActionRule();
        includeActionRule.setActionId(id);
        includeActionRule.setTransletName(transletName);
        includeActionRule.setMethodType(methodType);
        includeActionRule.setHidden(hidden);
        return includeActionRule;
    }

}
