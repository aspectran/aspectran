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
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.nio.charset.Charset;

/**
 * Defines rules for processing an incoming request, such as allowed methods,
 * character encoding, and definitions for parameters and attributes.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class RequestRule implements HasParameters, HasAttributes {

    public static final String CHARACTER_ENCODING_SETTING_NAME = "characterEncoding";

    public static final String LOCALE_RESOLVER_SETTING_NAME = "localeResolver";

    public static final String LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME = "localeChangeInterceptor";

    private final boolean explicit;

    private MethodType allowedMethod;

    /**
     * The request encoding is the character encoding in which parameters
     * in an incoming request are interpreted.
     */
    private String encoding;

    private ItemRuleMap parameterItemRuleMap;

    private ItemRuleMap attributeItemRuleMap;

    /**
     * Instantiates a new RequestRule.
     * @param explicit whether this request rule is explicit
     */
    public RequestRule(boolean explicit) {
        this.explicit = explicit;
    }

    /**
     * Returns whether the request rule was explicitly defined in the configuration.
     * @return true if this request rule is explicit; false otherwise
     */
    public boolean isExplicit() {
        return explicit;
    }

    /**
     * Returns the HTTP method allowed for the request.
     * @return the allowed method
     */
    public MethodType getAllowedMethod() {
        return allowedMethod;
    }

    /**
     * Sets the HTTP method allowed for the request.
     * @param allowedMethod the new allowed method
     */
    public void setAllowedMethod(MethodType allowedMethod) {
        this.allowedMethod = allowedMethod;
    }

    /**
     * Gets the character encoding for the request.
     * @return the request encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding for the request.
     * @param encoding the new request encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public ItemRuleMap getParameterItemRuleMap() {
        return parameterItemRuleMap;
    }

    @Override
    public void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap) {
        this.parameterItemRuleMap = parameterItemRuleMap;
    }

    @Override
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

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("method", allowedMethod);
        tsb.append("encoding", encoding);
        tsb.append("parameters", parameterItemRuleMap);
        tsb.append("attributes", attributeItemRuleMap);
        return tsb.toString();
    }

    /**
     * Creates a new instance of RequestRule.
     * @param explicit whether this request rule is explicit
     * @return a new RequestRule instance
     */
    @NonNull
    public static RequestRule newInstance(boolean explicit) {
        return new RequestRule(explicit);
    }

    /**
     * Creates a new instance of RequestRule.
     * @param allowedMethod the allowed HTTP method
     * @param encoding the character encoding
     * @return a new RequestRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
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

        RequestRule requestRule = new RequestRule(true);
        requestRule.setAllowedMethod(allowedMethodType);
        requestRule.setEncoding(encoding);
        return requestRule;
    }

}
