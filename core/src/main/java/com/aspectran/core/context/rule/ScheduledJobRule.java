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

import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;

/**
 * Represents a single job to be executed as part of a schedule.
 * This rule specifies *what* to run (a translet) and whether the job is currently active.
 * The execution time is determined by the trigger in the parent {@link ScheduleRule}.
 *
 * <p>Created: 2016. 01. 24.</p>
 */
public class ScheduledJobRule {

    private final ScheduleRule scheduleRule;

    private String transletName;

    private Boolean disabled;

    /**
     * Instantiates a new ScheduledJobRule.
     * @param scheduleRule the parent {@link ScheduleRule} that this job belongs to (must not be null)
     */
    public ScheduledJobRule(@NonNull ScheduleRule scheduleRule) {
        this.scheduleRule = scheduleRule;
    }

    /**
     * Returns the parent {@link ScheduleRule}.
     * @return the parent schedule rule
     */
    public ScheduleRule getScheduleRule() {
        return scheduleRule;
    }

    /**
     * Gets the name of the translet to be executed by this job.
     * @return the name of the translet
     */
    public String getTransletName() {
        return transletName;
    }

    /**
     * Sets the name of the translet to be executed.
     * @param transletName the name of the translet
     */
    public void setTransletName(String transletName) {
        this.transletName = transletName;
    }

    /**
     * Gets the raw boolean value indicating if this job is disabled.
     * @return true if disabled, false if enabled, or null if not specified
     */
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * Returns whether this job is disabled.
     * @return true if this job should not be executed; false otherwise
     */
    public boolean isDisabled() {
        return BooleanUtils.toBoolean(disabled);
    }

    /**
     * Sets whether this job is disabled.
     * @param disabled true to disable the job, false to enable it
     */
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

    /**
     * Creates a new instance of ScheduledJobRule.
     * @param scheduleRule the parent schedule rule
     * @param transletName the name of the translet to execute (required)
     * @param disabled whether the job is disabled
     * @return a new {@code ScheduledJobRule} instance
     * @throws IllegalRuleException if the translet name is null
     */
    @NonNull
    public static ScheduledJobRule newInstance(
            @NonNull ScheduleRule scheduleRule, String transletName,
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
