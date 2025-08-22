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
package com.aspectran.undertow.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A builder class for creating and configuring {@link DefaultTowService} instances.
 * <p>This class provides static factory methods to construct an Undertow service,
 * applying configuration from an {@link AspectranConfig} object and linking it
 * to a parent {@link CoreService}. It also sets up a {@link ServiceStateListener}
 * to manage the service's lifecycle, including pause/resume state.
 *
 * @since 2024-04-01
 */
public class DefaultTowServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTowServiceBuilder.class);

    /**
     * Builds a new {@link DefaultTowService} instance for the given parent service.
     * @param parentService the parent service
     * @return a new, configured {@code DefaultTowService} instance
     */
    @NonNull
    public static DefaultTowService build(CoreService parentService) {
        Assert.notNull(parentService, "parentService must not be null");
        DefaultTowService towService = new DefaultTowService(parentService, true);
        AspectranConfig aspectranConfig = parentService.getAspectranConfig();
        if (aspectranConfig != null) {
            WebConfig webConfig = aspectranConfig.getWebConfig();
            if (webConfig != null) {
                towService.configure(webConfig);
            }
        }
        setServiceStateListener(towService);
        return towService;
    }

    /**
     * Builds a new {@link DefaultTowService} instance for the given parent service and Aspectran configuration.
     * @param parentService the parent service
     * @param aspectranConfig the Aspectran configuration
     * @return a new, configured {@code DefaultTowService} instance
     */
    @NonNull
    public static DefaultTowService build(CoreService parentService, AspectranConfig aspectranConfig) {
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        DefaultTowService towService = new DefaultTowService(parentService, false);
        towService.configure(aspectranConfig);
        setServiceStateListener(towService);
        return towService;
    }

    private static void setServiceStateListener(@NonNull final DefaultTowService towService) {
        towService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                towService.pauseTimeout = 0L;
            }

            @Override
            public void stopped() {
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    towService.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0");
                }
            }

            @Override
            public void paused() {
                towService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                towService.pauseTimeout = 0L;
            }
        });
    }

}
