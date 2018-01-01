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
package com.aspectran.daemon.command.polling;

import com.aspectran.core.context.config.DaemonPollerConfig;
import com.aspectran.core.util.apon.AponParsingFailedException;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.daemon.Daemon;

import java.io.File;
import java.util.Arrays;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public class FileCommandPoller extends AbstractCommandPoller {

    private static final String DEFAULT_INBOUND_PATH = "/inbound";

    private static final String INBOUND_QUEUED_DIR = "queued";

    private static final String INBOUND_COMPLETED_DIR = "completed";

    private static final String INBOUND_FAILED_DIR = "failed";

    private final File inboundDir;

    private final File queuedDir;

    private final File completedDir;

    private final File failedDir;

    public FileCommandPoller(Daemon daemon, DaemonPollerConfig pollerConfig) throws Exception {
        super(daemon, pollerConfig);

        try {
            String basePath = getDaemon().getService().getActivityContext().getEnvironment().getBasePath();
            String inboundPath = pollerConfig.getString(DaemonPollerConfig.inbound, DEFAULT_INBOUND_PATH);
            File inboundDir = new File(basePath, inboundPath);
            inboundDir.mkdirs();
            this.inboundDir = inboundDir;

            System.out.println(inboundDir);
            System.out.println(inboundDir.getCanonicalPath());

            File queuedDir = new File(inboundPath, INBOUND_QUEUED_DIR);
            queuedDir.mkdir();
            this.queuedDir = queuedDir;

            File completedDir = new File(inboundPath, INBOUND_COMPLETED_DIR);
            completedDir.mkdir();
            this.completedDir = completedDir;

            File failedDir = new File(inboundPath, INBOUND_FAILED_DIR);
            failedDir.mkdir();
            this.failedDir = failedDir;
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon command poller", e);
        }
    }

    @Override
    public void polling() {
        File[] files = getInboundFiles();
        System.out.println(files.length);
        for (File file : files) {
            System.out.println(file);
        }
        for (int i = 0; i < files.length && i < getMaxThreads() - getExecutor().getQueueSize(); i++) {
            final File file = files[i];
            CommandParameters parameters = new CommandParameters();
            try {
                AponReader.parse(file, parameters);
            } catch (AponParsingFailedException e) {
                // TODO logging
                e.printStackTrace();
            }
            //TODO queuing
            CommandExecutor.Callback callback = new CommandExecutor.Callback() {
                @Override
                public void success() {

                }

                @Override
                public void failure() {

                }
            };
            getExecutor().execute(parameters, callback);
        }
    }

    private File[] getInboundFiles() {
        //File[] files = inboundDir.listFiles((file, name) -> (file.isFile() && name.toLowerCase().endsWith(".apon")));
        File[] files = inboundDir.listFiles((file, name) -> (name.toLowerCase().endsWith(".apon")));
        //File[] files = inboundDir.listFiles();
        if (files.length > 0) {
            Arrays.sort(files, (f1, f2) -> ((File)f1).getName().compareTo(((File)f2).getName()));
        }
        return files;
    }

}
