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
package com.aspectran.core.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.scheduler.service.SchedulerService;

/**
 * The Interface CoreService.
 */
public interface CoreService {

    /**
     * Returns the base path where the root application is running.
     * @return the base path for the root application
     */
    String getBasePath();

    /**
     * Returns whether the service should be started separately
     * late after the root service is started.
     * @return true if the service should start separately late; false otherwise
     */
    boolean isLateStart();

    /**
     * Returns whether to reload all Java classes, resources,
     * and activity context configurations.
     * @return false if only the activity context configuration
     *      is reloaded; true if all are reloaded
     */
    boolean isHardReload();

    /**
     * Returns the service controller for this service.
     * @return the service controller
     */
    ServiceController getServiceController();

    /**
     * Add a derived core service.
     * Derived services follow the life cycle of the root service.
     * @param coreService the core service
     */
    void joinDerivedService(CoreService coreService);

    void withdrawDerivedService(CoreService coreService);

    void leaveFromRootService();

    /**
     * Returns whether this service is derived from another root service.
     * @return whether this service is derived
     */
    boolean isDerived();

    /**
     * Returns the Aspectran configuration parameters used to
     * generate the AspectranService.
     * @return the Aspectran Configuration Parameters
     */
    AspectranConfig getAspectranConfig();

    /**
     * @return the activity context
     */
    ActivityContext getActivityContext();

    /**
     * @return the ClassLoader used within the service.
     */
    ClassLoader getServiceClassLoader();

    /**
     * @return the default activity
     */
    Activity getDefaultActivity();

    /**
     * @return the scheduler service
     */
    SchedulerService getSchedulerService();

}
