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
package com.aspectran.daemon.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.daemon.adapter.DaemonRequestAdapter;
import com.aspectran.daemon.adapter.DaemonResponseAdapter;
import com.aspectran.daemon.service.DaemonService;

import java.io.Writer;
import java.util.Map;

/**
 * The Class DaemonActivity.
 */
public class DaemonActivity extends CoreActivity {

    private final DaemonService service;

    private final Writer outputWriter;

    private ParameterMap parameterMap;

    private Map<String, Object> attributeMap;

    /**
     * Instantiates a new daemon activity.
     *
     * @param service the daemon service
     * @param outputWriter the output writer
     */
    public DaemonActivity(DaemonService service, Writer outputWriter) {
        super(service.getActivityContext());

        this.service = service;
        this.outputWriter = outputWriter;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            setSessionAdapter(service.newSessionAdapter());

            DaemonRequestAdapter requestAdapter = new DaemonRequestAdapter();
            setRequestAdapter(requestAdapter);

            DaemonResponseAdapter responseAdapter = new DaemonResponseAdapter(outputWriter);
            setResponseAdapter(responseAdapter);

            if (parameterMap != null) {
                requestAdapter.setParameterMap(parameterMap);
            }
            if (attributeMap != null) {
                requestAdapter.setAttributeMap(attributeMap);
            }

            super.adapt();
        } catch (Exception e) {
            throw new AdapterException("Failed to specify adapters required for daemon service activity", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Activity> T newActivity() {
        DaemonActivity activity = new DaemonActivity(service, outputWriter);
        activity.setIncluded(true);
        return (T)activity;
    }

}
