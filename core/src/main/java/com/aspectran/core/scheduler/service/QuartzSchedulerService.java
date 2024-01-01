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
package com.aspectran.core.scheduler.service;

import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.context.rule.params.TriggerExpressionParameters;
import com.aspectran.core.context.rule.type.TriggerType;
import com.aspectran.core.service.AbstractServiceController;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
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
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class QuartzSchedulerService.
 */
public class QuartzSchedulerService extends AbstractServiceController implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(QuartzSchedulerService.class);

    static final String SERVICE_DATA_KEY = "SERVICE";

    static final String JOB_RULE_DATA_KEY = "JOB_RULE";

    private final Set<Scheduler> schedulerSet = new HashSet<>();

    private final Map<String, Scheduler> schedulerMap = new HashMap<>();

    private int startDelaySeconds = 0;

    private boolean waitOnShutdown = false;

    public QuartzSchedulerService(CoreService rootService) {
        super(false);
        Assert.state(rootService != null, "rootService must not be null");
        setRootService(rootService);
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
    public boolean isDerived() {
        return false;
    }

    @Override
    protected void doStart() throws Exception {
        startSchedulerService();
    }

    @Override
    protected void doPause() throws Exception {
        try {
            for (Scheduler scheduler : schedulerSet) {
                scheduler.pauseAll();
            }
        } catch (Exception e) {
            throw new SchedulerServiceException("Could not pause all schedulers", e);
        }
    }

    @Override
    protected void doPause(long timeout) throws Exception {
        logger.warn(getServiceName() + " does not support pausing for a certain period of time");
    }

    @Override
    protected void doResume() throws Exception {
        try {
            for (Scheduler scheduler : schedulerSet) {
                scheduler.resumeAll();
            }
        } catch (Exception e) {
            throw new SchedulerServiceException("Could not resume all schedulers", e);
        }
    }

    @Override
    protected void doStop() throws Exception {
        stopSchedulerService();
    }

    @Override
    public ActivityContext getActivityContext() {
        Assert.state(getRootService().getActivityContext() != null,
            "No ActivityContext configured yet");
        return getRootService().getActivityContext();
    }

    private void startSchedulerService() throws SchedulerServiceException {
        ScheduleRuleRegistry scheduleRuleRegistry = getActivityContext().getScheduleRuleRegistry();
        if (scheduleRuleRegistry == null) {
            return;
        }

        Collection<ScheduleRule> scheduleRules = scheduleRuleRegistry.getScheduleRules();
        if (scheduleRules == null || scheduleRules.isEmpty()) {
            return;
        }

        logger.info("Now try to starting QuartzSchedulerService");

        try {
            for (ScheduleRule scheduleRule : scheduleRules) {
                Scheduler scheduler = buildScheduler((scheduleRule));
                schedulerSet.add(scheduler);
                schedulerMap.put(scheduleRule.getId(), scheduler);
            }

            for (Scheduler scheduler : schedulerSet) {
                logger.info("Starting scheduler '" + scheduler.getSchedulerName() + "'");

                // Listener attached to jobKey
                JobListener defaultJobListener = new QuartzJobListener();
                scheduler.getListenerManager().addJobListener(defaultJobListener);

                if (startDelaySeconds > 0) {
                    scheduler.startDelayed(startDelaySeconds);
                } else {
                    scheduler.start();
                }
            }
        } catch (Exception e) {
            throw new SchedulerServiceException("Could not start QuartzSchedulerService", e);
        }
    }

    private void stopSchedulerService() throws SchedulerServiceException {
        logger.info("Now try to shutting down QuartzSchedulerService");

        try {
            for (Scheduler scheduler : schedulerSet) {
                if (!scheduler.isShutdown()) {
                    logger.info("Shutting down the scheduler '" + scheduler.getSchedulerName() +
                            "' with waitForJobsToComplete=" + waitOnShutdown);
                    scheduler.shutdown(waitOnShutdown);
                }
            }
            schedulerSet.clear();
            schedulerMap.clear();
        } catch (Exception e) {
            throw new SchedulerServiceException("Could not shutdown QuartzSchedulerService", e);
        }
    }

    public void pause(String scheduleId) throws SchedulerServiceException {
        synchronized (getLock()) {
            try {
                Scheduler scheduler = getScheduler(scheduleId);
                if (scheduler != null && scheduler.isStarted()) {
                    scheduler.pauseJobs(GroupMatcher.jobGroupEquals(scheduleId));
                }
            } catch (Exception e) {
                throw new SchedulerServiceException("Could not pause scheduler '" + scheduleId + "'", e);
            }
        }
    }

    public synchronized void resume(String scheduleId) throws SchedulerServiceException {
        synchronized (getLock()) {
            try {
                Scheduler scheduler = getScheduler(scheduleId);
                if (scheduler != null && scheduler.isStarted()) {
                    scheduler.resumeJobs(GroupMatcher.jobGroupEquals(scheduleId));
                }
            } catch (Exception e) {
                throw new SchedulerServiceException("Could not resume scheduler '" + scheduleId + "'", e);
            }
        }
    }

    private Scheduler getScheduler(String scheduleId) {
        return schedulerMap.get(scheduleId);
    }

    private Scheduler buildScheduler(ScheduleRule scheduleRule) throws SchedulerException {
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
            if (isExposable(jobRule.getTransletName())) {
                JobDetail jobDetail = buildJobDetail(jobRule);
                if (jobDetail != null) {
                    String triggerName = jobDetail.getKey().getName();
                    String triggerGroup = scheduleRule.getId();
                    Trigger trigger = buildTrigger(triggerName, triggerGroup, scheduleRule);
                    scheduler.scheduleJob(jobDetail, trigger);
                }
            } else {
                logger.warn("Unavailable translet [" + jobRule.getTransletName() + "] in ScheduleRule " + scheduleRule);
            }
        }

        return scheduler;
    }

    private JobDetail buildJobDetail(ScheduledJobRule jobRule) {
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

    private Trigger buildTrigger(String name, String group, ScheduleRule scheduleRule) {
        TriggerExpressionParameters expressionParameters = scheduleRule.getTriggerExpressionParameters();
        int startDelaySeconds = this.startDelaySeconds;
        if (expressionParameters.getStartDelaySeconds() != null) {
            startDelaySeconds += expressionParameters.getStartDelaySeconds();
        }

        Date firstFireTime;
        if (startDelaySeconds > 0) {
            firstFireTime = new Date(System.currentTimeMillis() + (startDelaySeconds * 1000L));
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

}
