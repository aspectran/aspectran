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
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.LoggingGroupHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static com.aspectran.core.scheduler.service.SchedulerService.JOB_RULE_DATA_KEY;
import static com.aspectran.core.scheduler.service.SchedulerService.SERVICE_DATA_KEY;

/**
 * The Class ActivityLauncherJob.
 */
public class ActivityLauncherJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            ScheduledJobRule jobRule = (ScheduledJobRule)jobDataMap.get(JOB_RULE_DATA_KEY);
            SchedulerService service = (SchedulerService)jobDataMap.get(SERVICE_DATA_KEY);
            if (service.isActive() && !jobRule.isDisabled()) {
                if (service.getLoggingGroup() != null) {
                    LoggingGroupHelper.set(service.getLoggingGroup());
                }
                Activity activity = perform(service.getActivityContext(), jobExecutionContext, jobRule.getTransletName());
                jobExecutionContext.setResult(activity);
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @NonNull
    private Activity perform(
            ActivityContext context, JobExecutionContext jobExecutionContext, String transletName)
            throws ActivityException {
        JobActivity activity = new JobActivity(context, jobExecutionContext);
        activity.prepare(transletName);
        activity.perform();
        return activity;
    }

}
