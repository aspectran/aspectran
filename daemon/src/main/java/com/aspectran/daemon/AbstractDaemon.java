/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.daemon.command.polling.CommandPoller;
import com.aspectran.daemon.command.polling.FileCommandPoller;
import com.aspectran.daemon.service.DaemonService;

import java.io.File;

/**
 * <p>Created: 2017. 12. 11.</p>
 *
 * @since 5.1.0
 */
public class AbstractDaemon implements Daemon, Runnable {

    private String name;

    private DaemonService service;

    private CommandPoller commandPoller;

    private CommandRegistry commandRegistry;

    private boolean active;

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
    public boolean isActive() {
        return active;
    }

    protected void init(File aspectranConfigFile) throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        try {
            AponReader.parse(aspectranConfigFile, aspectranConfig);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                    aspectranConfigFile, e);
        }

        init(aspectranConfig);
    }

    protected void init(AspectranConfig aspectranConfig) throws Exception {
        try {
            this.service = DaemonService.create(aspectranConfig);
            this.service.start();

            DaemonConfig daemonConfig = aspectranConfig.touchDaemonConfig();
            DaemonPollerConfig pollerConfig = daemonConfig.touchDaemonPollerConfig();

            CommandPoller commandPoller = new FileCommandPoller(this, pollerConfig);

            CommandRegistry commandRegistry = new CommandRegistry(this);
            commandRegistry.addCommand(daemonConfig.getStringArray(DaemonConfig.commands));

            this.commandPoller = commandPoller;
            this.commandRegistry = commandRegistry;
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon", e);
        }
    }

    protected void start() throws Exception {
        start(false);
    }

    protected void start(boolean wait) throws Exception {
        if (!active) {
            if (name == null) {
                name = this.getClass().getSimpleName();
            }
            Thread thread = new Thread(this, name);
            thread.start();
            if (wait) {
                thread.join();
            }
        }
    }

    @Override
    public void run() {
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
    }

    @Override
    public void stop() {
        if (active) {
            active = false;
            Thread.currentThread().interrupt();
        }
    }

    protected void destroy() {
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
