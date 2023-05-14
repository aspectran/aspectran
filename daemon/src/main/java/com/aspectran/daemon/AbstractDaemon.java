/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.util.Aspectran;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.DaemonCommandRegistry;
import com.aspectran.daemon.command.builtins.QuitCommand;
import com.aspectran.daemon.command.file.CommandFilePoller;
import com.aspectran.daemon.command.file.DefaultCommandFilePoller;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.daemon.service.DefaultDaemonService;

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

    private DefaultDaemonService daemonService;

    private CommandFilePoller commandFilePoller;

    private CommandRegistry commandRegistry;

    private boolean wait;

    private Thread thread;

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
    public CommandFilePoller getCommandFilePoller() {
        return commandFilePoller;
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

        startDaemonService(aspectranConfig);
        init(aspectranConfig.touchDaemonConfig());
    }

    protected void init(DaemonConfig daemonConfig) throws Exception {
        Aspectran.printPrettyAboutMe(System.out);

        try {
            DaemonPollerConfig pollerConfig = daemonConfig.touchPollerConfig();
            this.commandFilePoller = new DefaultCommandFilePoller(this, pollerConfig);

            DaemonCommandRegistry commandRegistry = new DaemonCommandRegistry(this);
            commandRegistry.addCommand(daemonConfig.getCommands());
            if (commandRegistry.getCommand(QuitCommand.class) == null) {
                commandRegistry.addCommand(QuitCommand.class);
            }
            this.commandRegistry = commandRegistry;
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon", e);
        }
    }

    private void startDaemonService(AspectranConfig aspectranConfig) throws Exception {
        try {
            this.daemonService = DefaultDaemonService.create(aspectranConfig);
            this.daemonService.start();
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

    protected void start(long wait) throws Exception {
        if (!active) {
            this.wait = (wait >= 0L);
            if (name == null) {
                name = this.getClass().getSimpleName();
            }

            Runnable runnable = () -> {
                if (!active) {
                    active = true;
                    if (commandFilePoller != null) {
                        commandFilePoller.requeue();
                        while (active) {
                            try {
                                commandFilePoller.polling();
                                Thread.sleep(commandFilePoller.getPollingInterval());
                            } catch (InterruptedException ie) {
                                active = false;
                            }
                        }
                    }
                }
            };

            thread = new Thread(runnable, name);
            thread.start();
            if (wait >= 0L) {
                thread.join(wait);
            }
        }
    }

    @Override
    public void stop() {
        if (active) {
            active = false;
            if (thread != null) {
                if (thread.getState() == Thread.State.TIMED_WAITING) {
                    thread.interrupt();
                }
                thread = null;
            }
        }
    }

    @Override
    public void destroy() {
        stop();

        if (commandFilePoller != null) {
            commandFilePoller.stop();
            commandFilePoller = null;
        }
        if (daemonService != null) {
            daemonService.stop();
            daemonService = null;
        }
    }

}
