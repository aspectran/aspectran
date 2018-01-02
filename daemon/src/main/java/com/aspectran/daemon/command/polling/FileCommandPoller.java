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
import com.aspectran.core.util.FilenameUtils;
import com.aspectran.core.util.apon.AponParsingFailedException;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.daemon.Daemon;

import java.io.File;
import java.io.IOException;
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

            File queuedDir = new File(inboundDir, INBOUND_QUEUED_DIR);
            queuedDir.mkdir();
            this.queuedDir = queuedDir;

            File completedDir = new File(inboundDir, INBOUND_COMPLETED_DIR);
            completedDir.mkdir();
            this.completedDir = completedDir;

            File failedDir = new File(inboundDir, INBOUND_FAILED_DIR);
            failedDir.mkdir();
            this.failedDir = failedDir;
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon command poller", e);
        }
    }

    @Override
    public void polling() {
        File[] files = getInboundFiles();
        for (int i = 0; i < files.length && i < getMaxThreads() - getExecutor().getQueueSize(); i++) {
            File file = files[i];
            final CommandParameters parameters = readCommandFile(file);
            if (parameters != null) {
                final String queuedFileName = writeCommandFile(queuedDir, file.getName(), parameters);
                removeCommandFile(inboundDir, file.getName());
                if (queuedFileName != null) {
                    getExecutor().execute(parameters, new CommandExecutor.Callback() {
                        @Override
                        public void success() {
                            removeCommandFile(queuedDir, queuedFileName);
                            writeCommandFile(completedDir, queuedFileName, parameters);
                        }

                        @Override
                        public void failure() {
                            removeCommandFile(queuedDir, queuedFileName);
                            writeCommandFile(failedDir, queuedFileName, parameters);
                        }
                    });
                }
            }
        }
    }

    private File[] getInboundFiles() {
        File[] files = inboundDir.listFiles((file) -> (file.isFile() && file.getName().toLowerCase().endsWith(".apon")));
        if (files.length > 0) {
            Arrays.sort(files, (f1, f2) -> ((File)f1).getName().compareTo(((File)f2).getName()));
        }
        return files;
    }

    private CommandParameters readCommandFile(File file) {
        if (log.isDebugEnabled()) {
            log.debug("Read command file: " + file.getAbsolutePath());
        }
        try {
            CommandParameters parameters = new CommandParameters();
            AponReader.parse(file, parameters);
            return parameters;
        } catch (AponParsingFailedException e) {
            log.warn("Failed to read command file: " + file.getAbsolutePath(), e);
            return null;
        }
    }

    private String writeCommandFile(File targetDir, String fileName, CommandParameters parameters) {
        synchronized (this) {
            File completedFile = null;
            try {
                completedFile = FilenameUtils.getUniqueFile(new File(targetDir, fileName));
                if (log.isDebugEnabled()) {
                    log.debug("Write command file: " + completedFile.getAbsolutePath());
                }
                AponWriter aponWriter = new AponWriter(completedFile);
                aponWriter.setPrettyPrint(true);
                aponWriter.setIndentString("    ");
                aponWriter.write(parameters);
                aponWriter.close();
                return completedFile.getName();
            } catch (IOException e) {
                if (completedFile != null) {
                    log.warn("Failed to write command file: " + completedFile.getAbsolutePath(), e);
                } else {
                    File f = new File(targetDir, fileName);
                    log.warn("Failed to write command file: " + f.getAbsolutePath(), e);
                }
                return null;
            }
        }
    }

    private void removeCommandFile(File targetDir, String fileName) {
        File file = new File(targetDir, fileName);
        if (log.isDebugEnabled()) {
            log.debug("Delete command file: " + file.getAbsolutePath());
        }
        if (!file.delete()) {
            log.warn("Failed to delete command file: " + file.getAbsolutePath());
        }
    }

}
