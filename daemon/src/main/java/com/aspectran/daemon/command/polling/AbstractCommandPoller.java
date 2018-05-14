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
package com.aspectran.daemon.command.polling;

import com.aspectran.core.context.config.DaemonPollerConfig;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.daemon.Daemon;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public abstract class AbstractCommandPoller implements CommandPoller {

    protected final Log log = LogFactory.getLog(CommandPoller.class);

    private static final long DEFAULT_POLLING_INTERVAL = 5000L;

    private static final int DEFAULT_MAX_THREADS = Runtime.getRuntime().availableProcessors();

    private final Daemon daemon;

    private CommandExecutor executor;

    private long pollingInterval;

    private int maxThreads;

    private boolean requeue;

    public AbstractCommandPoller(Daemon daemon, DaemonPollerConfig pollerConfig) {
        this.daemon = daemon;

        this.pollingInterval = pollerConfig.getLong(DaemonPollerConfig.pollingInterval, DEFAULT_POLLING_INTERVAL);
        this.maxThreads = pollerConfig.getInt(DaemonPollerConfig.maxThreads, DEFAULT_MAX_THREADS);
        this.requeue = pollerConfig.getBoolean(DaemonPollerConfig.requeue);

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
    public boolean isRequeue() {
        return requeue;
    }

}
