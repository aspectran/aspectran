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
package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;

/**
 * The Class DefaultActivity
 */
public class DefaultActivity extends BasicActivity {

    /**
     * Instantiates a new DefaultActivity.
     *
     * @param context the activity context
     */
    public DefaultActivity(ActivityContext context) {
        super(context);
    }

    @Override
    public <T extends Activity> T newActivity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(String transletName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(TransletRule transletRule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(String transletName, TransletRule transletRule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(String transletName, String requestMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(String transletName, MethodType requestMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void perform() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performWithoutResponse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodType getRequestMethod() {
        return null;
    }

    @Override
    public String getTransletName() {
        return null;
    }

    @Override
    public Translet getTranslet() {
        return null;
    }

    @Override
    public ProcessResult getProcessResult() {
        return null;
    }

    @Override
    public Object getProcessResult(String actionId) {
        return null;
    }

    @Override
    public Response getDeclaredResponse() {
        return null;
    }

    @Override
    public boolean isResponseReserved() {
        return false;
    }

    @Override
    public void registerAspectRule(AspectRule aspectRule) {
        throw new UnsupportedOperationException();
    }

}
