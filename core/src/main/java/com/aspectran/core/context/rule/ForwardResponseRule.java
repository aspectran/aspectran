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

import com.aspectran.core.context.rule.ability.ActionPossessSupport;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class ForwardResponseRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class ForwardResponseRule extends ActionPossessSupport implements Replicable<ForwardResponseRule> {

    public static final ResponseType RESPONSE_TYPE = ResponseType.FORWARD;

    private String contentType;

    private String transletName;

    private ItemRuleMap attributeItemRuleMap;

    private Boolean defaultResponse;

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     *
     * @param contentType the new content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the translet name.
     *
     * @return the translet name
     */
    public String getTransletName() {
        return transletName;
    }

    /**
     * Sets the translet name.
     *
     * @param transletName the new translet name
     */
    public void setTransletName(String transletName) {
        this.transletName = transletName;
    }

    /**
     * Gets the attribute item rule map.
     *
     * @return the attribute item rule map
     */
    public ItemRuleMap getAttributeItemRuleMap() {
        return attributeItemRuleMap;
    }

    /**
     * Sets the attribute item rule map.
     *
     * @param attributeItemRuleMap the new attribute item rule map
     */
    public void setAttributeItemRuleMap(ItemRuleMap attributeItemRuleMap) {
        this.attributeItemRuleMap = attributeItemRuleMap;
    }

    /**
     * Adds a new attribute rule with the specified name and returns it.
     *
     * @param attributeName the attribute name
     * @return the attribute item rule
     */
    public ItemRule newAttributeItemRule(String attributeName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(attributeName);
        addAttributeItemRule(itemRule);
        return itemRule;
    }

    /**
     * Adds the attribute item rule.
     *
     * @param itemRule the attribute item rule
     */
    public void addAttributeItemRule(ItemRule itemRule) {
        if (attributeItemRuleMap == null) {
            attributeItemRuleMap = new ItemRuleMap();
        }
        attributeItemRuleMap.putItemRule(itemRule);
    }

    /**
     * Returns whether the default response.
     *
     * @return whether the default response
     */
    public Boolean getDefaultResponse() {
        return defaultResponse;
    }

    /**
     * Returns whether the default response.
     *
     * @return true, if is default response
     */
    public boolean isDefaultResponse() {
        return BooleanUtils.toBoolean(defaultResponse);
    }

    /**
     * Sets whether the default response.
     *
     * @param defaultResponse whether the default response
     */
    public void setDefaultResponse(Boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    @Override
    public ForwardResponseRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("responseType", RESPONSE_TYPE);
        tsb.append("translet", transletName);
        tsb.append("contentType", contentType);
        tsb.append("defaultResponse", defaultResponse);
        return tsb.toString();
    }

    /**
     * Returns a new instance of ForwardResponseRule.
     *
     * @param contentType the content type
     * @param transletName the translet name
     * @param defaultResponse whether the default response
     * @return an instance of ForwardResponseRule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ForwardResponseRule newInstance(String contentType, String transletName, Boolean defaultResponse)
            throws IllegalRuleException {
        if (transletName == null) {
            throw new IllegalRuleException("The 'forward' element requires a 'translet' attribute");
        }

        ForwardResponseRule frr = new ForwardResponseRule();
        frr.setContentType(contentType);
        frr.setTransletName(transletName);
        frr.setDefaultResponse(defaultResponse);
        return frr;
    }

    /**
     * Returns a new instance of ForwardResponseRule.
     *
     * @param transletName the translet name
     * @return an instance of ForwardResponseRule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ForwardResponseRule newInstance(String transletName) throws IllegalRuleException {
        if (transletName == null) {
            throw new IllegalRuleException("Argument 'transletName' must not be null");
        }

        ForwardResponseRule frr = new ForwardResponseRule();
        frr.setTransletName(transletName);
        return frr;
    }

    public static ForwardResponseRule replicate(ForwardResponseRule forwardResponseRule) {
        ForwardResponseRule frr = new ForwardResponseRule();
        frr.setContentType(forwardResponseRule.getContentType());
        frr.setTransletName(forwardResponseRule.getTransletName());
        frr.setAttributeItemRuleMap(forwardResponseRule.getAttributeItemRuleMap());
        frr.setDefaultResponse(forwardResponseRule.getDefaultResponse());
        frr.setActionList(forwardResponseRule.getActionList());
        return frr;
    }

}
