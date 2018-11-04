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
package com.aspectran.core.activity;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.util.ExceptionUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractActivity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public abstract class AbstractActivity implements Activity {

    private static final Log log = LogFactory.getLog(AbstractActivity.class);

    private final ActivityContext context;

    private boolean included;

    private Activity outerActivity;

    private SessionAdapter sessionAdapter;

    private RequestAdapter requestAdapter;

    private ResponseAdapter responseAdapter;

    private Throwable raisedException;

    /**
     * Instantiates a new abstract activity.
     *
     * @param context the activity context
     */
    protected AbstractActivity(ActivityContext context) {
        this.context = context;
    }

    @Override
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public Environment getEnvironment() {
        return context.getEnvironment();
    }

    /**
     * Gets the current activity.
     *
     * @return the current activity
     */
    protected Activity getCurrentActivity() {
        return context.getCurrentActivity();
    }

    /**
     * Saves the current activity.
     */
    protected void saveCurrentActivity() {
        context.setCurrentActivity(this);
    }

    /**
     * Backups the current activity.
     */
    protected void backupCurrentActivity() {
        outerActivity = getCurrentActivity();
    }

    /**
     * Removes the current activity.
     */
    protected void removeCurrentActivity() {
        if (outerActivity != null) {
            context.setCurrentActivity(outerActivity);
        } else {
            context.removeCurrentActivity();
        }
    }

    @Override
    public boolean isIncluded() {
        return included;
    }

    /**
     * Sets whether this activity is included in other activity.
     *
     * @param included whether or not included in other activity
     */
    public void setIncluded(boolean included) {
        this.included = included;
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return getEnvironment().getApplicationAdapter();
    }

    @Override
    public SessionAdapter getSessionAdapter() {
        return sessionAdapter;
    }

    /**
     * Sets the session adapter.
     *
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
     *
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
     *
     * @param responseAdapter the new response adapter
     */
    protected void setResponseAdapter(ResponseAdapter responseAdapter) {
        this.responseAdapter = responseAdapter;
    }

    @Override
    public Throwable getRaisedException() {
        return raisedException;
    }

    @Override
    public void setRaisedException(Throwable raisedException) {
        if (this.raisedException == null) {
            this.raisedException = raisedException;
            if (log.isDebugEnabled()) {
                log.error("Raised exception: ", getRootCauseOfRaisedException());
            }
        }
    }

    @Override
    public boolean isExceptionRaised() {
        return (this.raisedException != null);
    }

    @Override
    public Throwable getRootCauseOfRaisedException() {
        return ExceptionUtils.getRootCause(raisedException);
    }

    @Override
    public <T> T getBean(String id) {
        return context.getBeanRegistry().getBean(id);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return context.getBeanRegistry().getBean(requiredType);
    }

    @Override
    public <T> T getBean(String id, Class<T> requiredType) {
        return context.getBeanRegistry().getBean(id, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, String id) {
        return context.getBeanRegistry().getBean(requiredType, id);
    }

    @Override
    public <T> T getConfigBean(Class<T> classType) {
        return context.getBeanRegistry().getConfigBean(classType);
    }

    @Override
    public boolean containsBean(String id) {
        return context.getBeanRegistry().containsBean(id);
    }

    @Override
    public boolean containsBean(Class<?> requiredType) {
        return context.getBeanRegistry().containsBean(requiredType);
    }

    @Override
    public void terminate() {
        terminate("Terminated by user code");
    }

    @Override
    public void terminate(String cause) {
        throw new ActivityTerminatedException(cause);
    }

}
