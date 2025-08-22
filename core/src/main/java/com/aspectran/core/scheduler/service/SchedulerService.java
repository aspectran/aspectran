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
package com.aspectran.core.scheduler.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.ServiceLifeCycle;

/**
 * The main interface for the Aspectran scheduling service.
 * <p>This service is responsible for managing the lifecycle of scheduled jobs,
 * which are defined in the Aspectran configuration. It acts as a bridge to the
 * underlying Quartz scheduler, allowing Aspectran translets to be executed
 * at specified times or intervals.
 *
 * <p>The SchedulerService is a sub-service of the {@link com.aspectran.core.service.CoreService}
 * and its lifecycle is managed by it.
 */
public interface SchedulerService extends ServiceLifeCycle {

    String SERVICE_DATA_KEY = "SERVICE";

    String JOB_RULE_DATA_KEY = "JOB_RULE";

    /**
     * Returns the activity context.
     * @return the activity context
     */
    ActivityContext getActivityContext();

    /**
     * Returns the number of seconds to delay before starting the scheduler.
     * @return the start delay in seconds
     */
    int getStartDelaySeconds();

    /**
     * Returns whether to wait for jobs to complete on shutdown.
     * @return true to wait, false otherwise
     */
    boolean isWaitOnShutdown();

    /**
     * Returns the logging group name.
     * @return the logging group name
     */
    String getLoggingGroup();

    /**
     * Pauses all jobs in the scheduler.
     */
    void pauseAll();

    /**
     * Resumes all paused jobs in the scheduler.
     */
    void resumeAll();

    /**
     * Pauses the schedule with the given ID.
     * @param scheduleId the ID of the schedule to pause
     * @throws SchedulerServiceException if the schedule cannot be paused
     */
    void pause(String scheduleId) throws SchedulerServiceException;

    /**
     * Resumes the schedule with the given ID.
     * @param scheduleId the ID of the schedule to resume
     * @throws SchedulerServiceException if the schedule cannot be resumed
     */
    void resume(String scheduleId) throws SchedulerServiceException;

}
