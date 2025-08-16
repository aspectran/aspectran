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
 * Base implementation of the {@link Translet} interface that provides common
 * access to the underlying {@link TransletRule} and exposes basic request
 * metadata such as request name and method.
 * <p>
 * Subclasses such as {@code CoreTranslet} can focus on higher-level behavior
 * while delegating rule access and simple state management to this class.
 * </p>
 *
 * <p>Created: 2008. 07. 05.</p>
 */
public abstract class AbstractTranslet implements Translet {

    private final TransletRule transletRule;

    private String requestName;

    private MethodType requestMethod;

    /**
     * Create a new AbstractTranslet backed by the given {@link TransletRule}.
     * @param transletRule the rule describing this translet (must not be {@code null})
     */
    protected AbstractTranslet(@NonNull TransletRule transletRule) {
        this.transletRule = transletRule;
    }

    /**
     * Returns the request name associated with this translet.
     * @return the request name, or {@code null} if not initialized yet
     */
    @Override
    public String getRequestName() {
        return requestName;
    }

    /**
     * Set the request name for this translet.
     * @param requestName the request name
     */
    protected void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    /**
     * Returns the request method (e.g., GET/POST) for this translet.
     * @return the request method, or {@code null} if not initialized yet
     */
    @Override
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Set the request method for this translet.
     * @param requestMethod the request method
     */
    protected void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Returns the translet name as declared in the {@link TransletRule}.
     * @return the translet name
     */
    @Override
    public String getTransletName() {
        return transletRule.getName();
    }

    /**
     * Return the underlying {@link TransletRule}.
     */
    protected TransletRule getTransletRule() {
        return transletRule;
    }

    /**
     * Return the {@link RequestRule} associated with this translet.
     */
    protected RequestRule getRequestRule() {
        return transletRule.getRequestRule();
    }

    /**
     * Return the {@link ResponseRule} associated with this translet.
     */
    protected ResponseRule getResponseRule() {
        return transletRule.getResponseRule();
    }

    /**
     * Return the optional {@link DescriptionRule} for this translet, or {@code null}.
     */
    public DescriptionRule getDescriptionRule() {
        return transletRule.getDescriptionRule();
    }

}
