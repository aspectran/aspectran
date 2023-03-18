/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * The Class JobActivity.
 * 
 * <p>Created: 2013. 11. 18 PM 3:40:48</p>
 */
public class JobActivity extends CoreActivity {

    private final JobExecutionContext jobExecutionContext;

    /**
     * Instantiates a new job activity.
     * @param context the current ActivityContext
     * @param jobExecutionContext the job execution context
     */
    public JobActivity(ActivityContext context, JobExecutionContext jobExecutionContext) {
        super(context);
        this.jobExecutionContext = jobExecutionContext;
    }

    @Override
    protected void adapt() throws AdapterException {
        RequestAdapter requestAdapter = new QuartzJobRequestAdapter(getTranslet().getRequestMethod(), jobExecutionContext);
        setRequestAdapter(requestAdapter);

        ResponseAdapter responseAdapter = new QuartzJobResponseAdapter();
        setResponseAdapter(responseAdapter);

        super.adapt();
    }

}
