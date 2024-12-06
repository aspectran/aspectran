/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.core.activity.response.dispatch.ViewDispatcher;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.Tokenizer;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.List;

/**
 * The Class DispatchRule.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class DispatchRule implements Replicable<DispatchRule> {

    public static final ResponseType RESPONSE_TYPE = ResponseType.DISPATCH;

    private String name;

    private Token[] nameTokens;

    private String dispatcherName;

    private String contentType;

    private String encoding;

    private Boolean defaultResponse;

    private ViewDispatcher viewDispatcher;

    /**
     * Gets the view name.
     * @return the view name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the view name.
     * @param activity the activity
     * @return the view name
     */
    public String getName(Activity activity) {
        if (nameTokens != null && nameTokens.length > 0) {
            return activity.getTokenEvaluator().evaluateAsString(nameTokens);
        } else {
            return name;
        }
    }

    /**
     * Sets the dispatch name.
     * @param name the new dispatch name
     */
    public void setName(String name) {
        if (name == null) {
            setName(null, null);
            return;
        }
        this.name = name;
        List<Token> tokens = Tokenizer.tokenize(name, true);
        int count = 0;
        for (Token t : tokens) {
            if (t.getType() != TokenType.TEXT) {
                count++;
            }
        }
        if (count > 0) {
            this.nameTokens = tokens.toArray(new Token[0]);
        } else {
            this.nameTokens = null;
        }
    }

    /**
     * Sets the dispatch name and its name tokens.
     * @param name the new dispatch name
     * @param nameTokens the name tokens
     */
    public void setName(String name, Token[] nameTokens) {
        this.name = name;
        this.nameTokens = nameTokens;
    }

    /**
     * Gets the tokens of the dispatch name.
     * @return the tokens of the dispatch name
     */
    public Token[] getNameTokens() {
        return nameTokens;
    }

    /**
     * Gets the id or class name of the view dispatcher bean that
     * implements {@link ViewDispatcher}.
     * @return the id or class name of the view dispatcher bean
     */
    public String getDispatcherName() {
        return dispatcherName;
    }

    /**
     * Gets the id or class name of the view dispatcher bean that
     * implements {@link ViewDispatcher}.
     * @param dispatcherName the id or class name of the view dispatcher bean
     */
    public void setDispatcherName(String dispatcherName) {
        this.dispatcherName = dispatcherName;
    }

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
     * Gets the character encoding.
     * @return the character encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding.
     * @param encoding the character encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns whether the default response.
     * @return whether the default response
     */
    public Boolean getDefaultResponse() {
        return defaultResponse;
    }

    /**
     * Returns whether the default response.
     * @return true, if is default response
     */
    public boolean isDefaultResponse() {
        return BooleanUtils.toBoolean(defaultResponse);
    }

    /**
     * Sets whether the default response.
     * @param defaultResponse whether the default response
     */
    public void setDefaultResponse(Boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    public ViewDispatcher getViewDispatcher() {
        return viewDispatcher;
    }

    public void setViewDispatcher(ViewDispatcher viewDispatcher) {
        this.viewDispatcher = viewDispatcher;
    }

    @Override
    public DispatchRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        return toString(viewDispatcher, null);
    }

    /**
     * Returns a string representation of {@code DispatchRule} with used {@code Dispatcher}.
     * @param viewDispatcher the view dispatcher
     * @param viewName the target view name
     * @return a string representation of {@code DispatchRule}.
     */
    public String toString(ViewDispatcher viewDispatcher, String viewName) {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("type", RESPONSE_TYPE);
        tsb.appendForce("name", viewName != null ? viewName : name);
        tsb.append("contentType", contentType);
        tsb.append("encoding", encoding);
        tsb.append("default", getDefaultResponse());
        tsb.append("viewDispatcher", viewDispatcher);
        return tsb.toString();
    }

    /**
     * Returns a new instance of DispatchRule.
     * @param name the dispatch name
     * @param dispatcherName the id or class name of the view dispatcher bean
     * @param contentType the content type
     * @param encoding the character encoding
     * @param defaultResponse whether it is the default response
     * @return an instance of DispatchRule
     * @throws IllegalRuleException if an illegal rule is found
     */
    @NonNull
    public static DispatchRule newInstance(
            String name, String dispatcherName, String contentType,
            String encoding, Boolean defaultResponse) throws IllegalRuleException {
        DispatchRule dr = newInstance(name);
        dr.setDispatcherName(dispatcherName);
        dr.setContentType(contentType);
        dr.setEncoding(encoding);
        dr.setDefaultResponse(defaultResponse);
        return dr;
    }

    /**
     * Returns a new instance of DispatchRule.
     * @param name the dispatch name
     * @param dispatcher the id or class name of the view dispatcher bean
     * @param contentType the content type
     * @param encoding the character encoding
     * @return the dispatch rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    @NonNull
    public static DispatchRule newInstance(String name, String dispatcher, String contentType, String encoding)
            throws IllegalRuleException {
        return newInstance(name, dispatcher, contentType, encoding, null);
    }

    /**
     * Returns a new instance of DispatchRule.
     * @param name the dispatch name
     * @return the dispatch rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    @NonNull
    public static DispatchRule newInstance(String name) throws IllegalRuleException {
        if (name == null) {
            throw new IllegalRuleException("name must not be null");
        }
        DispatchRule dr = new DispatchRule();
        dr.setName(name);
        return dr;
    }

    /**
     * Returns a new derived instance of DispatchRule.
     * @param dispatchRule an instance of DispatchRule
     * @return the dispatch rule
     */
    @NonNull
    public static DispatchRule replicate(@NonNull DispatchRule dispatchRule) {
        DispatchRule dr = new DispatchRule();
        dr.setName(dispatchRule.getName(), dispatchRule.getNameTokens());
        dr.setContentType(dispatchRule.getContentType());
        dr.setEncoding(dispatchRule.getEncoding());
        dr.setDefaultResponse(dispatchRule.getDefaultResponse());
        return dr;
    }

}
