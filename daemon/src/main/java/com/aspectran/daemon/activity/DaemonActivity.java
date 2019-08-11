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
package com.aspectran.daemon.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.BasicSessionAdapter;
import com.aspectran.core.util.StringOutputWriter;
import com.aspectran.daemon.adapter.DaemonRequestAdapter;
import com.aspectran.daemon.adapter.DaemonResponseAdapter;
import com.aspectran.daemon.service.DaemonService;

import java.io.Writer;
import java.util.Map;

/**
 * An activity that processes a daemon command.
 */
public class DaemonActivity extends CoreActivity {

    private final DaemonService service;

    private Writer outputWriter;

    private ParameterMap parameterMap;

    private Map<String, Object> attributeMap;

    /**
     * Instantiates a new daemon activity.
     *
     * @param service the daemon service
     */
    public DaemonActivity(DaemonService service) {
        super(service.getActivityContext());
        this.service = service;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    public void setOutputWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            if (getOuterActivity() != null) {
                setSessionAdapter(getOuterActivity().getSessionAdapter());
            } else {
                setSessionAdapter(service.newSessionAdapter());
            }

            DaemonRequestAdapter requestAdapter = new DaemonRequestAdapter(getTranslet().getRequestMethod());
            if (getOuterActivity() != null) {
                requestAdapter.preparse(getOuterActivity().getRequestAdapter());
            } else {
                requestAdapter.preparse(attributeMap, parameterMap);
            }
            setRequestAdapter(requestAdapter);

            if (outputWriter == null) {
                outputWriter = new StringOutputWriter();
            }
            DaemonResponseAdapter responseAdapter = new DaemonResponseAdapter(outputWriter);
            setResponseAdapter(responseAdapter);

            super.adapt();
        } catch (Exception e) {
            throw new AdapterException("Failed to adapt for Daemon Activity", e);
        }
    }

    @Override
    public void perform() {
        if (getOuterActivity() == null) {
            ((BasicSessionAdapter)getSessionAdapter()).getSessionAgent().access();
        }
        super.perform();
    }

    @Override
    protected void release() {
        if (getOuterActivity() == null) {
            ((BasicSessionAdapter)getSessionAdapter()).getSessionAgent().complete();
        }
        super.release();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Activity> T newActivity() {
        DaemonActivity activity = new DaemonActivity(service);
        activity.setOutputWriter(outputWriter);
        activity.setIncluded(true);
        return (T)activity;
    }

}
