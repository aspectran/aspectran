/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class ScheduledJobRule.
 */
public class ScheduledJobRule {

    private final ScheduleRule scheduleRule;

    private String transletName;

    private Boolean disabled;

    public ScheduledJobRule(ScheduleRule scheduleRule) {
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
        tsb.append("disabled", disabled);
        return tsb.toString();
    }

    public static ScheduledJobRule newInstance(ScheduleRule scheduleRule, String transletName,
                                               Boolean disabled) throws IllegalRuleException {
        if (transletName == null) {
            throw new IllegalRuleException("The 'job' element requires a 'translet' attribute");
        }

        ScheduledJobRule scheduledJobRule = new ScheduledJobRule(scheduleRule);
        scheduledJobRule.setTransletName(transletName);
        scheduledJobRule.setDisabled(disabled);
        return scheduledJobRule;
    }

}