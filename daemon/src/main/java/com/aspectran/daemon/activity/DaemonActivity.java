/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.activity.ActivityPrepareException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.daemon.adapter.DaemonRequestAdapter;
import com.aspectran.daemon.adapter.DaemonResponseAdapter;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.OutputStringWriter;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Writer;
import java.util.Map;

/**
 * An activity that processes a daemon command.
 */
public class DaemonActivity extends CoreActivity {

    private final DaemonService daemonService;

    private String requestName;

    private MethodType requestMethod;

    private Map<String, Object> attributeMap;

    private ParameterMap parameterMap;

    /**
     * Instantiates a new daemon activity.
     * @param daemonService the daemon service
     */
    public DaemonActivity(@NonNull DaemonService daemonService) {
        super(daemonService.getActivityContext());
        this.daemonService = daemonService;
    }

    @Override
    public Mode getMode() {
        return Mode.DAEMON;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public MethodType getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getFullRequestName() {
        if (requestMethod != null && requestName != null) {
            return requestMethod + " " + requestName;
        } else if (requestName != null) {
            return requestName;
        } else {
            return StringUtils.EMPTY;
        }
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void prepare() throws TransletNotFoundException, ActivityPrepareException {
        Assert.state(requestName != null, "requestName is not set");
        Assert.state(requestMethod != null, "requestMethod is not set");
        prepare(requestName, requestMethod);
    }

    @Override
    protected void adapt() throws AdapterException {
        setSessionAdapter(daemonService.newSessionAdapter());

        DaemonRequestAdapter requestAdapter = new DaemonRequestAdapter(getTranslet().getRequestMethod());
        if (attributeMap != null) {
            requestAdapter.setAttributeMap(attributeMap);
        }
        if (parameterMap != null) {
            requestAdapter.setParameterMap(parameterMap);
        }
        setRequestAdapter(requestAdapter);

        Writer outputWriter = new OutputStringWriter();
        DaemonResponseAdapter responseAdapter = new DaemonResponseAdapter(outputWriter);
        setResponseAdapter(responseAdapter);

        super.adapt();
    }

    @Override
    protected void saveCurrentActivity() {
        super.saveCurrentActivity();

        if (!hasParentActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().access();
        }
    }

    @Override
    protected void removeCurrentActivity() {
        if (!hasParentActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().complete();
        }

        super.removeCurrentActivity();
    }

}
