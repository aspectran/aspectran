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
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * The Class QuartzJobListener.
 *
 * <p>Created: 2016. 9. 4.</p>
 *
 * @since 3.0.0
 */
public class QuartzJobListener implements JobListener {

    public static final String LISTENER_NAME = "defaultJobListener";

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobActivityReport report = new JobActivityReport(context, jobException);
        Activity activity = (Activity)context.get(QuartzSchedulerService.ACTIVITY_DATA_KEY);
        report.reporting(activity);
    }

}
