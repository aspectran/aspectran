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
package com.aspectran.scheduler.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;

/**
 * The Class JobActivity.
 * 
 * <p>Created: 2013. 11. 18 PM 3:40:48</p>
 */
public class JobActivity extends CoreActivity {

    /**
     * Instantiates a new job activity.
     *
     * @param context the current ActivityContext
     * @param requestAdapter the request adapter
     * @param responseAdapter the response adapter
     */
    public JobActivity(ActivityContext context, RequestAdapter requestAdapter, ResponseAdapter responseAdapter) {
        super(context);

        setRequestAdapter(requestAdapter);
        setResponseAdapter(responseAdapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Activity> T newActivity() {
        JobActivity activity = new JobActivity(getActivityContext(), getRequestAdapter(), getResponseAdapter());
        activity.setIncluded(true);
        return (T)activity;
    }

}
