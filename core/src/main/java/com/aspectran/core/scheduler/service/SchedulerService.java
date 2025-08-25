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
 * <p>This service defines the contract for managing the lifecycle of scheduled jobs.
 * Concrete implementations of this service act as a bridge to an underlying scheduling
 * engine (e.g., Quartz), allowing Aspectran translets to be executed at specified
 * times or intervals.</p>
 *
 * <p>The SchedulerService is a sub-service of the {@link com.aspectran.core.service.CoreService}
 * and its lifecycle is managed by it.</p>
 *
 * @since 3.0.0
 */
public interface SchedulerService extends ServiceLifeCycle {

    /**
     * The key for storing a reference to this service in the job data map.
     */
    String SERVICE_DATA_KEY = "SERVICE";

    /**
     * The key for storing the {@link com.aspectran.core.context.rule.ScheduledJobRule}
     * in the job data map, allowing the job to know which translet to execute.
     */
    String JOB_RULE_DATA_KEY = "JOB_RULE";

    /**
     * Returns the main ActivityContext, providing access to the application's core components.
     * @return the activity context
     */
    ActivityContext getActivityContext();

    /**
     * Returns the number of seconds to wait before starting the scheduler after the service is initialized.
     * @return the start delay in seconds
     */
    int getStartDelaySeconds();

    /**
     * Returns whether the scheduler should wait for running jobs to complete on shutdown.
     * @return true to wait, false otherwise
     */
    boolean isWaitOnShutdown();

    /**
     * Returns the name of the logging group for this service.
     * @return the logging group name
     */
    String getLoggingGroup();

    /**
     * Pauses all job executions in the scheduler.
     */
    void pauseAll();

    /**
     * Resumes all paused jobs in the scheduler.
     */
    void resumeAll();

    /**
     * Pauses all jobs associated with the specified schedule rule.
     * @param scheduleId the ID of the schedule to pause
     * @throws SchedulerServiceException if the schedule cannot be paused
     */
    void pause(String scheduleId) throws SchedulerServiceException;

    /**
     * Resumes all jobs associated with the specified schedule rule.
     * @param scheduleId the ID of the schedule to resume
     * @throws SchedulerServiceException if the schedule cannot be resumed
     */
    void resume(String scheduleId) throws SchedulerServiceException;

}
