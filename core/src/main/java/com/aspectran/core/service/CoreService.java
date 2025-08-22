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
package com.aspectran.core.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.FlashMapManager;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.core.support.i18n.locale.LocaleResolver;

/**
 * The central interface for an Aspectran service.
 * <p>This interface defines the core functionalities and access points for an
 * Aspectran application instance. It extends the concept of a service beyond
 * just lifecycle management, providing access to the {@link ActivityContext},
 * configuration, and various other core components.
 *
 * <p>Implementations of this interface serve as the main entry point for
 * interacting with a running Aspectran instance in various environments
 * (e.g., web, daemon, shell, embedded).
 */
public interface CoreService {

    /**
     * Returns the root service in the service hierarchy.
     * @return the root service
     */
    CoreService getRootService();

    /**
     * Returns the parent service in the service hierarchy.
     * @return the parent service, or {@code null} if this is the root service
     */
    CoreService getParentService();

    /**
     * Returns whether this service is the root service in the hierarchy.
     * @return {@code true} if this is the root service, {@code false} otherwise
     */
    boolean isRootService();

    /**
     * Returns whether the service should be started separately
     * after the root service has started.
     * @return true if the service should start separately; false otherwise
     */
    boolean isOrphan();

    /**
     * Returns whether this service is derived from a parent service.
     * A derived service typically reuses the {@link ActivityContext} of its parent.
     * @return {@code true} if this service is derived, {@code false} otherwise
     */
    boolean isDerived();

    /**
     * Returns the service life cycle manager for this service.
     * @return the service life cycle
     */
    ServiceLifeCycle getServiceLifeCycle();

    /**
     * Returns the base path where the root application is running.
     * @return the base path for the root application
     */
    String getBasePath();

    /**
     * Returns the name of this service's context.
     * @return the context name
     */
    String getContextName();

    /**
     * Returns the Aspectran configuration parameters used to
     * generate this service.
     * @return the Aspectran Configuration Parameters
     */
    AspectranConfig getAspectranConfig();

    /**
     * Returns the application adapter, which provides an abstraction over the
     * underlying application environment (e.g., Servlet container).
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Returns the {@link ActivityContext} associated with this service.
     * The ActivityContext is the central container for all application components.
     * @return the activity context
     */
    ActivityContext getActivityContext();

    /**
     * Returns whether a service-specific class loader is available.
     * @return {@code true} if a service class loader is available, {@code false} otherwise
     */
    boolean hasServiceClassLoader();

    /**
     * Returns the ClassLoader used within this service.
     * @return the ClassLoader used within the service
     */
    ClassLoader getServiceClassLoader();

    /**
     * Returns an alternative class loader, typically used for hot-reloading or isolation.
     * @return the alternative class loader
     */
    ClassLoader getAltClassLoader();

    /**
     * Returns the default activity associated with this service.
     * @return the default activity
     */
    Activity getDefaultActivity();

    /**
     * Returns the scheduler service associated with this core service.
     * @return the scheduler service
     */
    SchedulerService getSchedulerService();

    /**
     * Returns whether the given request name is acceptable for processing by this service.
     * This is typically used for filtering requests based on configured patterns.
     * @param requestName the name of the request to check
     * @return {@code true} if the request can be processed by this service; {@code false} otherwise
     */
    boolean isRequestAcceptable(String requestName);

    /**
     * Returns the FlashMap manager for this service.
     * @return the FlashMap manager
     */
    FlashMapManager getFlashMapManager();

    /**
     * Returns the LocaleResolver for this service, used for internationalization.
     * @return the LocaleResolver
     */
    LocaleResolver getLocaleResolver();

}
