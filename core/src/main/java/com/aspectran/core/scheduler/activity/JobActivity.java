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

import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.scheduler.adapter.QuartzJobRequestAdapter;
import com.aspectran.core.scheduler.adapter.QuartzJobResponseAdapter;
import org.quartz.JobExecutionContext;

/**
 * A specialized {@link CoreActivity} for executing scheduled jobs.
 * <p>This class adapts a Quartz {@link JobExecutionContext} to Aspectran's internal
 * request and response adapters, allowing a scheduled job to be processed like any
 * other request within the Aspectran framework.</p>
 *
 * @since 2013. 11. 18.
 */
public class JobActivity extends CoreActivity {

    private final JobExecutionContext jobExecutionContext;

    /**
     * Instantiates a new JobActivity.
     * @param context the current ActivityContext
     * @param jobExecutionContext the Quartz job execution context provided by the scheduler
     */
    public JobActivity(ActivityContext context, JobExecutionContext jobExecutionContext) {
        super(context);
        this.jobExecutionContext = jobExecutionContext;
    }

    @Override
    public Mode getMode() {
        return Mode.SCHEDULER;
    }

    /**
     * Adapts the Quartz {@link JobExecutionContext} to Aspectran's request and response adapters.
     * @throws AdapterException if the adaptation fails
     */
    @Override
    protected void adapt() throws AdapterException {
        RequestAdapter requestAdapter = new QuartzJobRequestAdapter(getTranslet().getRequestMethod(), jobExecutionContext);
        setRequestAdapter(requestAdapter);

        ResponseAdapter responseAdapter = new QuartzJobResponseAdapter();
        setResponseAdapter(responseAdapter);

        super.adapt();
    }

}
