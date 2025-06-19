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
import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.command.CommandExecutor;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public abstract class AbstractFileCommander implements FileCommander {

    private static final long DEFAULT_POLLING_INTERVAL = 5000L;

    private final Daemon daemon;

    private volatile long pollingInterval;

    private final boolean requeuable;

    public AbstractFileCommander(Daemon daemon, DaemonPollingConfig pollingConfig) {
        if (daemon == null) {
            throw new IllegalArgumentException("daemon must not be null");
        }

        this.daemon = daemon;
        this.pollingInterval = pollingConfig.getPollingInterval(DEFAULT_POLLING_INTERVAL);
        this.requeuable = pollingConfig.isRequeuable();
    }

    @Override
    public Daemon getDaemon() {
        return daemon;
    }

    @Override
    public CommandExecutor getCommandExecutor() {
        return daemon.getCommandExecutor();
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
