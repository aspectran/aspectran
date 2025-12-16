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
import com.aspectran.utils.io.OutputStringWriter;
import org.jspecify.annotations.NonNull;

import java.io.Writer;
import java.util.Map;

/**
 * A specialized {@link CoreActivity} that can be instantiated and executed
 * programmatically by user code.
 *
 * <p>While a standard {@code CoreActivity} is typically managed by the framework in
 * response to an external request (e.g., an HTTP request), this class allows for
 * the on-the-fly execution of a block of code (an {@link InstantAction}) or a
 * translet within a fully-managed activity context. This is useful for:
 * <ul>
 *   <li>Executing a translet from within another service or job.</li>
 *   <li>Unit or integration testing of components that require an active
 *   {@code Activity} context.</li>
 *   <li>Running a piece of logic with access to the full Aspectran environment,
 *   including beans, configuration, and other services.</li>
 * </ul>
 * <p>It can be configured with its own request parameters and attributes, and if no
 * response adapter is provided, it defaults to using an {@link OutputStringWriter}
 * to capture any output.
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
     * Creates a new standalone {@code InstantActivity}.
     * @param context the activity context
     */
    public InstantActivity(ActivityContext context) {
        super(context);
        mode = Mode.DEFAULT;
    }

    /**
     * Creates a new {@code InstantActivity} that inherits state from an existing one.
     * The response adapter is inherited by default.
     * @param activity the existing activity to inherit state from (must not be {@code null})
     */
    public InstantActivity(@NonNull Activity activity) {
        this(activity, true);
    }

    /**
     * Creates a new {@code InstantActivity} that inherits state from an existing one.
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
     * Sets request attributes to be exposed to this instant activity.
     * @param attributeMap attributes to add to the RequestAdapter (may be {@code null})
     */
    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    /**
     * Sets request parameters to be exposed to this instant activity.
     * @param parameterMap parameters to add to the RequestAdapter (may be {@code null})
     */
    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * Prepares this instant activity by ensuring adapters are initialized and by
     * applying any configured attributes and parameters. If no response adapter is
     * present, a new {@link DefaultResponseAdapter} backed by an
     * {@link OutputStringWriter} will be created to capture output.
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
