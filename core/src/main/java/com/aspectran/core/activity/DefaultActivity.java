/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;

import java.util.List;

/**
 * Default activity to handle all internal requests.
 *
 * <p>Note that this is an activity that has nothing to do with
 * advice. This does not execute any advice at all, and if you
 * attempt to register the advice dynamically, you will get an
 * exception of the advice constraint violation.</p>
 */
public class DefaultActivity extends AbstractActivity {

    /**
     * Instantiates a new DefaultActivity.
     *
     * @param context the activity context
     */
    public DefaultActivity(ActivityContext context) {
        super(context);
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
    public Object perform(InstantAction instantAction) {
        throw new UnsupportedOperationException();
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
    public void executeAdvice(List<AspectAdviceRule> aspectAdviceRuleList, boolean throwable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void executeAdvice(AspectAdviceRule aspectAdviceRule, boolean throwable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleException(List<ExceptionRule> exceptionRuleList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerAspectAdviceRule(AspectRule aspectRule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getSetting(String settingName) {
        return null;
    }

    @Override
    public <T> T getAspectAdviceBean(String aspectId) {
        return null;
    }

    @Override
    public <T extends Activity> T newActivity() {
        throw new UnsupportedOperationException();
    }

}
