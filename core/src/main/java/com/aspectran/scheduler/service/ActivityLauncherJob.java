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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.scheduler.activity.JobActivity;
import com.aspectran.scheduler.adapter.QuartzJobRequestAdapter;
import com.aspectran.scheduler.adapter.QuartzJobResponseAdapter;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * The Class ActivityLauncherJob.
 */
public class ActivityLauncherJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDetail jobDetail = jobExecutionContext.getJobDetail();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();

            ActivityContext context = (ActivityContext)jobDataMap.get(QuartzSchedulerService.ACTIVITY_CONTEXT_DATA_KEY);
            String transletName = jobDetail.getKey().getName();

            Activity activity = runActivity(context, transletName, jobExecutionContext);
            jobExecutionContext.put(QuartzSchedulerService.ACTIVITY_DATA_KEY, activity);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    private Activity runActivity(ActivityContext context, String transletName, JobExecutionContext jobExecutionContext)
            throws ActivityException {
        RequestAdapter requestAdapter = new QuartzJobRequestAdapter(jobExecutionContext);
        ResponseAdapter responseAdapter = new QuartzJobResponseAdapter();

        Activity activity = null;
        try {
            activity = new JobActivity(context, requestAdapter, responseAdapter);
            activity.prepare(transletName);
            activity.perform();
        } finally {
            if (activity != null) {
                activity.finish();
            }
        }
        return activity;
    }

}
