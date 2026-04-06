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
import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.StringifyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for {@link Activity} implementations.
 * <p>This class provides common functionality and manages the core components
 * and state required for processing a request. It handles the lifecycle of adapters
 * (request, response, session), manages exceptions, and provides access to various
 * evaluators and managers within the activity scope.
 * </p>
 */
public abstract class AbstractActivity implements Activity {

    private static final Logger logger = LoggerFactory.getLogger(AbstractActivity.class);

    /**
     * The owning activity context, providing access to environment, beans, adapters, etc.
     */
    private final ActivityContext context;

    /**
     * The previously current activity, to be restored after this activity completes.
     */
    private Activity pendingActivity;

    /**
     * The session adapter associated with this activity (optional).
     */
    private SessionAdapter sessionAdapter;

    /**
     * The request adapter associated with this activity (maybe initialized lazily).
     */
    private RequestAdapter requestAdapter;

    /**
     * The response adapter associated with this activity (maybe initialized lazily).
     */
    private ResponseAdapter responseAdapter;

    /**
     * The exception that was raised during processing, if any.
     */
    private Exception raisedException;

    /**
     * Arbitrary settings stored at the activity scope.
     */
    private Map<String, Object> settings;

    /**
     * Lazily initialized stringify context for logging/debugging purposes.
     */
    private StringifyContext stringifyContext;

    /**
     * Lazily initialized evaluator for token expressions.
     */
    private TokenEvaluator tokenEvaluator;

    /**
     * Lazily initialized evaluator for item expressions.
     */
    private ItemEvaluator itemEvaluator;

    /**
     * Manager for FlashMap to pass attributes across requests, optional.
     */
    private FlashMapManager flashMapManager;

    /**
     * Resolver for determining the current locale, optional.
     */
    private LocaleResolver localeResolver;

    /**
     * The stack of hints pushed onto the activity.
     */
    private ArrayStack<HintParameters> hintStack;

    /**
     * Creates a new AbstractActivity.
     * @param context the activity context
     * @throws IllegalArgumentException if the context is null
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
    public String getContextPath() {
        return null;
    }

    @Override
    public String getReverseContextPath() {
        return null;
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
     * Returns the current activity instance from the {@link ActivityContext}.
     * @return the current activity
     */
    protected Activity getCurrentActivity() {
        return context.getCurrentActivity();
    }

    /**
     * Saves the current activity in the {@link ActivityContext} and sets this activity as the current one.
     */
    protected void saveCurrentActivity() {
        if (context.hasCurrentActivity()) {
            pendingActivity = context.getCurrentActivity();
        }
        context.setCurrentActivity(this);
    }

    /**
     * Removes this activity as the current one and restores the previously saved activity (if any).
     */
    protected void removeCurrentActivity() {
        if (pendingActivity != null) {
            context.setCurrentActivity(pendingActivity);
            pendingActivity = null;
        } else {
            context.removeCurrentActivity();
        }
    }

    /**
     * Returns the pending (previous) activity that was saved before this activity became current.
     * @param <V> the activity subtype
     * @return the pending activity or {@code null} if none
     */
    @SuppressWarnings("unchecked")
    protected <V extends Activity> V getPendingActivity() {
        return (V)pendingActivity;
    }

    /**
     * Checks if this activity replaced a previously current one (i.e., has a pending activity).
     * @return {@code true} if there is a pending activity; {@code false} otherwise
     */
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
     * Sets the session adapter for this activity.
     * @param sessionAdapter the session adapter
     */
    protected void setSessionAdapter(SessionAdapter sessionAdapter) {
        this.sessionAdapter = sessionAdapter;
    }

    @Override
    public RequestAdapter getRequestAdapter() {
        return requestAdapter;
    }

    /**
     * Sets the request adapter for this activity.
     * @param requestAdapter the request adapter
     */
    protected void setRequestAdapter(RequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    @Override
    public ResponseAdapter getResponseAdapter() {
        return responseAdapter;
    }

    /**
     * Sets the response adapter for this activity.
     * @param responseAdapter the response adapter
     */
    protected void setResponseAdapter(ResponseAdapter responseAdapter) {
        this.responseAdapter = responseAdapter;
    }

    @Override
    public boolean isExceptionRaised() {
        return (raisedException != null);
    }

    @Override
    public Exception getRaisedException() {
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
    public void setRaisedException(Exception raisedException) {
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
    public HintParameters peekHint(String type) {
        if (hintStack != null && !hintStack.isEmpty()) {
            boolean barrier = false;
            // Search from top to bottom (most recently pushed to least recently pushed)
            for (int i = hintStack.size() - 1; i >= 0; i--) {
                HintParameters hint = hintStack.get(i);
                if (hint == null) {
                    barrier = true;
                    continue;
                }
                if (type.equals(hint.getType())) {
                    // If a frame boundary (barrier) has been encountered, only hints marked
                    // for propagation are allowed to pass through from upper frames.
                    // Hints within the current frame (before any barrier) are always returned.
                    if (!barrier || hint.isPropagated()) {
                        return hint;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int pushHint(HintParameters hint) {
        if (hint == null || hint.isEmpty()) {
            return 0;
        }
        if (hintStack == null) {
            hintStack = new ArrayStack<>();
        }
        hintStack.push(hint);
        if (logger.isDebugEnabled()) {
            logger.debug("{}", hint);
        }
        return 1;
    }

    @Override
    public int pushHint(List<HintParameters> hints) {
        if (hintStack == null) {
            hintStack = new ArrayStack<>();
        }
        hintStack.push(null);
        int pushedCount = 1;
        if (hints != null && !hints.isEmpty()) {
            for (HintParameters hint : hints) {
                pushedCount += pushHint(hint);
            }
        }
        return pushedCount;
    }


    @Override
    public void popHint() {
        if (hintStack != null && !hintStack.isEmpty()) {
            hintStack.pop();
        }
    }

    @Override
    public void popHint(int count) {
        if (hintStack != null && !hintStack.isEmpty()) {
            for (int i = 0; i < count; i++) {
                hintStack.pop();
                if (hintStack.isEmpty()) {
                    break;
                }
            }
        }
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
