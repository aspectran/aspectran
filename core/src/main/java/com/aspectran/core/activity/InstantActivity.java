/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.OutputStringWriter;

import java.io.Writer;
import java.util.Map;

/**
 * CoreActivity could only be executed by the framework, but
 * using this InstantActivity could also be executed by user code.
 */
public class InstantActivity extends CoreActivity {

    private Map<String, Object> attributeMap;

    private ParameterMap parameterMap;

    private volatile boolean performed;

    /**
     * Instantiates a new InstantActivity.
     * @param context the activity context
     */
    public InstantActivity(ActivityContext context) {
        super(context);
    }

    @Override
    public void setSessionAdapter(SessionAdapter sessionAdapter) {
        super.setSessionAdapter(sessionAdapter);
    }

    @Override
    public void setRequestAdapter(RequestAdapter requestAdapter) {
        super.setRequestAdapter(requestAdapter);
    }

    @Override
    public void setResponseAdapter(ResponseAdapter responseAdapter) {
        super.setResponseAdapter(responseAdapter);
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    @Override
    protected void adapt() throws AdapterException {
        if (getSessionAdapter() == null && getParentActivity() != null) {
            setSessionAdapter(getParentActivity().getSessionAdapter());
        }
        if (getRequestAdapter() == null) {
            MethodType requestMethod = (getTranslet() != null ? getTranslet().getRequestMethod() : null);
            DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(requestMethod);
            setRequestAdapter(requestAdapter);
        }
        if (attributeMap != null) {
            getRequestAdapter().putAllAttributes(attributeMap);
        }
        if (parameterMap != null) {
            getRequestAdapter().putAllParameters(parameterMap);
        }
        if (getResponseAdapter() == null) {
            Writer writer = new OutputStringWriter();
            DefaultResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
            setResponseAdapter(responseAdapter);
        }

        if (!hasParentActivity() && getSessionAdapter() instanceof DefaultSessionAdapter) {
            ((DefaultSessionAdapter)getSessionAdapter()).getSessionAgent().access();
        }

        super.adapt();
    }

    @Override
    public <V> V perform(InstantAction<V> instantAction) throws ActivityPerformException {
        if (performed) {
            throw new ActivityPerformException("Activity has already been performed");
        }
        performed = true;

        return super.perform(instantAction);
    }

    @Override
    protected void release() {
        if (!hasParentActivity() && getSessionAdapter() instanceof DefaultSessionAdapter) {
            ((DefaultSessionAdapter)getSessionAdapter()).getSessionAgent().complete();
        }

        super.release();
    }

}
