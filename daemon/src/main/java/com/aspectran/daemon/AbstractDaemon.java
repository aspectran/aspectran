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
package com.aspectran.daemon;

import com.aspectran.core.AboutMe;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.polling.FileCommander;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.daemon.service.DefaultDaemonService;
import com.aspectran.daemon.service.DefaultDaemonServiceBuilder;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.AponReader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;

/**
 * Base implementation of the {@link Daemon} contract.
 * <p>
 * This class wraps a {@link DaemonService} and provides high-level lifecycle
 * control (start/stop/destroy). Most of the actual command handling and
 * polling logic is managed by the underlying {@link DaemonService}.
 * Subclasses may extend behavior but typically use this as-is via
 * {@link DefaultDaemon} or platform wrappers (e.g., {@link JsvcDaemon}, {@link ProcrunDaemon}).
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 * @since 5.1.0
 */
public class AbstractDaemon implements Daemon {

    private String name;

    private DefaultDaemonService daemonService;

    private boolean waiting;

    private volatile boolean active;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getBasePath() {
        return (daemonService != null ? daemonService.getBasePath() : null);
    }

    @Override
    public DaemonService getDaemonService() {
        return daemonService;
    }

    @Override
    public CommandExecutor getCommandExecutor() {
        return (daemonService != null ? daemonService.getCommandExecutor() : null);
    }

    @Override
    public FileCommander getFileCommander() {
        return (daemonService != null ? daemonService.getFileCommander() : null);
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return (daemonService != null ? daemonService.getCommandRegistry() : null);
    }

    @Override
    public boolean isWaiting() {
        return waiting;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    protected void prepare(@Nullable String basePath, @Nullable File aspectranConfigFile) throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        if (aspectranConfigFile != null) {
            try {
                AponReader.read(aspectranConfigFile, aspectranConfig);
            } catch (AponParseException e) {
                throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                    aspectranConfigFile, e);
            }
        }
        prepare(basePath, aspectranConfig);
    }

    protected void prepare(@Nullable String basePath, @NonNull AspectranConfig aspectranConfig) throws Exception {
        if (StringUtils.hasText(basePath)) {
            aspectranConfig.touchContextConfig().setBasePath(basePath);
        }

        AboutMe.printPretty(System.out);
        startDaemonService(aspectranConfig);
    }

    private void startDaemonService(AspectranConfig aspectranConfig) throws Exception {
        try {
            this.daemonService = DefaultDaemonServiceBuilder.build(aspectranConfig);
            this.daemonService.setDaemon(this);
        } catch (Exception e) {
            throw new Exception("Failed to start daemon service", e);
        }
    }

    protected void start() throws Exception {
        start(false);
    }

    protected void start(boolean wait) throws Exception {
        if (wait) {
            start(0L);
        } else {
            start(-1L);
        }
    }

    protected void start(long waitTimeoutMillis) throws Exception {
        if (!active) {
            if (daemonService != null) {
                daemonService.start();
            }

            active = true;
            waiting = (waitTimeoutMillis >= 0L);

            if (waitTimeoutMillis >= 0L) {
                if (name == null) {
                    name = getClass().getSimpleName();
                }
                Thread thread = new Thread(() -> {
                    while (active) {
                        try {
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException ie) {
                            active = false;
                        }
                    }
                }, name);
                thread.start();
                thread.join(waitTimeoutMillis);
            }
        }
    }

    @Override
    public void stop() {
        if (active) {
            active = false;
            if (daemonService != null && daemonService.isActive()) {
                daemonService.stop();
            }
        }
    }

    @Override
    public void destroy() {
        stop();

        if (daemonService != null) {
            daemonService.withdraw();
            daemonService = null;
        }
    }

}
