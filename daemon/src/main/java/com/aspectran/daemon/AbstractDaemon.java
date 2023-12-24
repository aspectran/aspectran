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
import com.aspectran.core.context.config.DaemonExecutorConfig;
import com.aspectran.core.context.config.DaemonPollingConfig;
import com.aspectran.core.util.Aspectran;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.DaemonCommandRegistry;
import com.aspectran.daemon.command.builtins.QuitCommand;
import com.aspectran.daemon.command.polling.DefaultFileCommander;
import com.aspectran.daemon.command.polling.FileCommander;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.daemon.service.DefaultDaemonService;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.AponReader;

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

    private CommandExecutor commandExecutor;

    private FileCommander fileCommander;

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
    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    @Override
    public FileCommander getFileCommander() {
        return fileCommander;
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

    protected void init(@Nullable String basePath, @Nullable File aspectranConfigFile) throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        if (aspectranConfigFile != null) {
            try {
                AponReader.parse(aspectranConfigFile, aspectranConfig);
            } catch (AponParseException e) {
                throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                        aspectranConfigFile, e);
            }
        }
        init(basePath, aspectranConfig);
    }

    protected void init(@Nullable String basePath, @NonNull AspectranConfig aspectranConfig) throws Exception {
        if (basePath != null) {
            aspectranConfig.touchContextConfig().setBasePath(basePath);
        }

        startDaemonService(aspectranConfig);
        initDaemon(aspectranConfig.touchDaemonConfig());
    }

    private void initDaemon(DaemonConfig daemonConfig) throws Exception {
        Aspectran.printPrettyAboutMe(System.out);

        try {
            DaemonExecutorConfig executorConfig = daemonConfig.touchExecutorConfig();
            this.commandExecutor = new CommandExecutor(this, executorConfig);

            DaemonPollingConfig pollingConfig = daemonConfig.touchPollingConfig();
            this.fileCommander = new DefaultFileCommander(this, pollingConfig);

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
                    if (fileCommander != null) {
                        fileCommander.requeue();
                        while (active) {
                            try {
                                fileCommander.polling();
                                Thread.sleep(fileCommander.getPollingInterval());
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

        if (commandExecutor != null) {
            commandExecutor.shutdown();
        }
        if (fileCommander != null) {
            fileCommander = null;
        }
        if (daemonService != null) {
            daemonService.stop();
            daemonService = null;
        }
    }

}
