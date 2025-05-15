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
package com.aspectran.core.activity;

import com.aspectran.core.activity.aspect.AdviceException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.item.ItemEvaluation;
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.asel.token.TokenEvaluation;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.StringifyContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class AbstractActivity.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public abstract class AbstractActivity implements Activity {

    private final ActivityContext context;

    private Activity pendingActivity;

    private SessionAdapter sessionAdapter;

    private RequestAdapter requestAdapter;

    private ResponseAdapter responseAdapter;

    private Throwable raisedException;

    private Map<String, Object> settings;

    private StringifyContext stringifyContext;

    private TokenEvaluator tokenEvaluator;

    private ItemEvaluator itemEvaluator;

    private FlashMapManager flashMapManager;

    private LocaleResolver localeResolver;

    /**
     * Instantiates a new abstract activity.
     * @param context the activity context
     */
    protected AbstractActivity(ActivityContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    @Override
    public Mode getMode() {
        return Mode.DEFAULT;
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
            pendingActivity = context.getCurrentActivity();
        }
        context.setCurrentActivity(this);
    }

    /**
     * Removes the current activity.
     */
    protected void removeCurrentActivity() {
        if (pendingActivity != null) {
            context.setCurrentActivity(pendingActivity);
            pendingActivity = null;
        } else {
            context.removeCurrentActivity();
        }
    }

    @SuppressWarnings("unchecked")
    protected <V extends Activity> V getPendingActivity() {
        return (V)pendingActivity;
    }

    protected boolean isOriginalActivity() {
        return (pendingActivity != null);
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return context.getApplicationAdapter();
    }

    @Override
    public boolean hasSessionAdapter() {
        return (sessionAdapter != null);
    }

    @Override
    public SessionAdapter getSessionAdapter() {
        if (sessionAdapter == null) {
            throw new IllegalStateException("Session adapter not set");
        }
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
            if (raisedException instanceof ActionExecutionException ||
                    raisedException instanceof AdviceException ||
                    raisedException instanceof ResponseException) {
                this.raisedException = ExceptionUtils.getCause(raisedException);
            } else {
                this.raisedException = raisedException;
            }
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

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getSetting(String name) {
        if (settings != null) {
            Object value = settings.get(name);
            if (value != null) {
                return (V)value;
            }
        }
        return null;
    }

    @Override
    public void putSetting(String name, Object value) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Setting name must not be null or empty");
        }
        if (settings == null) {
            settings = new LinkedHashMap<>();
        }
        settings.put(name, value);
    }

    @Override
    public boolean hasStringifyContext() {
        return (stringifyContext != null);
    }

    @Override
    public StringifyContext getStringifyContext() {
        if (stringifyContext == null) {
            stringifyContext = new ActivityStringifyContext(this);
        }
        return stringifyContext;
    }

    @Override
    public TemplateRenderer getTemplateRenderer() {
        return context.getTemplateRenderer();
    }

    @Override
    public TokenEvaluator getTokenEvaluator() {
        if (tokenEvaluator == null) {
            tokenEvaluator = new TokenEvaluation(this);
        }
        return tokenEvaluator;
    }

    @Override
    public ItemEvaluator getItemEvaluator() {
        if (itemEvaluator == null) {
            itemEvaluator = new ItemEvaluation(getTokenEvaluator());
        }
        return itemEvaluator;
    }

    @Override
    public FlashMapManager getFlashMapManager() {
        return flashMapManager;
    }

    protected void setFlashMapManager(FlashMapManager flashMapManager) {
        this.flashMapManager = flashMapManager;
    }

    @Override
    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    protected void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
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
