/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
package com.aspectran.daemon.command;

import com.aspectran.core.context.config.DaemonPollerConfig;
import com.aspectran.daemon.Daemon;

import java.io.File;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public class CommandPoller {

    private static final long DEFAULT_POLLING_INTERVAL = 5000L;

    private static final int DEFAULT_MAX_THREADS = 5;

    private static final String DEFAULT_INBOUND_PATH = "/inbound";

    private static final String INBOUND_QUEUED_DIR = "queued";

    private static final String INBOUND_COMPLETED_DIR = "completed";

    private static final String INBOUND_FAILED_DIR = "failed";

    private static final String INBOUND_TRASH_DIR = "trash";

    private final Daemon daemon;

    private long pollingInterval = DEFAULT_POLLING_INTERVAL;

    private int maxThreads = DEFAULT_MAX_THREADS;

    private File inboundPath;

    private File queuedPath;

    private File completedPath;

    private File failedPath;

    private File trashPath;

    public CommandPoller(Daemon daemon) {
        this.daemon = daemon;
    }

    public Daemon getDaemon() {
        return daemon;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public File getInboundPath() {
        return inboundPath;
    }

    public File getQueuedPath() {
        return queuedPath;
    }

    public File getCompletedPath() {
        return completedPath;
    }

    public File getFailedPath() {
        return failedPath;
    }

    public File getTrashPath() {
        return trashPath;
    }

    public void init(DaemonPollerConfig pollerConfig) throws Exception {
        try {
            this.pollingInterval = pollerConfig.getLong(DaemonPollerConfig.pollingInterval, DEFAULT_POLLING_INTERVAL);
            this.maxThreads = pollerConfig.getInt(DaemonPollerConfig.maxThreads, DEFAULT_MAX_THREADS);

            String basePath = daemon.getService().getActivityContext().getEnvironment().getBasePath();
            String inbound = pollerConfig.getString(DaemonPollerConfig.inbound, DEFAULT_INBOUND_PATH);
            File inboundPath = new File(basePath, inbound);
            inboundPath.mkdirs();
            this.inboundPath = inboundPath;

            File queuedPath = new File(inboundPath, INBOUND_QUEUED_DIR);
            queuedPath.mkdir();
            this.queuedPath = queuedPath;

            File completedPath = new File(inboundPath, INBOUND_COMPLETED_DIR);
            completedPath.mkdir();
            this.completedPath = completedPath;

            File failedPath = new File(inboundPath, INBOUND_FAILED_DIR);
            failedPath.mkdir();
            this.failedPath = failedPath;

            File trashPath = new File(inboundPath, INBOUND_TRASH_DIR);
            trashPath.mkdir();
            this.trashPath = trashPath;
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon command poller", e);
        }
    }

    public void polling() {

    }

}
