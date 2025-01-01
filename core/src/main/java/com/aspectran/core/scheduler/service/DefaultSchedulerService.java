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
package com.aspectran.core.scheduler.service;

import com.aspectran.core.service.CoreService;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * The Class DefaultSchedulerService.
 */
public class DefaultSchedulerService extends AbstractSchedulerService {

    DefaultSchedulerService(CoreService parentService) {
        super(parentService);
    }

    @Override
    public void pauseAll() {
        synchronized (getLock()) {
            try {
                for (Scheduler scheduler : getSchedulers()) {
                    scheduler.pauseAll();
                }
            } catch (Exception e) {
                throw new SchedulerServiceException("Could not pause all schedulers", e);
            }
        }
    }

    @Override
    public void resumeAll() {
        synchronized (getLock()) {
            try {
                for (Scheduler scheduler : getSchedulers()) {
                    scheduler.resumeAll();
                }
            } catch (Exception e) {
                throw new SchedulerServiceException("Could not resume all schedulers", e);
            }
        }
    }

    @Override
    public void pause(String scheduleId) throws SchedulerServiceException {
        synchronized (getLock()) {
            try {
                Scheduler scheduler = getScheduler(scheduleId);
                if (scheduler != null && scheduler.isStarted()) {
                    scheduler.pauseJobs(GroupMatcher.jobGroupEquals(scheduleId));
                }
            } catch (Exception e) {
                throw new SchedulerServiceException("Could not pause scheduler '" + scheduleId + "'", e);
            }
        }
    }

    @Override
    public synchronized void resume(String scheduleId) throws SchedulerServiceException {
        synchronized (getLock()) {
            try {
                Scheduler scheduler = getScheduler(scheduleId);
                if (scheduler != null && scheduler.isStarted()) {
                    scheduler.resumeJobs(GroupMatcher.jobGroupEquals(scheduleId));
                }
            } catch (Exception e) {
                throw new SchedulerServiceException("Could not resume scheduler '" + scheduleId + "'", e);
            }
        }
    }

}
