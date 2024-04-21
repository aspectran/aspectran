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
package com.aspectran.shell.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * <p>Created: 4/1/24</p>
 */
public class DefaultShellServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellServiceBuilder.class);

    /**
     * Returns a new instance of {@code DefaultShellService}.
     * @param aspectranConfig the aspectran configuration
     * @param console the {@code Console} instance
     * @return the instance of {@code DefaultShellService}
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
                shellService.getConsole().clearScreen();
                shellService.printGreetings();
                shellService.printHelp();
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
                        "to a value of greater than 0");
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
