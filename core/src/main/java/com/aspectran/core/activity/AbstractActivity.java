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
package com.aspectran.core.activity;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * The Class AbstractActivity.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public abstract class AbstractActivity implements Activity {

    private final ActivityContext context;

    private Activity parentActivity;

    private SessionAdapter sessionAdapter;

    private RequestAdapter requestAdapter;

    private ResponseAdapter responseAdapter;

    private Throwable raisedException;

    /**
     * Instantiates a new abstract activity.
     * @param context the activity context
     */
    protected AbstractActivity(@NonNull ActivityContext context) {
        this.context = context;
    }

    @Override
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public ClassLoader getClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // ignore
        }
        if (cl == null) {
            cl = context.getClassLoader();
        }
        return cl;
    }

    @Override
    public Environment getEnvironment() {
        return context.getEnvironment();
    }

    /**
     * Gets the current activity.
     * @return the current activity
     */
    protected Activity getCurrentActivity() {
        return context.getCurrentActivity();
    }

    /**
     * Saves the current activity.
     */
    protected void saveCurrentActivity() {
        if (context.hasCurrentActivity()) {
            parentActivity = context.getCurrentActivity();
        }
        context.setCurrentActivity(this);
    }

    /**
     * Removes the current activity.
     */
    protected void removeCurrentActivity() {
        if (parentActivity != null) {
            context.setCurrentActivity(parentActivity);
        } else {
            context.removeCurrentActivity();
        }
    }

    @SuppressWarnings("unchecked")
    protected <V extends Activity> V getParentActivity() {
        return (V)parentActivity;
    }

    protected boolean hasParentActivity() {
        return (parentActivity != null);
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return context.getApplicationAdapter();
    }

    @Override
    public SessionAdapter getSessionAdapter() {
        return sessionAdapter;
    }

    /**
     * Sets the session adapter.
     * @param sessionAdapter the new session adapter
     */
    protected void setSessionAdapter(SessionAdapter sessionAdapter) {
        this.sessionAdapter = sessionAdapter;
    }

    @Override
    public RequestAdapter getRequestAdapter() {
        return requestAdapter;
    }

    /**
     * Sets the request adapter.
     * @param requestAdapter the new request adapter
     */
    protected void setRequestAdapter(RequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    @Override
    public ResponseAdapter getResponseAdapter() {
        return responseAdapter;
    }

    /**
     * Sets the response adapter.
     * @param responseAdapter the new response adapter
     */
    protected void setResponseAdapter(ResponseAdapter responseAdapter) {
        this.responseAdapter = responseAdapter;
    }

    @Override
    public boolean isExceptionRaised() {
        return (raisedException != null);
    }

    @Override
    public Throwable getRaisedException() {
        return raisedException;
    }

    @Override
    public Throwable getRootCauseOfRaisedException() {
        if (raisedException != null) {
            return ExceptionUtils.getRootCause(raisedException);
        } else {
            return null;
        }
    }

    @Override
    public void setRaisedException(Throwable raisedException) {
        if (this.raisedException == null) {
            this.raisedException = raisedException;
        }
    }

    @Override
    public void clearRaisedException() {
        raisedException = null;
    }

    @Override
    public void terminate() throws ActivityTerminatedException {
        terminate("Explicitly terminated by calling terminate()");
    }

    @Override
    public void terminate(String cause) throws ActivityTerminatedException {
        throw new ActivityTerminatedException(cause);
    }

    //---------------------------------------------------------------------
    // Implementation of BeanRegistry interface
    //---------------------------------------------------------------------

    @Override
    public <V> V getBean(String id) {
        return context.getBeanRegistry().getBean(id);
    }

    @Override
    public <V> V getBean(Class<V> type) {
        return context.getBeanRegistry().getBean(type);
    }

    @Override
    public <V> V getBean(Class<V> type, String id) {
        return context.getBeanRegistry().getBean(type, id);
    }

    @Override
    public <V> V getPrototypeScopeBean(BeanRule beanRule) {
        return context.getBeanRegistry().getPrototypeScopeBean(beanRule);
    }

    @Override
    public boolean containsBean(String id) {
        return context.getBeanRegistry().containsBean(id);
    }

    @Override
    public boolean containsBean(Class<?> type) {
        return context.getBeanRegistry().containsBean(type);
    }

    @Override
    public boolean containsBean(Class<?> type, String id) {
        return context.getBeanRegistry().containsBean(type, id);
    }

}
