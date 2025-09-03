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

import com.aspectran.core.component.bean.annotation.CronTrigger;
import com.aspectran.core.component.bean.annotation.SimpleTrigger;
import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.core.context.rule.params.TriggerExpressionParameters;
import com.aspectran.core.context.rule.params.TriggerParameters;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.context.rule.type.TriggerType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a group of scheduled jobs that share a single trigger configuration.
 * This rule defines which scheduler bean to use, a single trigger (simple or cron)
 * that determines when the jobs will run, and a list of one or more jobs
 * ({@link ScheduledJobRule}) to be executed when the trigger fires.
 *
 * <p>Created: 2016. 01. 24.</p>
 */
public class ScheduleRule implements BeanReferenceable, Describable {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.SCHEDULE_RULE;

    private String id;

    private TriggerType triggerType;

    private TriggerExpressionParameters triggerExpressionParameters;

    private String schedulerBeanId;

    private Class<?> schedulerBeanClass;

    private List<ScheduledJobRule> scheduledJobRuleList = new ArrayList<>();

    private DescriptionRule descriptionRule;

    /**
     * Gets the ID of this schedule rule.
     * @return the schedule ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of this schedule rule.
     * @param id the schedule ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the type of the trigger (e.g., SIMPLE or CRON).
     * @return the trigger type
     */
    public TriggerType getTriggerType() {
        return triggerType;
    }

    /**
     * Sets the type of the trigger.
     * @param triggerType the trigger type
     */
    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    /**
     * Gets the parameters that define the trigger's behavior (e.g., cron expression or simple interval).
     * @return the trigger expression parameters
     */
    public TriggerExpressionParameters getTriggerExpressionParameters() {
        return triggerExpressionParameters;
    }

    /**
     * Sets the parameters for the trigger.
     * @param triggerExpressionParameters the trigger expression parameters
     */
    public void setTriggerExpressionParameters(TriggerExpressionParameters triggerExpressionParameters) {
        this.triggerExpressionParameters = triggerExpressionParameters;
    }

    /**
     * Gets the ID of the scheduler bean that will manage this schedule.
     * @return the scheduler bean ID
     */
    public String getSchedulerBeanId() {
        return schedulerBeanId;
    }

    /**
     * Sets the ID of the scheduler bean.
     * @param schedulerBeanId the scheduler bean ID
     */
    public void setSchedulerBeanId(String schedulerBeanId) {
        this.schedulerBeanId = schedulerBeanId;
    }

    /**
     * Gets the class of the scheduler bean.
     * @return the scheduler bean class
     */
    public Class<?> getSchedulerBeanClass() {
        return schedulerBeanClass;
    }

    /**
     * Sets the class of the scheduler bean.
     * @param schedulerBeanClass the scheduler bean class
     */
    public void setSchedulerBeanClass(Class<?> schedulerBeanClass) {
        this.schedulerBeanClass = schedulerBeanClass;
    }

    /**
     * Gets the list of jobs to be executed by this schedule.
     * @return the list of scheduled job rules
     */
    public List<ScheduledJobRule> getScheduledJobRuleList() {
        return scheduledJobRuleList;
    }

    /**
     * Sets the list of jobs for this schedule.
     * @param scheduledJobRuleList the list of scheduled job rules
     */
    public void setScheduledJobRuleList(List<ScheduledJobRule> scheduledJobRuleList) {
        this.scheduledJobRuleList = scheduledJobRuleList;
    }

    /**
     * Adds a job to this schedule.
     * @param scheduledJobRule the job rule to add
     */
    public void addScheduledJobRule(ScheduledJobRule scheduledJobRule) {
        scheduledJobRuleList.add(scheduledJobRule);
    }

