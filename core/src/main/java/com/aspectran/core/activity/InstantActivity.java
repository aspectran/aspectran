/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.adapter.BasicRequestAdapter;
import com.aspectran.core.adapter.BasicResponseAdapter;
import com.aspectran.core.adapter.BasicSessionAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.OutputStringWriter;

import java.io.Writer;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * An activity that handles the temporary request.
 *
 * <p>Note that this is an activity that has nothing to do with
 * advice. This does not execute any advice at all, and if you
 * attempt to register the advice dynamically, you will get an
 * exception of the advice constraint violation.</p>
 *
 * @since 3.0.0
 */
public class InstantActivity extends CoreActivity {

    private ParameterMap parameterMap;

    private Map<String, Object> attributeMap;

    private volatile boolean performed;

    /**
     * Instantiates a new InstantActivity.
     *
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

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    @Override
    protected void adapt() throws AdapterException {
        if (getSessionAdapter() == null && getParentActivity() != null) {
            setSessionAdapter(getParentActivity().getSessionAdapter());
        }
        if (getRequestAdapter() == null) {
            MethodType requestMethod = (getTranslet() != null ? getTranslet().getRequestMethod() : null);
            BasicRequestAdapter requestAdapter = new BasicRequestAdapter(requestMethod, null);
            setRequestAdapter(requestAdapter);
        }
        if (getResponseAdapter() == null) {
            Writer writer = new OutputStringWriter();
            BasicResponseAdapter responseAdapter = new BasicResponseAdapter(null, writer);
            setResponseAdapter(responseAdapter);
        }

        if (!hasParentActivity() && getSessionAdapter() instanceof BasicSessionAdapter) {
            ((BasicSessionAdapter)getSessionAdapter()).getSessionAgent().access();
        }

        super.adapt();
    }

    @Override
    protected void parseRequest() {
        if (parameterMap != null) {
            getRequestAdapter().putAllParameters(parameterMap);
        }
        if (attributeMap != null) {
            getRequestAdapter().putAllAttributes(attributeMap);
        }
    }

    @Override
    public <V> V perform(Callable<V> instantAction) throws ActivityPerformException {
        if (performed) {
            throw new ActivityPerformException("Activity has already been performed");
        }
        performed = true;

        return super.perform(instantAction);
    }

    @Override
    protected void release() {
        if (!hasParentActivity() && getSessionAdapter() instanceof BasicSessionAdapter) {
            ((BasicSessionAdapter)getSessionAdapter()).getSessionAgent().complete();
        }

        super.release();
    }

}
