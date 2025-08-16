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
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Writer;
import java.util.Map;

/**
 * CoreActivity could only be executed by the framework, but
 * using this InstantActivity could also be executed by user code.
 */
public class InstantActivity extends CoreActivity {

    /**
     * The operating mode inherited from the original activity or default if standalone.
     */
    private final Mode mode;

    /**
     * Optional request attributes to be applied to the internal RequestAdapter.
     */
    private Map<String, Object> attributeMap;

    /**
     * Optional request parameters to be applied to the internal RequestAdapter.
     */
    private ParameterMap parameterMap;

    /**
     * Instantiates a new InstantActivity.
     * @param context the activity context
     */
    public InstantActivity(ActivityContext context) {
        super(context);
        mode = Mode.DEFAULT;
    }

    /**
     * Creates a new InstantActivity inheriting state from the given activity.
     * The response adapter is inherited by default.
     * @param activity the existing activity to inherit state from (must not be {@code null})
     */
    public InstantActivity(@NonNull Activity activity) {
        this(activity, true);
    }

    /**
     * Creates a new InstantActivity inheriting state from the given activity.
     * @param activity the existing activity to inherit state from (must not be {@code null})
     * @param responseAdapterInheritable whether to inherit the response adapter as well
     */
    public InstantActivity(@NonNull Activity activity, boolean responseAdapterInheritable) {
        super(activity.getActivityContext());
        this.mode = activity.getMode();
        if (activity.hasSessionAdapter()) {
            setSessionAdapter(activity.getSessionAdapter());
        }
        setRequestAdapter(activity.getRequestAdapter());
        if (responseAdapterInheritable) {
            setResponseAdapter(activity.getResponseAdapter());
        }
        setFlashMapManager(activity.getFlashMapManager());
        setLocaleResolver(activity.getLocaleResolver());
    }

    @Override
    public Mode getMode() {
        return mode;
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

    /**
     * Sets additional request attributes to be exposed to the instant activity.
     * @param attributeMap attributes to add to the RequestAdapter (may be {@code null})
     */
    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    /**
     * Sets additional request parameters to be exposed to the instant activity.
     * @param parameterMap parameters to add to the RequestAdapter (may be {@code null})
     */
    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * Prepares this instant activity by ensuring adapters are initialized and by
     * applying any configured attributes and parameters. Also ensures a writer-backed
     * ResponseAdapter if none is present.
     * @throws AdapterException if adapter initialization fails
     */
    @Override
    protected void adapt() throws AdapterException {
        if (!hasSessionAdapter() && getPendingActivity() != null && getPendingActivity().hasSessionAdapter()) {
            setSessionAdapter(getPendingActivity().getSessionAdapter());
        }
        if (getRequestAdapter() == null) {
            MethodType requestMethod = (hasTranslet() ? getTranslet().getRequestMethod() : null);
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

        super.adapt();
    }

    /**
     * Saves the current activity and marks session access when backed by a DefaultSessionAdapter.
     */
    @Override
    protected void saveCurrentActivity() {
        super.saveCurrentActivity();

        if (isOriginalActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().access();
        }
    }

    /**
     * Restores the previous activity and marks session completion when backed by a DefaultSessionAdapter.
     */
    @Override
    protected void removeCurrentActivity() {
        if (isOriginalActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().complete();
        }

        super.removeCurrentActivity();
    }

}
