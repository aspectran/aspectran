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
package com.aspectran.core.scheduler.service;

import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.SchedulerConfig;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.context.rule.params.TriggerExpressionParameters;
import com.aspectran.core.context.rule.type.TriggerType;
import com.aspectran.core.scheduler.activity.ActivityJobListener;
import com.aspectran.core.scheduler.activity.ActivityLauncherJob;
import com.aspectran.core.service.AbstractServiceLifeCycle;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for {@link SchedulerService} implementations.
 * <p>This class provides the core functionality for integrating with the Quartz
 * scheduler. It manages the lifecycle of Quartz {@link Scheduler} instances,
 * builds jobs and triggers from Aspectran's {@link ScheduleRule} configuration,
 * and handles the startup and shutdown of the schedulers.
 *
 * <p>It is designed to be a sub-service of a {@link CoreService}, inheriting its
 * lifecycle and accessing the main {@link ActivityContext}.
 */
public abstract class AbstractSchedulerService extends AbstractServiceLifeCycle implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSchedulerService.class);

    private final Map<String, Scheduler> schedulerMap = new HashMap<>();

    private int startDelaySeconds = 0;

    private boolean waitOnShutdown = false;

    private String loggingGroup;

    AbstractSchedulerService(CoreService parentService) {
        super(parentService);
        Assert.notNull(parentService, "parentService must not be null");
    }

    @Override
    public ActivityContext getActivityContext() {
        Assert.state(getParentService().getActivityContext() != null,
                "No ActivityContext configured yet");
        return getParentService().getActivityContext();
    }

    @Override
    public int getStartDelaySeconds() {
        return startDelaySeconds;
    }

    public void setStartDelaySeconds(int startDelaySeconds) {
        this.startDelaySeconds = startDelaySeconds;
    }

    @Override
    public boolean isWaitOnShutdown() {
        return waitOnShutdown;
    }

    public void setWaitOnShutdown(boolean waitOnShutdown) {
        this.waitOnShutdown = waitOnShutdown;
    }

    @Override
    public String getLoggingGroup() {
        return loggingGroup;
    }

    public void setLoggingGroup(String loggingGroup) {
        this.loggingGroup = loggingGroup;
    }

    @Override
    public boolean isDerived() {
        return true;
    }

    protected Collection<Scheduler> getSchedulers() {
        return schedulerMap.values();
    }

    protected Scheduler getScheduler(String scheduleId) {
        return schedulerMap.get(scheduleId);
    }

    private void addScheduler(String scheduleId, Scheduler scheduler) {
        Assert.notNull(scheduleId, "scheduleId must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");
        schedulerMap.put(scheduleId, scheduler);
    }

    private void clearSchedulers() {
        schedulerMap.clear();
    }

    @Override
    protected void doStart() throws Exception {
        try {
            buildSchedulers();
            for (Scheduler scheduler : getSchedulers()) {
                logger.info("Starting scheduler '{}'", scheduler.getSchedulerName());

                // Listener attached to jobKey
                JobListener defaultJobListener = new ActivityJobListener(getLoggingGroup());
                scheduler.getListenerManager().addJobListener(defaultJobListener);

                if (getStartDelaySeconds() > 0) {
                    scheduler.startDelayed(getStartDelaySeconds());
                } else {
                    scheduler.start();
                }
            }
        } catch (Exception e) {
            throw new SchedulerServiceException("Could not start " + getServiceName(), e);
        }
    }

    @Override
    protected void doStop() throws Exception {
        try {
            for (Scheduler scheduler : getSchedulers()) {
                if (!scheduler.isShutdown()) {
                    logger.info("Shutting down the scheduler '{}' with waitForJobsToComplete={}",
                            scheduler.getSchedulerName(), isWaitOnShutdown());
                    scheduler.shutdown(isWaitOnShutdown());
                }
            }
            clearSchedulers();
        } catch (Exception e) {
            throw new SchedulerServiceException("Could not shutdown " + getServiceName(), e);
        }
    }

    private void buildSchedulers() throws SchedulerServiceException {
        ScheduleRuleRegistry scheduleRuleRegistry = getActivityContext().getScheduleRuleRegistry();
        if (scheduleRuleRegistry == null) {
            return;
        }

        Collection<ScheduleRule> scheduleRules = scheduleRuleRegistry.getScheduleRules();
        if (scheduleRules == null || scheduleRules.isEmpty()) {
            return;
        }

        try {
            for (ScheduleRule scheduleRule : scheduleRules) {
                Scheduler scheduler = createScheduler(scheduleRule);
                addScheduler(scheduleRule.getId(), scheduler);
            }
        } catch (Exception e) {
            throw new SchedulerServiceException("Could not start " + getServiceName(), e);
        }
    }

    @NonNull
    private Scheduler createScheduler(@NonNull ScheduleRule scheduleRule) throws SchedulerException {
        Scheduler scheduler = null;
        if (scheduleRule.getSchedulerBeanClass() != null) {
            scheduler = (Scheduler)getActivityContext().getBeanRegistry().getBean(scheduleRule.getSchedulerBeanClass());
        } else if (scheduleRule.getSchedulerBeanId() != null) {
            scheduler = getActivityContext().getBeanRegistry().getBean(scheduleRule.getSchedulerBeanId());
        }
        if (scheduler == null) {
            throw new SchedulerServiceException("No such scheduler bean; Invalid ScheduleRule " + scheduleRule);
        }

        List<ScheduledJobRule> jobRuleList = scheduleRule.getScheduledJobRuleList();
        for (ScheduledJobRule jobRule : jobRuleList) {
            JobDetail jobDetail = createJobDetail(jobRule);
            if (jobDetail != null) {
                String triggerName = jobDetail.getKey().getName();
                String triggerGroup = scheduleRule.getId();
                Trigger trigger = createTrigger(triggerName, triggerGroup, scheduleRule, getStartDelaySeconds());
                scheduler.scheduleJob(jobDetail, trigger);
            }
        }

        return scheduler;
    }

    private JobDetail createJobDetail(@NonNull ScheduledJobRule jobRule) {
        String jobName = jobRule.getTransletName();
        String jobGroup = jobRule.getScheduleRule().getId();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SERVICE_DATA_KEY, this);
        jobDataMap.put(JOB_RULE_DATA_KEY, jobRule);

        return JobBuilder.newJob(ActivityLauncherJob.class)
                .withIdentity(jobName, jobGroup)
                .setJobData(jobDataMap)
                .build();
    }

    private Trigger createTrigger(
            String name, String group, @NonNull ScheduleRule scheduleRule, final int startDelaySeconds) {
        TriggerExpressionParameters expressionParameters = scheduleRule.getTriggerExpressionParameters();
        int startDelaySecondsToUse = startDelaySeconds;
        if (expressionParameters.getStartDelaySeconds() != null) {
            startDelaySecondsToUse += expressionParameters.getStartDelaySeconds();
        }

        Date firstFireTime;
        if (startDelaySecondsToUse > 0) {
            firstFireTime = new Date(System.currentTimeMillis() + (startDelaySecondsToUse * 1000L));
        } else {
            firstFireTime = new Date();
        }

        if (scheduleRule.getTriggerType() == TriggerType.SIMPLE) {
            Long intervalInMilliseconds = expressionParameters.getIntervalInMilliseconds();
            Integer intervalInSeconds = expressionParameters.getIntervalInSeconds();
            Integer intervalInMinutes = expressionParameters.getIntervalInMinutes();
            Integer intervalInHours = expressionParameters.getIntervalInHours();
            Integer repeatCount = expressionParameters.getRepeatCount();
            Boolean repeatForever = expressionParameters.getRepeatForever();

            SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule();
            if (intervalInMilliseconds != null) {
                builder.withIntervalInMilliseconds(intervalInMilliseconds);
            }
            if (intervalInSeconds != null) {
                builder.withIntervalInSeconds(intervalInSeconds);
            }
            if (intervalInMinutes != null) {
                builder.withIntervalInMinutes(intervalInMinutes);
            }
            if (intervalInHours != null) {
                builder.withIntervalInHours(intervalInHours);
            }
            if (repeatCount != null) {
                builder.withRepeatCount(repeatCount);
            }
            if (Boolean.TRUE.equals(repeatForever)) {
                builder.repeatForever();
            }

            return TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .startAt(firstFireTime)
                    .withSchedule(builder)
                    .build();
        } else {
            String expression = expressionParameters.getExpression();
            CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(expression);

            return TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .startAt(firstFireTime)
                    .withSchedule(cronSchedule)
                    .build();
        }
    }

    protected void configure(@NonNull SchedulerConfig schedulerConfig) {
        if (schedulerConfig.hasWaitOnShutdown()) {
            int startDelaySeconds = schedulerConfig.getStartDelaySeconds();
            if (startDelaySeconds < 0) {
                startDelaySeconds = 3;
                if (logger.isDebugEnabled()) {
                    logger.debug("Scheduler option 'startDelaySeconds' is not specified, defaulting to 3 seconds");
                }
            }
            setStartDelaySeconds(startDelaySeconds);
        }

        if (schedulerConfig.hasWaitOnShutdown()) {
            setWaitOnShutdown(schedulerConfig.isWaitOnShutdown());
        }

        if (StringUtils.hasText(getParentService().getContextName())) {
            setLoggingGroup(getParentService().getContextName());
        }
    }

}
