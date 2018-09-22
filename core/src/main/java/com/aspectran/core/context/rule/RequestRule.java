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

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.ToStringBuilder;

import java.nio.charset.Charset;

/**
 * The Class RequestRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class RequestRule {

    public static final String CHARACTER_ENCODING_SETTING_NAME = "characterEncoding";

    public static final String LOCALE_RESOLVER_SETTING_NAME = "localeResolver";

    public static final String LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME = "localeChangeInterceptor";

    private boolean implicit;

    /**
     * The request encoding is the character encoding in which parameters
     * in an incoming request are interpreted.
     */
    private String encoding;

    private MethodType allowedMethod;

    private ItemRuleMap parameterItemRuleMap;

    private ItemRuleMap attributeItemRuleMap;

    public RequestRule() {
    }

    /**
     * Gets whether the request rule is implicitly generated.
     *
     * @return true if this request rule is implicit; false otherwise
     */
    public boolean isImplicit() {
        return implicit;
    }

    /**
     * Sets whether the request rule is implicitly generated.
     *
     * @param implicit whether this request rule is implicit
     */
    protected void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    /**
     * Gets the request encoding.
     *
     * @return the request encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the request encoding.
     *
     * @param encoding the new request encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the method allowed on the requested resource.
     *
     * @return the allowed method
     */
    public MethodType getAllowedMethod() {
        return allowedMethod;
    }

    /**
     * Sets the method allowed on the requested resource.
     *
     * @param allowedMethod the new allowed method
     */
    public void setAllowedMethod(MethodType allowedMethod) {
        this.allowedMethod = allowedMethod;
    }

    /**
     * Gets the parameter item rule map.
     *
     * @return the parameter item rule map
     */
    public ItemRuleMap getParameterItemRuleMap() {
        return parameterItemRuleMap;
    }

    /**
     * Sets the attribute item rule map.
     *
     * @param parameterItemRuleMap the new attribute item rule map
     */
    public void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap) {
        this.parameterItemRuleMap = parameterItemRuleMap;
    }

    /**
     * Adds a new parameter rule with the specified name and returns it.
     *
     * @param parameterName the parameter name
     * @return the parameter item rule
     */
    public ItemRule newParameterItemRule(String parameterName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(parameterName);
        addParameterItemRule(itemRule);
        return itemRule;
    }

    /**
     * Adds the parameter item rule.
     *
     * @param parameterItemRule the parameter item rule
     */
    public void addParameterItemRule(ItemRule parameterItemRule) {
        if (parameterItemRuleMap == null) {
            parameterItemRuleMap = new ItemRuleMap();
        }
        parameterItemRuleMap.putItemRule(parameterItemRule);
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
     * @param attributeName the parameter name
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
     * @param attributeItemRule the attribute item rule
     */
    public void addAttributeItemRule(ItemRule attributeItemRule) {
        if (attributeItemRuleMap == null) {
            attributeItemRuleMap = new ItemRuleMap();
        }
        attributeItemRuleMap.putItemRule(attributeItemRule);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("method", allowedMethod);
        tsb.append("encoding", encoding);
        tsb.append("parameters", parameterItemRuleMap);
        tsb.append("attributes", attributeItemRuleMap);
        return tsb.toString();
    }

    public static RequestRule newInstance(boolean implicit) {
        RequestRule requestRule = new RequestRule();
        requestRule.setImplicit(implicit);
        return requestRule;
    }

    public static RequestRule newInstance(String allowedMethod, String encoding) throws IllegalRuleException {
        MethodType allowedMethodType = null;
        if (allowedMethod != null) {
            allowedMethodType = MethodType.resolve(allowedMethod);
            if (allowedMethodType == null) {
                throw new IllegalRuleException("No request method type for '" + allowedMethod + "'");
            }
        }

        if (encoding != null) {
            try {
                Charset.forName(encoding);
            } catch (Exception e) {
                throw new IllegalRuleException("Unsupported character encoding name: " + encoding, e);
            }
        }

        RequestRule requestRule = new RequestRule();
        requestRule.setAllowedMethod(allowedMethodType);
        requestRule.setEncoding(encoding);
        return requestRule;
    }

}
