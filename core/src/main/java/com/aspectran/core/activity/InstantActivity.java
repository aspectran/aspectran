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
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.OutputStringWriter;

import java.io.Writer;
import java.util.Map;

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
public class InstantActivity<T> extends CoreActivity {

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

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    @Override
    public void setSessionAdapter(SessionAdapter sessionAdapter) {
        super.setSessionAdapter(sessionAdapter);
    }

    @Override
    public void prepare(String transletName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(TransletRule transletRule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(String transletName, TransletRule transletRule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(String transletName, String requestMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(String transletName, MethodType requestMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void adapt() throws AdapterException {
        BasicRequestAdapter requestAdapter = new BasicRequestAdapter(null, null);
        setRequestAdapter(requestAdapter);

        Writer writer = new OutputStringWriter();
        BasicResponseAdapter responseAdapter = new BasicResponseAdapter(null, writer);
        setResponseAdapter(responseAdapter);

        if (parameterMap != null) {
            requestAdapter.setParameterMap(parameterMap);
        }
        if (attributeMap != null) {
            requestAdapter.setAttributeMap(attributeMap);
        }

        super.adapt();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T perform(InstantAction instantAction) throws ActivityPerformException, ActivityTerminatedException {
        if (performed) {
            throw new ActivityPerformException("Activity has already been performed");
        }

        performed = true;

        if (getParentActivity() == null && getSessionAdapter() instanceof BasicSessionAdapter) {
            ((BasicSessionAdapter)getSessionAdapter()).getSessionAgent().access();
        }

        return (T)super.perform(instantAction);
    }

    @Override
    protected void release() {
        if (getParentActivity() == null && getSessionAdapter() instanceof BasicSessionAdapter) {
            ((BasicSessionAdapter)getSessionAdapter()).getSessionAgent().complete();
        }

        super.release();
    }

}
