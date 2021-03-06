/*
 * Copyright (c) 2008-2021 The Aspectran Project
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

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.util.OutputStringWriter;
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
        setSessionAdapter(service.newSessionAdapter());

        DaemonRequestAdapter requestAdapter = new DaemonRequestAdapter(getTranslet().getRequestMethod());
        setRequestAdapter(requestAdapter);

        if (outputWriter == null) {
            outputWriter = new OutputStringWriter();
        }
        DaemonResponseAdapter responseAdapter = new DaemonResponseAdapter(outputWriter);
        setResponseAdapter(responseAdapter);

        if (!hasParentActivity() && getSessionAdapter() instanceof DefaultSessionAdapter) {
            ((DefaultSessionAdapter)getSessionAdapter()).getSessionAgent().access();
        }

        super.adapt();
    }

    @Override
    protected void parseRequest() throws ActivityTerminatedException, RequestParseException {
        if (parameterMap != null) {
            ((DaemonRequestAdapter)getRequestAdapter()).setParameterMap(parameterMap);
        }
        if (attributeMap != null) {
            ((DaemonRequestAdapter)getRequestAdapter()).setAttributeMap(attributeMap);
        }

        super.parseRequest();
    }

    @Override
    protected void release() {
        if (!hasParentActivity() && getSessionAdapter() instanceof DefaultSessionAdapter) {
            ((DefaultSessionAdapter)getSessionAdapter()).getSessionAgent().complete();
        }

        super.release();
    }

}
