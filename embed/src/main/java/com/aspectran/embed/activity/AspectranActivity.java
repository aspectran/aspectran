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
package com.aspectran.embed.activity;

import com.aspectran.core.activity.ActivityPrepareException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.embed.adapter.AspectranRequestAdapter;
import com.aspectran.embed.adapter.AspectranResponseAdapter;
import com.aspectran.embed.service.AbstractEmbeddedAspectran;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.Assert;
import com.aspectran.utils.OutputStringWriter;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Writer;
import java.util.Map;

/**
 * The Class AspectranActivity.
 */
public class AspectranActivity extends CoreActivity {

    private final EmbeddedAspectran aspectran;

    private Writer outputWriter;

    private String requestName;

    private MethodType requestMethod;

    private Map<String, Object> attributeMap;

    private ParameterMap parameterMap;

    private String body;

    /**
     * Instantiates a new embedded aspectran activity.
     * @param aspectran the embedded aspectran
     */
    public AspectranActivity(AbstractEmbeddedAspectran aspectran) {
        this(aspectran, null);
    }

    /**
     * Instantiates a new embedded aspectran activity.
     * @param aspectran the embedded aspectran
     * @param outputWriter the output writer
     */
    public AspectranActivity(@NonNull AbstractEmbeddedAspectran aspectran, Writer outputWriter) {
        super(aspectran.getActivityContext());

        this.aspectran = aspectran;
        this.outputWriter = outputWriter;
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

    public void setBody(String body) {
        this.body = body;
    }

    public void prepare() throws TransletNotFoundException, ActivityPrepareException {
        Assert.state(requestName != null, "requestName is not set");
        Assert.state(requestMethod != null, "requestMethod is not set");
        prepare(requestName, requestMethod);
    }

    @Override
    protected void adapt() throws AdapterException {
        setSessionAdapter(aspectran.newSessionAdapter());

        AspectranRequestAdapter requestAdapter = new AspectranRequestAdapter(getTranslet().getRequestMethod());
        if (parameterMap != null) {
            requestAdapter.setParameterMap(parameterMap);
        }
        if (attributeMap != null) {
            requestAdapter.setAttributeMap(attributeMap);
        }
        if (body != null) {
            requestAdapter.setBody(body);
        }
        setRequestAdapter(requestAdapter);

        if (outputWriter == null) {
            outputWriter = new OutputStringWriter();
        }
        AspectranResponseAdapter responseAdapter = new AspectranResponseAdapter(outputWriter);
        setResponseAdapter(responseAdapter);

        super.adapt();
    }

    @Override
    protected void saveCurrentActivity() {
        super.saveCurrentActivity();

        if (!hasParentActivity() && getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().access();
        }
    }

    @Override
    protected void removeCurrentActivity() {
        if (!hasParentActivity() && getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().complete();
        }

        super.removeCurrentActivity();
    }

}
