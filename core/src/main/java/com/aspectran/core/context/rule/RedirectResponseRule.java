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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.TokenExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import com.aspectran.core.context.rule.ability.ActionPossessSupport;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.util.List;
import java.util.Map;

/**
 * The Class RedirectResponseRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class RedirectResponseRule extends ActionPossessSupport implements Replicable<RedirectResponseRule> {

    public static final ResponseType RESPONSE_TYPE = ResponseType.REDIRECT;

    private String contentType;

    private String target;

    private Token[] targetTokens;

    private String encoding;

    private Boolean excludeNullParameter;

    private Boolean excludeEmptyParameter;

    private ItemRuleMap parameterItemRuleMap;

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
     * Gets the redirect target.
     *
     * @return the redirect target
     */
    public String getTarget() {
        return target;
    }

    /**
     * Gets the redirect target.
     *
     * @param activity the activity
     * @return the redirect target
     */
    public String getTarget(Activity activity) {
        if (targetTokens != null && targetTokens.length > 0) {
            TokenEvaluator evaluator = new TokenExpressionParser(activity);
            return evaluator.evaluateAsString(targetTokens);
        } else {
            return target;
        }
    }

    /**
     * Sets the target name.
     *
     * @param target the new target name
     */
    public void setTarget(String target) {
        this.target = target;

        List<Token> tokens = Tokenizer.tokenize(target, true);
        int tokenCount = 0;

        for (Token t : tokens) {
            if (t.getType() != TokenType.TEXT) {
                tokenCount++;
            }
        }

        if (tokenCount > 0) {
            this.targetTokens = tokens.toArray(new Token[0]);
        } else {
            this.targetTokens = null;
        }
    }

    public void setTarget(String target, Token[] targetTokens) {
        this.target = target;
        this.targetTokens = targetTokens;
    }

    /**
     * Gets the tokens of the redirect target.
     *
     * @return the tokens of the redirect target
     */
    public Token[] getTargetTokens() {
        return targetTokens;
    }

    /**
     * Gets the character encoding.
     *
     * @return the character encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding.
     *
     * @param encoding the new character encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets whether to exclude parameters with null values.
     *
     * @return whether to exclude parameters with null values
     */
    public Boolean getExcludeNullParameter() {
        return excludeNullParameter;
    }

    /**
     * Returns whether to exclude parameters with null values.
     *
     * @return whether to exclude parameters with null values
     */
    public boolean isExcludeNullParameter() {
        return BooleanUtils.toBoolean(excludeNullParameter);
    }

    /**
     * Sets whether to exclude parameters with null values.
     *
     * @param excludeNullParameter whether to exclude parameters with null values
     */
    public void setExcludeNullParameter(Boolean excludeNullParameter) {
        this.excludeNullParameter = excludeNullParameter;
    }

    /**
     * Gets whether to exclude parameters with empty values.
     *
     * @return whether to exclude parameters with empty values
     */
    public Boolean getExcludeEmptyParameter() {
        return excludeNullParameter;
    }

    /**
     * Returns whether to exclude parameters with empty values.
     *
     * @return whether to exclude parameters with empty values
     */
    public boolean isExcludeEmptyParameter() {
        return BooleanUtils.toBoolean(excludeEmptyParameter);
    }

    /**
     * Sets whether to exclude parameters with empty values.
     *
     * @param excludeEmptyParameter whether to exclude parameters with empty values
     */
    public void setExcludeEmptyParameter(Boolean excludeEmptyParameter) {
        this.excludeEmptyParameter = excludeEmptyParameter;
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
     * Sets the parameter map.
     *
     * @param parameterMap the parameter map
     */
    public void setParameterMap(Map<String, String> parameterMap) {
        if (parameterMap == null) {
            this.parameterItemRuleMap = null;
            return;
        }

        ItemRuleMap itemRuleMap = new ItemRuleMap();
        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            ItemRule ir = new ItemRule();
            ir.setTokenize(false);
            ir.setName(entry.getKey());
            ir.setValue(entry.getValue());
        }
        this.parameterItemRuleMap = itemRuleMap;
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
     * @param defaultResponse whether it is the default response
     */
    public void setDefaultResponse(Boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    @Override
    public RedirectResponseRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("responseType", RESPONSE_TYPE);
        tsb.append("contentType", contentType);
        tsb.appendForce("target", target);
        tsb.appendForce("encoding", encoding);
        tsb.append("excludeNullParameter", excludeNullParameter);
        tsb.append("excludeEmptyParameter", excludeEmptyParameter);
        tsb.append("default", defaultResponse);
        return tsb.toString();
    }

    public static RedirectResponseRule newInstance(String contentType, String target, String encoding,
                                                   Boolean excludeNullParameter, Boolean excludeEmptyParameter, Boolean defaultResponse) {
        RedirectResponseRule rrr = new RedirectResponseRule();
        rrr.setContentType(contentType);
        if (target != null && target.length() > 0) {
            rrr.setTarget(target);
        }
        rrr.setEncoding(encoding);
        rrr.setExcludeNullParameter(excludeNullParameter);
        rrr.setExcludeEmptyParameter(excludeEmptyParameter);
        rrr.setDefaultResponse(defaultResponse);
        return rrr;
    }

    public static RedirectResponseRule newInstance(String target) throws IllegalRuleException {
        if (target == null) {
            throw new IllegalRuleException("Argument 'target' must not be null");
        }
        RedirectResponseRule rrr = new RedirectResponseRule();
        rrr.setTarget(target);
        return rrr;
    }

    public static RedirectResponseRule replicate(RedirectResponseRule redirectResponseRule) {
        RedirectResponseRule rrr = new RedirectResponseRule();
        rrr.setContentType(redirectResponseRule.getContentType());
        rrr.setTarget(redirectResponseRule.getTarget(), redirectResponseRule.getTargetTokens());
        rrr.setEncoding(redirectResponseRule.getEncoding());
        rrr.setExcludeNullParameter(redirectResponseRule.getExcludeNullParameter());
        rrr.setExcludeEmptyParameter(redirectResponseRule.getExcludeEmptyParameter());
        rrr.setParameterItemRuleMap(redirectResponseRule.getParameterItemRuleMap());
        rrr.setDefaultResponse(redirectResponseRule.getDefaultResponse());
        rrr.setActionList(redirectResponseRule.getActionList());
        return rrr;
    }

}
