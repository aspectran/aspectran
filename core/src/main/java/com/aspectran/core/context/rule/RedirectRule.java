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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.rule.ability.HasParameters;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.Map;

/**
 * Defines a rule for sending a client-side redirect response to a new URL.
 * It is a type of response rule.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class RedirectRule implements Replicable<RedirectRule>, HasParameters {

    public static final ResponseType RESPONSE_TYPE = ResponseType.REDIRECT;

    private String contentType;

    private String path;

    private Token[] pathTokens;

    private String encoding;

    private Boolean excludeNullParameters;

    private Boolean excludeEmptyParameters;

    private ItemRuleMap parameterItemRuleMap;

    private Boolean defaultResponse;

    /**
     * Gets the content type.
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * @param contentType the new content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the redirect path or URL.
     * @return the redirect path
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the redirect path, evaluating any tokens in the path.
     * @param activity the current activity
     * @return the evaluated redirect path
     */
    public String getPath(Activity activity) {
        if (pathTokens != null) {
            return activity.getTokenEvaluator().evaluateAsString(pathTokens);
        } else {
            return path;
        }
    }

    /**
     * Sets the redirect path or URL.
     * @param path the redirect path
     */
    public void setPath(String path) {
        this.path = path;
        this.pathTokens = TokenParser.parsePathSafely(path);
    }

    private void setPath(String path, Token[] pathTokens) {
        if (pathTokens != null && pathTokens.length == 0) {
            throw new IllegalArgumentException("pathTokens must not be empty");
        }
        this.path = path;
        this.pathTokens = pathTokens;
    }

    /**
     * Gets the tokens of the redirect path.
     * @return the tokens of the redirect path
     */
    public Token[] getPathTokens() {
        return pathTokens;
    }

    /**
     * Gets the character encoding for the redirect URL.
     * @return the character encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding for the redirect URL.
     * @param encoding the new character encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets whether to exclude parameters with null values.
     * @return whether to exclude parameters with null values
     */
    public Boolean getExcludeNullParameters() {
        return excludeNullParameters;
    }

    /**
     * Returns whether to exclude parameters with null values.
     * @return whether to exclude parameters with null values
     */
    public boolean isExcludeNullParameters() {
        return BooleanUtils.toBoolean(excludeNullParameters);
    }

    /**
     * Sets whether to exclude parameters with null values.
     * @param excludeNullParameters whether to exclude parameters with null values
     */
    public void setExcludeNullParameters(Boolean excludeNullParameters) {
        this.excludeNullParameters = excludeNullParameters;
    }

    /**
     * Gets whether to exclude parameters with empty values.
     * @return whether to exclude parameters with empty values
     */
    public Boolean getExcludeEmptyParameters() {
        return excludeNullParameters;
    }

    /**
     * Returns whether to exclude parameters with empty values.
     * @return whether to exclude parameters with empty values
     */
    public boolean isExcludeEmptyParameters() {
        return BooleanUtils.toBoolean(excludeEmptyParameters);
    }

    /**
     * Sets whether to exclude parameters with empty values.
     * @param excludeEmptyParameters whether to exclude parameters with empty values
     */
    public void setExcludeEmptyParameters(Boolean excludeEmptyParameters) {
        this.excludeEmptyParameters = excludeEmptyParameters;
    }

    /**
     * Gets the map of parameters to include in the redirect URL.
     * @return the parameter item rule map
     */
    @Override
    public ItemRuleMap getParameterItemRuleMap() {
        return parameterItemRuleMap;
    }

    /**
     * Sets the map of parameters to include in the redirect URL.
     * @param parameterItemRuleMap the new parameter item rule map
     */
    @Override
    public void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap) {
        this.parameterItemRuleMap = parameterItemRuleMap;
    }

    /**
     * Adds a parameter item rule.
     * @param parameterItemRule the parameter item rule
     */
    @Override
    public void addParameterItemRule(ItemRule parameterItemRule) {
        if (parameterItemRuleMap == null) {
            parameterItemRuleMap = new ItemRuleMap();
        }
        parameterItemRuleMap.putItemRule(parameterItemRule);
    }

    /**
     * Sets the parameter map.
     * @param parameters the parameter map
     */
    public void setParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            this.parameterItemRuleMap = null;
        } else {
            ItemRuleMap itemRuleMap = new ItemRuleMap();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                ItemRule ir = new ItemRule();
                ir.setTokenize(false);
                ir.setName(entry.getKey());
                ir.setValue(entry.getValue());
                itemRuleMap.putItemRule(ir);
            }
            this.parameterItemRuleMap = itemRuleMap;
        }
    }

    /**
     * Returns whether this is the default response.
     * @return whether this is the default response
     */
    public Boolean getDefaultResponse() {
        return defaultResponse;
    }

    /**
     * Returns whether this is the default response.
     * @return true, if this is the default response
     */
    public boolean isDefaultResponse() {
        return BooleanUtils.toBoolean(defaultResponse);
    }

    /**
     * Sets whether this is the default response.
     * @param defaultResponse whether this is the default response
     */
    public void setDefaultResponse(Boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    @Override
    public RedirectRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("type", RESPONSE_TYPE);
        tsb.append("contentType", contentType);
        tsb.append("path", path);
        tsb.append("parameters", parameterItemRuleMap);
        tsb.append("encoding", encoding);
        tsb.append("excludeNullParameters", excludeNullParameters);
        tsb.append("excludeEmptyParameters", excludeEmptyParameters);
        tsb.append("default", getDefaultResponse());
        return tsb.toString();
    }

    /**
     * Creates a new instance of RedirectRule.
     * @param contentType the content type
     * @param path the redirect path
     * @param encoding the character encoding
     * @param excludeNullParameters whether to exclude null parameters
     * @param excludeEmptyParameters whether to exclude empty parameters
     * @param defaultResponse whether this is the default response
     * @return a new RedirectRule instance
     */
    @NonNull
    public static RedirectRule newInstance(String contentType, String path, String encoding,
                                           Boolean excludeNullParameters, Boolean excludeEmptyParameters,
                                           Boolean defaultResponse) {
        RedirectRule rr = new RedirectRule();
        rr.setContentType(contentType);
        if (StringUtils.hasLength(path)) {
            rr.setPath(path);
        }
        rr.setEncoding(encoding);
        rr.setExcludeNullParameters(excludeNullParameters);
        rr.setExcludeEmptyParameters(excludeEmptyParameters);
        rr.setDefaultResponse(defaultResponse);
        return rr;
    }

    /**
     * Creates a new instance of RedirectRule.
     * @param path the redirect path
     * @return a new RedirectRule instance
     * @throws IllegalRuleException if the path is null
     */
    @NonNull
    public static RedirectRule newInstance(String path) throws IllegalRuleException {
        if (path == null) {
            throw new IllegalRuleException("path must not be null");
        }
        RedirectRule rr = new RedirectRule();
        rr.setPath(path);
        return rr;
    }

    /**
     * Creates a replica of the given RedirectRule.
     * @param redirectRule the redirect rule to replicate
     * @return a new, replicated instance of RedirectRule
     */
    @NonNull
    public static RedirectRule replicate(@NonNull RedirectRule redirectRule) {
        RedirectRule rr = new RedirectRule();
        rr.setContentType(redirectRule.getContentType());
        rr.setPath(redirectRule.getPath(), redirectRule.getPathTokens());
        rr.setEncoding(redirectRule.getEncoding());
        rr.setExcludeNullParameters(redirectRule.getExcludeNullParameters());
        rr.setExcludeEmptyParameters(redirectRule.getExcludeEmptyParameters());
        rr.setParameterItemRuleMap(redirectRule.getParameterItemRuleMap());
        rr.setDefaultResponse(redirectRule.getDefaultResponse());
        return rr;
    }

}
