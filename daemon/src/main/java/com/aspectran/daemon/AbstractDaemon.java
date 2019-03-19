/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.context.config.DaemonPollerConfig;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.DaemonCommandRegistry;
import com.aspectran.daemon.command.builtins.QuitCommand;
import com.aspectran.daemon.command.polling.CommandPoller;
import com.aspectran.daemon.command.polling.FileCommandPoller;
import com.aspectran.daemon.service.AspectranDaemonService;
import com.aspectran.daemon.service.DaemonService;

import java.io.File;

/**
 * The Abstract Daemon.
 *
 * <p>Created: 2017. 12. 11.</p>
 *
 * @since 5.1.0
 */
public class AbstractDaemon implements Daemon {

    private String name;

    private AspectranDaemonService service;

    private CommandPoller commandPoller;

    private CommandRegistry commandRegistry;

    private boolean wait;

    private Thread pollingThread;

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
    public DaemonService getService() {
        return service;
    }

    @Override
    public CommandPoller getCommandPoller() {
        return commandPoller;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public boolean isWait() {
        return wait;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    protected void init(String basePath, File aspectranConfigFile) throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        try {
            AponReader.parse(aspectranConfigFile, aspectranConfig);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                    aspectranConfigFile, e);
        }
        if (basePath != null) {
            aspectranConfig.touchContextConfig().setBasePath(basePath);
        }

        init(aspectranConfig);
    }

    protected void init(AspectranConfig aspectranConfig) throws Exception {
        try {
            this.service = AspectranDaemonService.create(aspectranConfig);
            this.service.start();
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon", e);
        }

        init(aspectranConfig.touchDaemonConfig());
    }

    protected void init(DaemonConfig daemonConfig) throws Exception {
        try {
            DaemonPollerConfig pollerConfig = daemonConfig.touchDaemonPollerConfig();

            this.commandPoller = new FileCommandPoller(this, pollerConfig);

            DaemonCommandRegistry commandRegistry = new DaemonCommandRegistry(this);
            commandRegistry.addCommand(daemonConfig.getStringArray(DaemonConfig.commands));
            if (commandRegistry.getCommand(QuitCommand.class) == null) {
                commandRegistry.addCommand(QuitCommand.class);
            }
            this.commandRegistry = commandRegistry;
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon", e);
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

    protected void start(long wait) throws Exception {
        if (!active) {
            this.wait = (wait >= 0L);
            if (name == null) {
                name = this.getClass().getSimpleName();
            }

            Runnable runnable = () -> {
                if (!active) {
                    active = true;
                    getCommandPoller().requeue();
                    while (active) {
                        try {
                            getCommandPoller().polling();
                            Thread.sleep(getCommandPoller().getPollingInterval());
                        } catch (InterruptedException ie) {
                            active = false;
                        }
                    }
                }
            };

            pollingThread = new Thread(runnable, name);
            pollingThread.start();
            if (wait >= 0L) {
                pollingThread.join(wait);
            }
        }
    }

    @Override
    public void stop() {
        if (active) {
            active = false;
            if (pollingThread != null) {
                if (pollingThread.getState() == Thread.State.TIMED_WAITING) {
                    pollingThread.interrupt();
                }
                pollingThread = null;
            }
        }
    }

    @Override
    public void destroy() {
        stop();

        if (commandPoller != null) {
            commandPoller.stop();
            commandPoller = null;
        }
        if (service != null) {
            service.stop();
            service = null;
        }
    }

}
