/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.logging.LoggingGroupHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * The Class ActivityJobListener.
 *
 * <p>Created: 2016. 9. 4.</p>
 *
 * @since 3.0.0
 */
public class ActivityJobListener implements JobListener {

    private static final String LISTENER_NAME = "activityJobListener";

    private final String loggingGroup;

    public ActivityJobListener(String loggingGroup) {
        this.loggingGroup = loggingGroup;
    }

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        if (loggingGroup != null) {
            LoggingGroupHelper.set(loggingGroup);
        }
        ActivityJobReporter.jobToBeExecuted(context, false);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        if (loggingGroup != null) {
            LoggingGroupHelper.set(loggingGroup);
        }
        ActivityJobReporter.jobToBeExecuted(context, true);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (loggingGroup != null) {
            LoggingGroupHelper.set(loggingGroup);
        }
        ActivityJobReporter.jobWasExecuted(context, jobException);
    }

}