    @Override
    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    @Override
    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", id);
        tsb.append("scheduler", schedulerBeanId);
        tsb.append("trigger", triggerExpressionParameters);
        tsb.append("jobs", scheduledJobRuleList);
        return tsb.toString();
    }

    /**
     * Creates a new instance of ScheduleRule.
     * @param id the ID of the schedule, which is mandatory
     * @return a new {@code ScheduleRule} instance
     * @throws IllegalRuleException if the ID is null
     */
    @NonNull
    public static ScheduleRule newInstance(String id) throws IllegalRuleException {
        if (id == null) {
            throw new IllegalRuleException("The 'schedule' element requires an 'id' attribute");
        }

        ScheduleRule scheduleRule = new ScheduleRule();
        scheduleRule.setId(id);
        return scheduleRule;
    }

    /**
     * A static helper method to update the trigger from a parameter map.
     * @param scheduleRule the rule to update
     * @param triggerParameters the parameters containing trigger info
     * @throws IllegalRuleException if the parameters are invalid
     */
    public static void updateTrigger(ScheduleRule scheduleRule, @NonNull TriggerParameters triggerParameters)
            throws IllegalRuleException {
        updateTriggerType(scheduleRule, triggerParameters.getString(TriggerParameters.type));
        TriggerExpressionParameters expressionParameters = triggerParameters.getParameters(TriggerParameters.expression);
        if (expressionParameters == null) {
            throw new IllegalRuleException("Be sure to specify trigger expression parameters " +
                    Arrays.toString(TriggerExpressionParameters.getParameterKeys()));
        }
        updateTriggerExpression(scheduleRule, expressionParameters);
    }

    /**
     * A static helper method to update the trigger from type and expression strings.
     * @param scheduleRule the rule to update
     * @param type the trigger type string ("simple" or "cron")
     * @param expression the trigger expression string
     * @throws IllegalRuleException if the expression is invalid
     */
    public static void updateTrigger(@NonNull ScheduleRule scheduleRule, String type, String expression)
            throws IllegalRuleException {
        updateTriggerType(scheduleRule, type);
        updateTriggerExpression(scheduleRule, expression);
    }

    /**
     * A static helper method to update the trigger type.
     * @param scheduleRule the rule to update
     * @param type the trigger type string
     */
    public static void updateTriggerType(@NonNull ScheduleRule scheduleRule, String type) {
        TriggerType triggerType;
        if (type != null) {
            triggerType = TriggerType.resolve(type);
            if (triggerType == null) {
                throw new IllegalArgumentException("Unknown trigger type '" + type +
                        "'; Trigger type for Scheduler must be 'cron' or 'simple'");
            }
        } else {
            triggerType = TriggerType.CRON;
        }
        scheduleRule.setTriggerType(triggerType);
    }

    /**
     * A static helper method to update the trigger expression from a string.
     * @param scheduleRule the rule to update
     * @param expression the trigger expression string
     * @throws IllegalRuleException if the expression cannot be parsed
     */
    public static void updateTriggerExpression(@NonNull ScheduleRule scheduleRule, String expression)
            throws IllegalRuleException {
        if (StringUtils.hasText(expression)) {
            TriggerExpressionParameters expressionParameters;
            try {
                expressionParameters = new TriggerExpressionParameters(expression);
            } catch (IOException e) {
                throw new IllegalRuleException("Trigger expression parameters can not be parsed", e);
            }
            updateTriggerExpression(scheduleRule, expressionParameters);
        }
    }

    /**
     * A static helper method to update the trigger expression from a parameter object.
     * @param scheduleRule the rule to update
     * @param expressionParameters the trigger expression parameters
     */
    public static void updateTriggerExpression(@NonNull ScheduleRule scheduleRule, TriggerExpressionParameters expressionParameters) {
        if (scheduleRule.getTriggerType() == TriggerType.SIMPLE) {
            Long intervalInMilliseconds = expressionParameters.getLong(TriggerExpressionParameters.intervalInMilliseconds);
            Integer intervalInSeconds = expressionParameters.getInt(TriggerExpressionParameters.intervalInSeconds);
            Integer intervalInMinutes = expressionParameters.getInt(TriggerExpressionParameters.intervalInMinutes);
            Integer intervalInHours = expressionParameters.getInt(TriggerExpressionParameters.intervalInHours);
            if (intervalInMilliseconds == null && intervalInSeconds == null && intervalInMinutes == null && intervalInHours == null) {
                throw new IllegalArgumentException("Must specify the interval between execution times for simple trigger. (" +
                        "Specifiable time interval types: intervalInMilliseconds, intervalInSeconds, intervalInMinutes, intervalInHours)");
            }
        } else {
            String expression = expressionParameters.getString(TriggerExpressionParameters.expression);
            String[] fields = StringUtils.tokenize(expression, " ");
            if (fields.length != 6) {
                throw new IllegalArgumentException(
                        String.format("Cron expression must consist of 6 fields (found %d in %s)",
                            fields.length, expression));
            }
            expressionParameters.putValue(TriggerParameters.expression, StringUtils.join(fields, " "));
        }
        scheduleRule.setTriggerExpressionParameters(expressionParameters);
    }

    /**
     * A static helper method to update the trigger from a {@link SimpleTrigger} annotation.
     * @param scheduleRule the rule to update
     * @param simpleTriggerAnno the source annotation
     */
    public static void updateTriggerExpression(@NonNull ScheduleRule scheduleRule, @NonNull SimpleTrigger simpleTriggerAnno) {
        TriggerExpressionParameters expressionParameters = new TriggerExpressionParameters();
        scheduleRule.setTriggerType(TriggerType.SIMPLE);
        if (simpleTriggerAnno.startDelaySeconds() > 0) {
            expressionParameters.setStartDelaySeconds(simpleTriggerAnno.startDelaySeconds());
        }
        if (simpleTriggerAnno.intervalInMilliseconds() > 0L) {
            expressionParameters.setIntervalInMilliseconds(simpleTriggerAnno.intervalInMilliseconds());
        }
        if (simpleTriggerAnno.intervalInSeconds() > 0) {
            expressionParameters.setIntervalInSeconds(simpleTriggerAnno.intervalInSeconds());
        }
        if (simpleTriggerAnno.intervalInMinutes() > 0) {
            expressionParameters.setIntervalInMinutes(simpleTriggerAnno.intervalInMinutes());
        }
        if (simpleTriggerAnno.intervalInHours() > 0) {
            expressionParameters.setIntervalInHours(simpleTriggerAnno.intervalInHours());
        }
        if (simpleTriggerAnno.repeatCount() > 0) {
            expressionParameters.setRepeatCount(simpleTriggerAnno.repeatCount());
        }
        if (simpleTriggerAnno.repeatForever()) {
            expressionParameters.setRepeatForever(true);
        }
        updateTriggerExpression(scheduleRule, expressionParameters);
    }

    /**
     * A static helper method to update the trigger from a {@link CronTrigger} annotation.
     * @param scheduleRule the rule to update
     * @param cronTriggerAnno the source annotation
     */
    public static void updateTriggerExpression(@NonNull ScheduleRule scheduleRule, @NonNull CronTrigger cronTriggerAnno) {
        TriggerExpressionParameters expressionParameters = new TriggerExpressionParameters();
        scheduleRule.setTriggerType(TriggerType.CRON);
        String expression = StringUtils.emptyToNull(cronTriggerAnno.expression());
        expressionParameters.setExpression(expression);
        updateTriggerExpression(scheduleRule, expressionParameters);
    }

}
