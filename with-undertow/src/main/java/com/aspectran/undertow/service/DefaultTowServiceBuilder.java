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
 * <p>Created: 4/1/24</p>
 */
public class DefaultTowServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTowServiceBuilder.class);

    /**
     * Returns a new instance of {@code DefaultTowService}.
     * @param parentService the parent service
     * @return the instance of {@code DefaultTowService}
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
     * Returns a new instance of {@code DefaultTowService}.
     * @param parentService the parent service
     * @param aspectranConfig the aspectran configuration
     * @return the instance of {@code DefaultTowService}
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
