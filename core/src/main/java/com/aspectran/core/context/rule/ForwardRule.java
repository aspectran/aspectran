/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class ForwardRule.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class ForwardRule implements Replicable<ForwardRule> {

    public static final ResponseType RESPONSE_TYPE = ResponseType.FORWARD;

    private String contentType;

    private String transletName;

    private MethodType requestMethod;

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

    public MethodType getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
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
    public ForwardRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("type", RESPONSE_TYPE);
        tsb.append("translet", transletName);
        tsb.append("method", requestMethod);
        tsb.append("contentType", contentType);
        tsb.append("default", getDefaultResponse());
        return tsb.toString();
    }

    /**
     * Returns a new instance of ForwardRule.
     *
     * @param contentType the content type
     * @param transletName the translet name
     * @param method the request method
     * @param defaultResponse whether the default response
     * @return an instance of ForwardRule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ForwardRule newInstance(String contentType, String transletName, String method, Boolean defaultResponse)
            throws IllegalRuleException {
        if (transletName == null) {
            throw new IllegalRuleException("The 'forward' element requires a 'translet' attribute");
        }

        MethodType requestMethod = null;
        if (method != null) {
            requestMethod = MethodType.resolve(method);
            if (requestMethod == null) {
                throw new IllegalRuleException("No request method type for '" + method + "'");
            }
        }

        ForwardRule fr = new ForwardRule();
        fr.setContentType(contentType);
        fr.setTransletName(transletName);
        fr.setRequestMethod(requestMethod);
        fr.setDefaultResponse(defaultResponse);
        return fr;
    }

    /**
     * Returns a new instance of ForwardRule.
     *
     * @param transletName the translet name
     * @return an instance of ForwardRule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ForwardRule newInstance(String transletName) throws IllegalRuleException {
        if (transletName == null) {
            throw new IllegalRuleException("transletName must not be null");
        }

        ForwardRule fr = new ForwardRule();
        fr.setTransletName(transletName);
        return fr;
    }

    public static ForwardRule replicate(ForwardRule forwardRule) {
        ForwardRule fr = new ForwardRule();
        fr.setContentType(forwardRule.getContentType());
        fr.setTransletName(forwardRule.getTransletName());
        fr.setAttributeItemRuleMap(forwardRule.getAttributeItemRuleMap());
        fr.setDefaultResponse(forwardRule.getDefaultResponse());
        return fr;
    }

}
