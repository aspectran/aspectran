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
 * Builder utility for creating and wiring a DefaultDaemonService instance.
 * <p>
 * This class centralizes the bootstrapping of the daemon-oriented CoreService including
 * configuration and lifecycle listeners. The {@link #build(AspectranConfig)} method:
 * </p>
 * <ul>
 *   <li>creates a new {@link DefaultDaemonService}</li>
 *   <li>applies the provided {@link AspectranConfig} via {@code configure(...)};</li>
 *   <li>registers a {@link ServiceStateListener} to manage global service exposure
 *       ({@link CoreServiceHolder}) and session manager lifecycle, and to control
 *       pause/resume semantics through the service's {@code pauseTimeout} flag</li>
 * </ul>
 *
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

    /**
     * Registers a {@link ServiceStateListener} on the given service to coordinate global exposure
     * and pause/resume semantics.
     * <p>Listener behavior:</p>
     * <ul>
     *   <li><b>started()</b>: exposes the service via {@link com.aspectran.core.service.CoreServiceHolder};
     *       creates the session manager; clears pause state ({@code pauseTimeout = 0}).</li>
     *   <li><b>stopped()</b>: destroys the session manager; removes the service from
     *       {@link com.aspectran.core.service.CoreServiceHolder}.</li>
     *   <li><b>paused(long)</b>: sets {@code pauseTimeout} to a future instant (now + millis) if the
     *       value is positive, otherwise logs a warning.</li>
     *   <li><b>paused()</b>: pauses indefinitely by setting {@code pauseTimeout = -1}.</li>
     *   <li><b>resumed()</b>: clears pause state by setting {@code pauseTimeout = 0}.</li>
     * </ul>
     * @param daemonService the service to attach the listener to (never {@code null})
     */
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
