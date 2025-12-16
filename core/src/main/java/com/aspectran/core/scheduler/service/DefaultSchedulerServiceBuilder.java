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

import com.aspectran.core.context.config.SchedulerConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A builder class for creating and configuring {@link DefaultSchedulerService} instances.
 * <p>This class provides a static factory method to construct a scheduler service,
 * linking it to a parent {@link CoreService} and applying configuration from a
 * {@link SchedulerConfig} object. It also sets up a {@link ServiceStateListener}
 * to integrate the scheduler's pause/resume lifecycle with the main service.
 */
public class DefaultSchedulerServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSchedulerServiceBuilder.class);

    /**
     * Builds a new {@link DefaultSchedulerService}.
     * @param parentService the parent core service
     * @param schedulerConfig the scheduler configuration
     * @return a new, configured {@code DefaultSchedulerService} instance
     */
    @NonNull
    public static DefaultSchedulerService build(CoreService parentService, SchedulerConfig schedulerConfig) {
        Assert.notNull(parentService, "parentService must not be null");
        Assert.notNull(schedulerConfig, "schedulerConfig must not be null");
        DefaultSchedulerService schedulerService = new DefaultSchedulerService(parentService);
        schedulerService.configure(schedulerConfig);
        setServiceStateListener(schedulerService);
        return schedulerService;
    }

    private static void setServiceStateListener(@NonNull DefaultSchedulerService schedulerService) {
        schedulerService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
            }

            @Override
            public void stopped() {
            }

            @Override
            public void paused(long millis) {
                logger.warn("{} does not support pausing for a certain period of time.",
                        schedulerService.getServiceName());
            }

            @Override
            public void paused() {
                schedulerService.pauseAll();
            }

            @Override
            public void resumed() {
                schedulerService.resumeAll();
            }
        });
    }

}
