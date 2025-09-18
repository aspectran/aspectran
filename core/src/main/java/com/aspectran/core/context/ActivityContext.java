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
package com.aspectran.core.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.bean.async.AsyncTaskExecutor;
import com.aspectran.core.component.bean.event.EventPublisher;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.utils.statistic.CounterStatistic;

import java.nio.charset.StandardCharsets;

/**
 * Central interface to provide configuration for an application. This is the heart of the Aspectran framework.
 *
 * <p>It is responsible for holding and managing all the Aspectran components, such as
 * rule registries, the environment, and the application adapter. It also manages the
 * lifecycle of activities, which represent single units of work (e.g., handling a web request).
 *
 * <p>The {@code ActivityContext} is thread-safe and provides a way to access the
 * current activity via a {@link ThreadLocal} mechanism, making it suitable for
 * multi-threaded environments.
 *
 * @since 2008. 06. 09
 */
public interface ActivityContext {

    /** The separator for joining IDs. */
    String ID_SEPARATOR = ".";

    /** The separator for joining IDs. */
    char ID_SEPARATOR_CHAR = '.';

    /** The separator for joining names. */
    String NAME_SEPARATOR = "/";

    /** The separator for joining names. */
    char NAME_SEPARATOR_CHAR = '/';

    /** The default character encoding. */
    String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    /** The bean ID for the message source. */
    String MESSAGE_SOURCE_BEAN_ID = "messageSource";

    /**
     * Returns the name of this context.
     * @return the name of this context, or {@code null} if it is not named
     */
    String getName();

    /**
     * Returns a description of this context.
     * @return a description of this context, or {@code null} if no description is available
     */
    String getDescription();

    /**
     * Returns the master service that holds this context.
     * @return the master {@link CoreService} instance
     */
    CoreService getMasterService();

    /**
     * Returns the application adapter for the current environment.
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Returns the class loader for this context.
     * @return the class loader
     */
    ClassLoader getClassLoader();

    /**
     * Returns the environment for this context.
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Returns the aspect rule registry.
     * @return the aspect rule registry
     */
    AspectRuleRegistry getAspectRuleRegistry();

    /**
     * Returns the bean registry.
     * @return the bean registry
     */
    BeanRegistry getBeanRegistry();

    /**
     * Returns the schedule rule registry.
     * @return the schedule rule registry
     */
    ScheduleRuleRegistry getScheduleRuleRegistry();

    /**
     * Returns the template renderer.
     * @return the template renderer
     */
    TemplateRenderer getTemplateRenderer();

    /**
     * Returns the translet rule registry.
     * @return the translet rule registry
     */
    TransletRuleRegistry getTransletRuleRegistry();

    /**
     * Returns the message source for internationalization (i18n).
     * @return the message source
     */
    MessageSource getMessageSource();

    /**
     * Returns the event publisher.
     * @return the event publisher
     */
    EventPublisher getEventPublisher();

    /**
     * Returns the asynchronous task executor.
     * @return the asynchronous task executor
     * @throws AsyncTaskExecutorNotAvailableException if the async feature is not enabled
     */
    AsyncTaskExecutor getAsyncTaskExecutor();

    /**
     * Returns the default activity for this context.
     * @return the default activity
     */
    Activity getDefaultActivity();

    /**
     * Returns the currently available activity.
     * <p>If there is a current activity for the thread, it is returned.
     * Otherwise, the default activity is returned.</p>
     * @return the available activity
     */
    Activity getAvailableActivity();

    /**
     * Returns the current activity for the current thread.
     * @return the current activity
     * @throws NoActivityStateException if no activity is currently associated with the thread
     */
    Activity getCurrentActivity();

    /**
     * Sets the current activity for the current thread.
     * @param activity the activity to set as current
     */
    void setCurrentActivity(Activity activity);

    /**
     * Removes the current activity for the current thread.
     */
    void removeCurrentActivity();

    /**
     * Returns whether a current activity exists for the current thread.
     * @return {@code true} if a current activity exists, {@code false} otherwise
     */
    boolean hasCurrentActivity();

    /**
     * Returns the statistics counter for activities.
     * @return the activity counter
     */
    CounterStatistic getActivityCounter();

}
