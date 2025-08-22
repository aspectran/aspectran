/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.embed.service.DefaultEmbeddedAspectran;
import com.aspectran.utils.Assert;
import com.aspectran.utils.OutputStringWriter;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Writer;
import java.util.Map;

/**
 * Activity implementation used by the embedded Aspectran service to process programmatic requests.
 * <p>This class extends {@link CoreActivity} and adapts the core Aspectran processing
 * pipeline to an embedded, non-web execution context. It provides embedded-specific
 * request and response adapters, allowing translets to be triggered by internal calls
 * from the embedding application.
 */
public class AspectranActivity extends CoreActivity {

    private final DefaultEmbeddedAspectran aspectran;

    private Writer outputWriter;

    private String requestName;

    private MethodType requestMethod;

    private Map<String, Object> attributeMap;

    private ParameterMap parameterMap;

    private String body;

    /**
     * Instantiates a new AspectranActivity.
     * @param aspectran the embedded Aspectran service instance
     */
    public AspectranActivity(DefaultEmbeddedAspectran aspectran) {
        this(aspectran, null);
    }

    /**
     * Instantiates a new AspectranActivity with a specific output writer.
     * @param aspectran the embedded Aspectran service instance
     * @param outputWriter the writer to which the response output will be written
     */
    public AspectranActivity(@NonNull DefaultEmbeddedAspectran aspectran, Writer outputWriter) {
        super(aspectran.getActivityContext());

        this.aspectran = aspectran;
        this.outputWriter = outputWriter;
    }

    /**
     * Returns the name of the request being processed by this activity.
     * @return the request name
     */
    public String getRequestName() {
        return requestName;
    }

    /**
     * Sets the name of the request to be processed by this activity.
     * @param requestName the request name
     */
    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    /**
     * Returns the method type of the request being processed by this activity.
     * @return the request method type
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Sets the method type of the request to be processed by this activity.
     * @param requestMethod the request method type
     */
    public void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Returns a human-readable representation of the request, combining
     * method and name when both are available (e.g., "GET foo/bar").
     * @return the combined method and request name, or just the name if no method is set
     */
    public String getFullRequestName() {
        if (requestMethod != null && requestName != null) {
            return requestMethod + " " + requestName;
        } else if (requestName != null) {
            return requestName;
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Sets a map of attributes to be passed to the request scope.
     * @param attributeMap the attribute map
     */
    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    /**
     * Sets a map of parameters to be passed to the request.
     * @param parameterMap the parameter map
     */
    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * Sets the request body content.
     * @param body the request body as a string
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Prepares this activity for execution by validating and applying the
     * configured request name and method.
     * @throws TransletNotFoundException if the target request cannot be found
     * @throws ActivityPrepareException if preparation fails for any reason
     */
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

        setFlashMapManager(aspectran.getFlashMapManager());
        setLocaleResolver(aspectran.getLocaleResolver());

        super.adapt();
    }

    @Override
    protected void saveCurrentActivity() {
        super.saveCurrentActivity();

        if (isOriginalActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().access();
        }
    }

    @Override
    protected void removeCurrentActivity() {
        if (isOriginalActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().complete();
        }

        super.removeCurrentActivity();
    }

}
