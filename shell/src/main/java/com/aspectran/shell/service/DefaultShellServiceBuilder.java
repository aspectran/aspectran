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
package com.aspectran.shell.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A builder class for creating and configuring {@link DefaultShellService} instances.
 * <p>This class provides a static factory method to construct a shell service,
 * applying configuration from an {@link AspectranConfig} object and linking it
 * with a {@link ShellConsole}. It also sets up a {@link ServiceStateListener} to
 * manage the service's lifecycle, including session management, console initialization,
 * and registration with the {@link CoreServiceHolder}.
 */
public class DefaultShellServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellServiceBuilder.class);

    /**
     * Builds a new {@link DefaultShellService} instance.
     * @param aspectranConfig the Aspectran configuration
     * @param console the shell console for I/O
     * @return a new, configured {@code DefaultShellService} instance
     */
    @NonNull
    public static DefaultShellService build(AspectranConfig aspectranConfig, ShellConsole console) {
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        DefaultShellService shellService = new DefaultShellService(console);
        shellService.configure(aspectranConfig);
        setServiceStateListener(shellService);
        return shellService;
    }

    private static void setServiceStateListener(@NonNull final DefaultShellService shellService) {
        shellService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                CoreServiceHolder.hold(shellService);
                shellService.createSessionManager();
                shellService.pauseTimeout = 0L;
                shellService.printGreetings();
                shellService.printDescription();
            }

            @Override
            public void stopped() {
                shellService.destroySessionManager();
                CoreServiceHolder.release(shellService);
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    shellService.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0.");
                }
            }

            @Override
            public void paused() {
                shellService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                shellService.pauseTimeout = 0L;
            }
        });
    }

}
