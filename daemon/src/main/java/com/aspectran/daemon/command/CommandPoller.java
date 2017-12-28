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
import java.util.Arrays;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public class CommandPoller {

    private static final long DEFAULT_POLLING_INTERVAL = 5000L;

    public static final int DEFAULT_MAX_THREADS = 5;

    private static final String DEFAULT_INBOUND_PATH = "/inbound";

    private static final String INBOUND_QUEUED_DIR = "queued";

    private static final String INBOUND_COMPLETED_DIR = "completed";

    private static final String INBOUND_FAILED_DIR = "failed";

    private static final String INBOUND_TRASH_DIR = "trash";

    private final Daemon daemon;

    private long pollingInterval = DEFAULT_POLLING_INTERVAL;

    private int maxThreads = DEFAULT_MAX_THREADS;

    private File inboundDir;

    private File queuedDir;

    private File completedDir;

    private File failedDir;

    private File trashDir;

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

    public File getInboundDir() {
        return inboundDir;
    }

    public File getQueuedDir() {
        return queuedDir;
    }

    public File getCompletedDir() {
        return completedDir;
    }

    public File getFailedDir() {
        return failedDir;
    }

    public File getTrashDir() {
        return trashDir;
    }

    public void init(DaemonPollerConfig pollerConfig) throws Exception {
        try {
            this.pollingInterval = pollerConfig.getLong(DaemonPollerConfig.pollingInterval, DEFAULT_POLLING_INTERVAL);
            this.maxThreads = pollerConfig.getInt(DaemonPollerConfig.maxThreads, DEFAULT_MAX_THREADS);

            daemon.getCommander().setMaxThreads(maxThreads);

            String basePath = daemon.getService().getActivityContext().getEnvironment().getBasePath();
            String inboundPath = pollerConfig.getString(DaemonPollerConfig.inbound, DEFAULT_INBOUND_PATH);
            File inboundDir = new File(basePath, inboundPath);
            inboundDir.mkdirs();
            this.inboundDir = inboundDir;

            File queuedDir = new File(inboundPath, INBOUND_QUEUED_DIR);
            queuedDir.mkdir();
            this.queuedDir = queuedDir;

            File completedDir = new File(inboundPath, INBOUND_COMPLETED_DIR);
            completedDir.mkdir();
            this.completedDir = completedDir;

            File failedDir = new File(inboundPath, INBOUND_FAILED_DIR);
            failedDir.mkdir();
            this.failedDir = failedDir;

            File trashDir = new File(inboundPath, INBOUND_TRASH_DIR);
            trashDir.mkdir();
            this.trashDir = trashDir;
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon command poller", e);
        }
    }

    public void polling() {
        File[] commandFiles = getInbounds();
        daemon.getCommander().execute(commandFiles);
    }

    private File[] getInbounds() {
        File[] files = inboundDir.listFiles((file, name) -> (file.isFile() && name.toLowerCase().endsWith(".apon")));
        Arrays.sort(files, (f1, f2) -> ((File)f1).getName().compareTo(((File)f2).getName()));
        return files;
    }

}
