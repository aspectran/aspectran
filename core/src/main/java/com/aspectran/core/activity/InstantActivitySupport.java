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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Convenience base class that exposes the current {@link ActivityContext}
 * and provides helpers to execute short-lived, programmatic activities.
 * <p>
 * Subclasses typically get injected with the {@link ActivityContext} by the container
 * and can then call convenience methods like {@link #instantActivity(InstantAction)}
 * or {@link #instantActivity(String, MethodType)} to run work within a proper
 * activity/transactional boundary without having to manage the plumbing.
 * </p>
 *
 * <p>Created: 29/09/2019</p>
 */
public abstract class InstantActivitySupport implements ActivityContextAware {

    private ActivityContext context;

    /**
     * Returns the injected {@link ActivityContext}.
     * @return the current activity context (never {@code null} once injected)
     * @throws IllegalStateException if the context has not been injected yet
     */
    @NonNull
    protected ActivityContext getActivityContext() {
        Assert.state(context != null, "No ActivityContext injected");
        return context;
    }

    /**
     * Injects the {@link ActivityContext}. Framework code calls this once during initialization.
     * @param context the activity context to use; must not be {@code null}
     * @throws IllegalStateException if the context has already been set
     */
    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        Assert.state(this.context == null, "ActivityContext already injected");
        this.context = context;
    }

    /**
     * Returns an activity that is available to run work right now, creating an
     * instant activity if necessary.
     * @return an available activity bound to the current thread/context
     */
    protected Activity getAvailableActivity() {
        return getActivityContext().getAvailableActivity();
    }

    /**
     * Returns the currently active activity, if any.
     * @return the current activity
     */
    protected Activity getCurrentActivity() {
        return getActivityContext().getCurrentActivity();
    }

    /**
     * Returns whether a current activity is associated with this thread/context.
     * @return {@code true} if a current activity exists; {@code false} otherwise
     */
    protected boolean hasCurrentActivity() {
        return getActivityContext().hasCurrentActivity();
    }

    /**
     * Shortcut to the {@link ApplicationAdapter} from the context.
     * @return the application adapter
     */
    protected ApplicationAdapter getApplicationAdapter() {
        return getActivityContext().getApplicationAdapter();
    }

    /**
     * Shortcut to the {@link Environment} from the context.
     * @return the environment
     */
    protected Environment getEnvironment() {
        return getActivityContext().getEnvironment();
    }

    /**
     * Shortcut to the {@link BeanRegistry} from the context.
     * @return the bean registry
     */
    protected BeanRegistry getBeanRegistry() {
        return getActivityContext().getBeanRegistry();
    }

    /**
     * Shortcut to the {@link TemplateRenderer} from the context.
     * @return the template renderer
     */
    protected TemplateRenderer getTemplateRenderer() {
        return getActivityContext().getTemplateRenderer();
    }

    /**
     * Shortcut to the {@link MessageSource} from the context.
     * @return the message source
     */
    protected MessageSource getMessageSource() {
        return getActivityContext().getMessageSource();
    }

    /**
     * Executes the given {@link InstantAction} within a freshly created instant activity
     * bound to the current {@link ActivityContext}.
     * @param <V> the result type of the action
     * @param instantAction the action to execute; must not be {@code null}
     * @return the action result
     * @throws IllegalArgumentException if {@code instantAction} is {@code null}
     * @throws InstantActivityException if any error occurs while preparing or performing the activity
     */
    protected <V> V instantActivity(InstantAction<V> instantAction) {
        if (instantAction == null) {
            throw new IllegalArgumentException("instantAction must not be null");
        }
        try {
            InstantActivity activity = new InstantActivity(getActivityContext());
            return activity.perform(instantAction);
        } catch (Exception e) {
            throw new InstantActivityException(e);
        }
    }

    /**
     * Performs a request identified by its name using the default request method ({@link MethodType#GET})
     * within an instant activity that inherits state from the current activity.
     * @param requestName the request name to execute; must not be blank
     * @throws IllegalArgumentException if {@code requestName} is {@code null} or empty
     * @throws IllegalStateException if there is no current {@link Translet}
     * @throws InstantActivityException if any error occurs while preparing or performing the activity
     */
    protected void instantActivity(String requestName) {
        instantActivity(requestName, MethodType.GET);
    }

    /**
     * Performs a request identified by its name and method within an instant activity that
     * inherits state (e.g., adapters, locale, etc.) from the current activity.
     * @param requestName the request name to execute; must not be blank
     * @param requestMethod the request method to use (e.g., GET or POST)
     * @throws IllegalArgumentException if {@code requestName} is {@code null} or empty
     * @throws IllegalStateException if there is no current {@link Translet}
     * @throws InstantActivityException if any error occurs while preparing or performing the activity
     */
    protected void instantActivity(String requestName, MethodType requestMethod) {
        if (StringUtils.isEmpty(requestName)) {
            throw new IllegalArgumentException("requestName must not be null or empty");
        }
        Activity currentActivity = getCurrentActivity();
        Translet translet = currentActivity.getTranslet();
        if (translet == null) {
            throw new IllegalStateException("No Translet in " + currentActivity);
        }
        try {
            InstantActivity activity = new InstantActivity(currentActivity);
            activity.prepare(requestName, requestMethod);
            activity.perform();
        } catch (Exception e) {
            throw new InstantActivityException(e);
        }
    }

}
