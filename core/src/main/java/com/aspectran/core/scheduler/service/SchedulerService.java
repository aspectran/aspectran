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
 * The Interface SchedulerService.
 */
public interface SchedulerService extends ServiceLifeCycle {

    String SERVICE_DATA_KEY = "SERVICE";

    String JOB_RULE_DATA_KEY = "JOB_RULE";

    ActivityContext getActivityContext();

    int getStartDelaySeconds();

    boolean isWaitOnShutdown();

    String getLoggingGroup();

    void pauseAll();

    void resumeAll();

    void pause(String scheduleId) throws SchedulerServiceException;

    void resume(String scheduleId) throws SchedulerServiceException;

}
