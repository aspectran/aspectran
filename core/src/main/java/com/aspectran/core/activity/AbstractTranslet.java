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
package com.aspectran.core.activity;

import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;

public abstract class AbstractTranslet implements Translet {

    private final TransletRule transletRule;

    private String requestName;

    private MethodType requestMethod;

    protected AbstractTranslet(TransletRule transletRule) {
        this.transletRule = transletRule;
    }

    @Override
    public String getTransletName() {
        return transletRule.getName();
    }

    @Override
    public String getRequestName() {
        return requestName;
    }

    protected void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    @Override
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    protected void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    protected TransletRule getTransletRule() {
        return transletRule;
    }

    protected RequestRule getRequestRule() {
        return transletRule.getRequestRule();
    }

    protected ResponseRule getResponseRule() {
        return transletRule.getResponseRule();
    }

}
