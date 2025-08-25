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
package com.aspectran.core.activity;

import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Abstract base implementation of the {@link Translet} interface.
 * <p>This class provides the fundamental infrastructure for a translet by holding the
 * defining {@link TransletRule} and managing basic request metadata like the request
 * name and method. Subclasses can build upon this base to implement the detailed
 * logic of request processing.</p>
 *
 * @since 2008. 07. 05.
 */
public abstract class AbstractTranslet implements Translet {

    /** The rule that defines the behavior of this translet. */
    private final TransletRule transletRule;

    /** The name of the current request being processed. */
    private String requestName;

    /** The request method of the current request. */
    private MethodType requestMethod;

    /**
     * Creates a new AbstractTranslet backed by the given {@link TransletRule}.
     * @param transletRule the rule that defines this translet (must not be {@code null})
     */
    protected AbstractTranslet(@NonNull TransletRule transletRule) {
        this.transletRule = transletRule;
    }

    @Override
    public String getRequestName() {
        return requestName;
    }

    /**
     * Sets the name of the current request.
     * @param requestName the request name
     */
    protected void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    @Override
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Sets the request method of the current request.
     * @param requestMethod the request method
     */
    protected void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    @Override
    public String getTransletName() {
        return transletRule.getName();
    }

    /**
     * Returns the underlying {@link TransletRule} that defines this translet's behavior.
     * @return the defining translet rule
     */
    protected TransletRule getTransletRule() {
        return transletRule;
    }

    /**
     * Returns the {@link RequestRule} associated with this translet, if any.
     * @return the request rule, or {@code null} if not defined
     */
    protected RequestRule getRequestRule() {
        return transletRule.getRequestRule();
    }

    /**
     * Returns the {@link ResponseRule} associated with this translet, if any.
     * @return the response rule, or {@code null} if not defined
     */
    protected ResponseRule getResponseRule() {
        return transletRule.getResponseRule();
    }

    /**
     * Returns the optional {@link DescriptionRule} for this translet.
     * @return the description rule, or {@code null} if not defined
     */
    public DescriptionRule getDescriptionRule() {
        return transletRule.getDescriptionRule();
    }

}
