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
package com.aspectran.daemon.command.polling;

import com.aspectran.core.context.config.DaemonPollingConfig;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.service.DaemonService;

/**
 * Abstract base class for {@link FileCommander} implementations, providing common
 * state and behavior.
 * <p>
 * This class manages a reference to the owning {@link DaemonService}, the polling
 * interval, and the re-queueability of commands based on the provided
 * {@link DaemonPollingConfig}.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public abstract class AbstractFileCommander implements FileCommander {

    private static final long DEFAULT_POLLING_INTERVAL = 5000L;

    private final DaemonService daemonService;

    private volatile long pollingInterval;

    private final boolean requeuable;

    /**
     * Instantiates a new abstract file commander.
     * @param daemonService the daemon service that owns this commander
     * @param pollingConfig the polling configuration
     */
    public AbstractFileCommander(DaemonService daemonService, DaemonPollingConfig pollingConfig) {
        if (daemonService == null) {
            throw new IllegalArgumentException("daemonService must not be null");
        }

        this.daemonService = daemonService;
        this.pollingInterval = pollingConfig.getPollingInterval(DEFAULT_POLLING_INTERVAL);
        this.requeuable = pollingConfig.isRequeuable();
    }

    @Override
    public DaemonService getDaemonService() {
        return daemonService;
    }

    @Override
    public CommandExecutor getCommandExecutor() {
        return daemonService.getCommandExecutor();
    }

    @Override
    public long getPollingInterval() {
        return pollingInterval;
    }

    @Override
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    @Override
    public boolean isRequeuable() {
        return requeuable;
    }

}
