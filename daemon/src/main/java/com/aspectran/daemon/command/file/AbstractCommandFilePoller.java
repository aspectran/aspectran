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
package com.aspectran.daemon.command.file;

import com.aspectran.core.context.config.DaemonPollerConfig;
import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.command.CommandExecutor;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public abstract class AbstractCommandFilePoller implements CommandFilePoller {

    private static final long DEFAULT_POLLING_INTERVAL = 5000L;

    private static final int DEFAULT_MAX_THREADS = Runtime.getRuntime().availableProcessors();

    private final Daemon daemon;

    private final CommandExecutor executor;

    private volatile long pollingInterval;

    private final int maxThreads;

    private final boolean requeuable;

    public AbstractCommandFilePoller(Daemon daemon, DaemonPollerConfig pollerConfig) {
        if (daemon == null) {
            throw new IllegalArgumentException("daemon must not be null");
        }

        this.daemon = daemon;

        this.pollingInterval = pollerConfig.getPollingInterval(DEFAULT_POLLING_INTERVAL);
        this.maxThreads = pollerConfig.getMaxThreads(DEFAULT_MAX_THREADS);
        this.requeuable = pollerConfig.isRequeuable();

        this.executor = new CommandExecutor(daemon, maxThreads);
    }

    @Override
    public Daemon getDaemon() {
        return daemon;
    }

    @Override
    public CommandExecutor getExecutor() {
        return executor;
    }

    @Override
    public void stop() {
        executor.shutdown();
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
    public int getMaxThreads() {
        return maxThreads;
    }

    @Override
    public boolean isRequeuable() {
        return requeuable;
    }

}
