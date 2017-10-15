/*
 * Copyright (c) 2008-2017 The Aspectran Project
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

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.adapter.BasicRequestAdapter;
import com.aspectran.core.adapter.BasicResponseAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringOutputWriter;

import java.io.Writer;
import java.util.Map;

/**
 * The Class InstantActivity
 *
 * @since 3.0.0
 */
public class InstantActivity extends BasicActivity {

    private ParameterMap parameterMap;

    private Map<String, Object> attributeMap;

    /**
     * Instantiates a new instant activity.
     *
     * @param context the activity context
     */
    public InstantActivity(ActivityContext context) {
        super(context);
    }

    /**
     * Instantiates a new instant activity.
     *
     * @param context the activity context
     * @param sessionAdapter the session adapter
     */
    public InstantActivity(ActivityContext context, SessionAdapter sessionAdapter) {
        super(context);
        setSessionAdapter(sessionAdapter);
    }

    /**
     * Sets the parameter map.
     *
     * @param parameterMap the parameter map
     */
    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * Sets the attribute map.
     *
     * @param attributeMap the attribute map
     */
    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    public void adapt() throws AdapterException {
        try {
            RequestAdapter requestAdapter = new BasicRequestAdapter(null, parameterMap);
            setRequestAdapter(requestAdapter);

            Writer writer = new StringOutputWriter();
            ResponseAdapter responseAdapter = new BasicResponseAdapter(null, writer);
            setResponseAdapter(responseAdapter);

            if (attributeMap != null) {
                for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
                    requestAdapter.setAttribute(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            throw new AdapterException("Could not adapt to InstantActivity", e);
        }
    }

    @Override
    public <T extends Activity> T newActivity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(String transletName) {
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
    public void perform() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performWithoutResponse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodType getRequestMethod() {
        return null;
    }

    @Override
    public String getTransletName() {
        return null;
    }

    @Override
    public Translet getTranslet() {
        return null;
    }

    @Override
    public ProcessResult getProcessResult() {
        return null;
    }

    @Override
    public Object getProcessResult(String actionId) {
        return null;
    }

    @Override
    public boolean isResponseReserved() {
        return false;
    }

}
