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
package com.aspectran.scheduler.service;

import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ScheduleJobRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.params.TriggerParameters;
import com.aspectran.core.context.rule.type.TriggerType;
import com.aspectran.core.service.AbstractServiceController;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.PluralWildcardPattern;
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

    public final static String ACTIVITY_CONTEXT_DATA_KEY = "ACTIVITY_CONTEXT";

    public final static String ACTIVITY_DATA_KEY = "ACTIVITY";

    private final Log log = LogFactory.getLog(QuartzSchedulerService.class);

    private ActivityContext context;

    private final Set<Scheduler> schedulerSet = new HashSet<>();

    private Map<String, Scheduler> schedulerMap = new HashMap<>();

    private int startDelaySeconds = 0;

    private boolean waitOnShutdown = false;

    private PluralWildcardPattern exposableTransletNamesPattern;

    public QuartzSchedulerService(ActivityContext context) {
        this.context = context;
    }

    @Override
    public int getStartDelaySeconds() {
        return startDelaySeconds;
    }

    @Override
    public void setStartDelaySeconds(int startDelaySeconds) {
        this.startDelaySeconds = startDelaySeconds;
    }

    @Override
    public boolean isWaitOnShutdown() {
        return waitOnShutdown;
    }

    @Override
    public void setWaitOnShutdown(boolean waitOnShutdown) {
        this.waitOnShutdown = waitOnShutdown;
    }

    @Override
    public void setExposals(String[] includePatterns, String[] excludePatterns) {
        if ((includePatterns != null && includePatterns.length > 0) ||
                excludePatterns != null && excludePatterns.length > 0) {
            exposableTransletNamesPattern = new PluralWildcardPattern(includePatterns, excludePatterns,
                    ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR);
        }
    }

    private boolean isExposable(String transletName) {
        return (exposableTransletNamesPattern == null || exposableTransletNamesPattern.matches(transletName));
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
    protected void doRestart() throws Exception {
        stopSchedulerService();
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
        throw new UnsupportedOperationException();
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

    private void startSchedulerService() throws SchedulerServiceException {
        if (context.getScheduleRuleRegistry() == null) {
            return;
        }

        Collection<ScheduleRule> scheduleRules = context.getScheduleRuleRegistry().getScheduleRules();
        if (scheduleRules == null || scheduleRules.isEmpty()) {
            return;
        }

        log.info("Now try to starting QuartzSchedulerService");

        try {
            for (ScheduleRule scheduleRule : scheduleRules) {
                Scheduler scheduler = buildScheduler((scheduleRule));
                schedulerSet.add(scheduler);
                schedulerMap.put(scheduleRule.getId(), scheduler);
            }

            for (Scheduler scheduler : schedulerSet) {
                log.info("Starting scheduler '" + scheduler.getSchedulerName() + "'");

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
        log.info("Now try to shutting down QuartzSchedulerService");

        try {
            for (Scheduler scheduler : schedulerSet) {
                if (!scheduler.isShutdown()) {
                    log.info("Shutting down the scheduler '" + scheduler.getSchedulerName() +
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

    private Scheduler getScheduler(String scheduleId) throws SchedulerException {
        return schedulerMap.get(scheduleId);
    }

    private Scheduler buildScheduler(ScheduleRule scheduleRule) throws SchedulerException {
        Scheduler scheduler = null;
        if (scheduleRule.getSchedulerBeanClass() != null) {
            scheduler = (Scheduler)context.getBeanRegistry().getBean(scheduleRule.getSchedulerBeanClass());
        } else if (scheduleRule.getSchedulerBeanId() != null) {
            scheduler = context.getBeanRegistry().getBean(scheduleRule.getSchedulerBeanId());
        }
        if (scheduler == null) {
            throw new ActionExecutionException("No such Scheduler bean; Invalid ScheduleRule " + scheduleRule);
        }

        List<ScheduleJobRule> jobRuleList = scheduleRule.getScheduleJobRuleList();
        for (ScheduleJobRule jobRule : jobRuleList) {
            if (isExposable(jobRule.getTransletName())) {
                JobDetail jobDetail = buildJobDetail(jobRule);
                if (jobDetail != null) {
                    String triggerName = jobDetail.getKey().getName();
                    String triggerGroup = scheduleRule.getId();
                    Trigger trigger = buildTrigger(triggerName, triggerGroup, scheduleRule);

                    scheduler.scheduleJob(jobDetail, trigger);
                }
            } else {
                log.warn("Unexposable translet [" + jobRule.getTransletName() + "] in ScheduleRule " + scheduleRule);
            }
        }

        return scheduler;
    }

    private JobDetail buildJobDetail(ScheduleJobRule scheduleJobRule) {
        if (scheduleJobRule.isDisabled()) {
            return null;
        }

        String jobName = scheduleJobRule.getTransletName();
        String jobGroup = scheduleJobRule.getScheduleRule().getId();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ACTIVITY_CONTEXT_DATA_KEY, context);

        return JobBuilder.newJob(ActivityLauncherJob.class)
                .withIdentity(jobName, jobGroup)
                .setJobData(jobDataMap)
                .build();
    }

    private Trigger buildTrigger(String name, String group, ScheduleRule scheduleRule) {
        Trigger trigger;

        Parameters triggerParameters = scheduleRule.getTriggerParameters();
        Integer triggerStartDelaySeconds = triggerParameters.getInt(TriggerParameters.startDelaySeconds);
        int intTriggerStartDelaySeconds = (triggerStartDelaySeconds != null) ? triggerStartDelaySeconds : 0;

        Date firstFireTime;
        if (startDelaySeconds > 0 || (triggerStartDelaySeconds != null && triggerStartDelaySeconds > 0)) {
            firstFireTime = new Date(System.currentTimeMillis() + ((startDelaySeconds + intTriggerStartDelaySeconds) * 1000L));
        } else {
            firstFireTime = new Date();
        }

        if (scheduleRule.getTriggerType() == TriggerType.SIMPLE) {
            Long intervalInMilliseconds = triggerParameters.getLong(TriggerParameters.intervalInMilliseconds);
            Integer intervalInSeconds = triggerParameters.getInt(TriggerParameters.intervalInSeconds);
            Integer intervalInMinutes = triggerParameters.getInt(TriggerParameters.intervalInMinutes);
            Integer intervalInHours = triggerParameters.getInt(TriggerParameters.intervalInHours);
            Integer repeatCount = triggerParameters.getInt(TriggerParameters.repeatCount);
            Boolean repeatForever = triggerParameters.getBoolean(TriggerParameters.repeatForever);

            SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule();

            if (intervalInMilliseconds != null) {
                builder.withIntervalInMilliseconds(intervalInMilliseconds);
            }
            if (intervalInMinutes != null) {
                builder.withIntervalInMinutes(intervalInMinutes);
            }
            if (intervalInSeconds != null) {
                builder.withIntervalInSeconds(intervalInSeconds);
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

            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .startAt(firstFireTime)
                    .withSchedule(builder)
                    .build();
        } else {
            String expression = triggerParameters.getString(TriggerParameters.expression);
            CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(expression);

            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .startAt(firstFireTime)
                    .withSchedule(cronSchedule)
                    .build();
        }

        return trigger;
    }

}
