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
package com.aspectran.daemon.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A builder class for creating and configuring {@link DefaultDaemonService} instances.
 * <p>This class provides a static factory method to construct a daemon service,
 * applying configuration from an {@link AspectranConfig} object. It also sets up
 * a {@link ServiceStateListener} to manage the service's lifecycle, including
 * session management, registration with the {@link CoreServiceHolder}, and
 * pause/resume state.
 */
public abstract class DefaultDaemonServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDaemonServiceBuilder.class);

    /**
     * Builds a new {@link DefaultDaemonService} instance from the given configuration.
     * @param aspectranConfig the Aspectran configuration
     * @return a new, configured {@code DefaultDaemonService} instance
     */
    @NonNull
    public static DefaultDaemonService build(AspectranConfig aspectranConfig) {
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        DefaultDaemonService daemonService = new DefaultDaemonService();
        daemonService.configure(aspectranConfig);
        setServiceStateListener(daemonService);
        return daemonService;
    }

    /**
     * Sets up a {@link ServiceStateListener} to manage the daemon service's lifecycle events.
     * @param daemonService the daemon service to configure
     */
    private static void setServiceStateListener(@NonNull DefaultDaemonService daemonService) {
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
                            "to a value of greater than 0.");
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
