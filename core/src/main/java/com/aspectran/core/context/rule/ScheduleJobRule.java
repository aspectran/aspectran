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
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class ScheduleJobRule.
 */
public class ScheduleJobRule {

    private final ScheduleRule scheduleRule;

    private String transletName;

    private MethodType requestMethod;

    private Boolean disabled;

    public ScheduleJobRule(ScheduleRule scheduleRule) {
        this.scheduleRule = scheduleRule;
    }

    public ScheduleRule getScheduleRule() {
        return scheduleRule;
    }

    public String getTransletName() {
        return transletName;
    }

    public void setTransletName(String transletName) {
        this.transletName = transletName;
    }

    public MethodType getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public boolean isDisabled() {
        return BooleanUtils.toBoolean(disabled);
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("translet", transletName);
        tsb.append("method", requestMethod);
        tsb.append("disabled", disabled);
        return tsb.toString();
    }

    public static ScheduleJobRule newInstance(ScheduleRule scheduleRule, String transletName,
                                              String method, Boolean disabled) throws IllegalRuleException {
        if (transletName == null) {
            throw new IllegalRuleException("The 'job' element requires a 'translet' attribute");
        }

        ScheduleJobRule scheduleJobRule = new ScheduleJobRule(scheduleRule);
        scheduleJobRule.setTransletName(transletName);
        scheduleJobRule.setDisabled(disabled);

        if (method != null) {
            MethodType methodType = MethodType.resolve(method);
            if (methodType == null) {
                throw new IllegalRuleException("No request method type for '" + method + "'");
            }
            scheduleJobRule.setRequestMethod(methodType);
        }

        return scheduleJobRule;
    }

}
