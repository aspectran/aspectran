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
package com.aspectran.core.activity;

import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.adapter.BasicRequestAdapter;
import com.aspectran.core.adapter.BasicResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.StringOutputWriter;

import java.io.Writer;
import java.util.Map;

/**
 * The Class InstantActivity
 *
 * @since 3.0.0
 */
public class InstantActivity extends DefaultActivity {

    /**
     * Instantiates a new InstantActivity.
     *
     * @param context the activity context
     */
    public InstantActivity(ActivityContext context) {
        this(context, null, null);
    }

    /**
     * Instantiates a new InstantActivity.
     *
     * @param context the activity context
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     */
    public InstantActivity(ActivityContext context, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        super(context);
        adapt(parameterMap, attributeMap);
    }

    private void adapt(ParameterMap parameterMap, Map<String, Object> attributeMap) {
        BasicRequestAdapter requestAdapter = new BasicRequestAdapter(null);
        setRequestAdapter(requestAdapter);

        Writer writer = new StringOutputWriter();
        BasicResponseAdapter responseAdapter = new BasicResponseAdapter(null, writer);
        setResponseAdapter(responseAdapter);

        if (parameterMap != null) {
            requestAdapter.setParameterMap(parameterMap);
        }
        if (attributeMap != null) {
            requestAdapter.setAttributeMap(attributeMap);
        }
    }

    @Override
    public void setSessionAdapter(SessionAdapter sessionAdapter) {
        super.setSessionAdapter(sessionAdapter);
    }

}
