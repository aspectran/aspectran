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
package com.aspectran.core.scheduler.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.component.schedule.ScheduledJobLockProvider;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.utils.logging.LoggingGroupHelper;
import org.jspecify.annotations.NonNull;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aspectran.core.scheduler.service.SchedulerService.JOB_RULE_DATA_KEY;
import static com.aspectran.core.scheduler.service.SchedulerService.SERVICE_DATA_KEY;

/**
 * A Quartz {@link Job} implementation that launches an Aspectran {@link Activity}
 * to execute a configured translet.
 * <p>This class acts as the bridge between the Quartz scheduler and the Aspectran framework,
 * allowing scheduled tasks to leverage the full power of Aspectran's activity processing.</p>
 *
 * @since 3.0.0
 */
public class ActivityLauncherJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLauncherJob.class);

    /**
     * Called by the Quartz scheduler when the job is to be executed.
     * It retrieves the necessary {@link ScheduledJobRule} and {@link SchedulerService}
     * from the {@link JobExecutionContext} and then initiates an Aspectran {@link Activity}
     * to process the associated translet.
     * @param jobExecutionContext the context for the job execution
     * @throws JobExecutionException if an error occurs during translet execution
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            ScheduledJobRule jobRule = (ScheduledJobRule)jobDataMap.get(JOB_RULE_DATA_KEY);
            SchedulerService service = (SchedulerService)jobDataMap.get(SERVICE_DATA_KEY);
            if (service.isActive() && !jobRule.isDisabled() && !jobRule.getScheduleRule().isDisabled()) {
                if (service.getLoggingGroup() != null) {
                    LoggingGroupHelper.set(service.getLoggingGroup());
                }

                String lockKey = null;
                ScheduledJobLockProvider lockProvider = service.getJobLockProvider();
                if (lockProvider != null && !jobRule.isIsolated() && !jobRule.getScheduleRule().isIsolated()) {
                    lockKey = getLockKey(service, jobRule, jobExecutionContext);
                    if (!lockProvider.lock(lockKey)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Skipping execution of scheduled job '{}' as it is already locked by another node",
                                    lockKey);
                        }
                        return;
                    }
                }

                try {
                    Activity activity = perform(service.getActivityContext(), jobExecutionContext, jobRule.getTransletName());
                    jobExecutionContext.setResult(activity);
                } finally {
                    if (lockKey != null) {
                        lockProvider.unlock(lockKey);
                    }
                }
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    /**
     * Generates a unique lock key for the scheduled job.
     * The key includes the scheduled fire time to ensure that nodes with slight clock drifts
     * all compete for the same lock key for a specific execution interval.
     * @param service the scheduler service
     * @param jobRule the scheduled job rule
     * @param context the job execution context
     * @return the unique lock key
     */
    @NonNull
    private String getLockKey(@NonNull SchedulerService service, ScheduledJobRule jobRule, JobExecutionContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("job-lock:");
        if (service.getActivityContext().getName() != null) {
            sb.append(service.getActivityContext().getName()).append(":");
        }
        sb.append(jobRule.getScheduleRule().getId()).append(":");
        sb.append(jobRule.getTransletName()).append(":");
        sb.append(context.getScheduledFireTime().getTime());
        return sb.toString();
    }

    /**
     * Performs the actual execution of the Aspectran translet within a new {@link JobActivity}.
     * @param context the current ActivityContext
     * @param jobExecutionContext the Quartz job execution context
     * @param transletName the name of the translet to execute
     * @return the {@link Activity} instance used for execution
     * @throws ActivityException if an error occurs during execution
     */
    @NonNull
    private Activity perform(ActivityContext context, JobExecutionContext jobExecutionContext, String transletName)
            throws ActivityException {
        JobActivity activity = new JobActivity(context, jobExecutionContext);
        activity.prepare(transletName);
        activity.perform();
        return activity;
    }

}
