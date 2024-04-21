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
package com.aspectran.daemon.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * <p>Created: 4/1/24</p>
 */
public abstract class DefaultDaemonServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDaemonServiceBuilder.class);

    /**
     * Returns a new instance of {@code DefaultDaemonService}.
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of {@code DefaultDaemonService}
     */
    @NonNull
    public static DefaultDaemonService build(AspectranConfig aspectranConfig) {
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        DefaultDaemonService daemonService = new DefaultDaemonService();
        daemonService.configure(aspectranConfig);
        setServiceStateListener(daemonService);
        return daemonService;
    }

    private static void setServiceStateListener(@NonNull final DefaultDaemonService daemonService) {
        daemonService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                CoreServiceHolder.hold(daemonService);
                daemonService.createSessionManager();
                daemonService.pauseTimeout = 0L;
            }

            @Override
            public void stopped() {
                daemonService.destroySessionManager();
                CoreServiceHolder.release(daemonService);
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    daemonService.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                        "to a value of greater than 0");
                }
            }

            @Override
            public void paused() {
                daemonService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                daemonService.pauseTimeout = 0L;
            }
        });
    }

}
