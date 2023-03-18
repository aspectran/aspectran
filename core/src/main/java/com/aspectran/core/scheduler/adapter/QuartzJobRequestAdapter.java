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
package com.aspectran.core.scheduler.adapter;

import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import org.quartz.JobExecutionContext;

/**
 * Adapt Quartz Job Request to Core {@link RequestAdapter}.
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobRequestAdapter extends DefaultRequestAdapter {

    /**
     * Instantiates a new QuartzJobRequestAdapter.
     * @param requestMethod the request method
     * @param jobExecutionContext the job execution context
     */
    public QuartzJobRequestAdapter(MethodType requestMethod, JobExecutionContext jobExecutionContext) {
        super(requestMethod, jobExecutionContext);
    }

}
