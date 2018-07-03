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
import com.aspectran.core.util.FilenameUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.AponParseException;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.daemon.Daemon;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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

    private final Object lock = new Object();

    public FileCommandPoller(Daemon daemon, DaemonPollerConfig pollerConfig) throws Exception {
        super(daemon, pollerConfig);

        try {
            String basePath = getDaemon().getService().getActivityContext().getEnvironment().getBasePath();
            String inboundPath = pollerConfig.getString(DaemonPollerConfig.inbound, DEFAULT_INBOUND_PATH);
            File inboundDir;
            if (inboundPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
                // Using url fully qualified paths
                URI uri = URI.create(inboundPath);
                inboundDir = new File(uri);
            } else {
                inboundDir = new File(basePath, inboundPath);
            }
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

            File[] inboundFiles = getCommandFiles(inboundDir);
            if (inboundFiles != null) {
                for (File file : inboundFiles) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon command poller", e);
        }
    }

    @Override
    public void requeue() {
        File[] queuedFiles = getCommandFiles(queuedDir);
        if (queuedFiles != null) {
            if (isRequeue()) {
                for (File file : queuedFiles) {
                    CommandParameters parameters = readCommandFile(file);
                    if (parameters != null) {
                        executeQueuedCommand(parameters, file.getName(), null);
                    }
                }
            } else {
                for (File file : queuedFiles) {
                    removeCommandFile(queuedDir, file.getName());
                }
            }
        }
    }

    @Override
    public void polling() {
        File[] files = getCommandFiles(inboundDir);
        if (files != null) {
            int limit = getMaxThreads() - getExecutor().getQueueSize();
            for (int i = 0; i < files.length && i < limit; i++) {
                File file = files[i];
                CommandParameters parameters = readCommandFile(file);
                if (parameters != null) {
                    String inboundFileName = file.getName();
                    String queuedFileName = writeCommandFile(queuedDir, inboundFileName, parameters);
                    removeCommandFile(inboundDir, inboundFileName);
                    if (queuedFileName != null) {
                        executeQueuedCommand(parameters, queuedFileName, inboundFileName);
                    }
                }
            }
        }
    }

    private void executeQueuedCommand(final CommandParameters parameters, final String queuedFileName,
                                      final String inboundFileName) {
        getExecutor().execute(parameters, new CommandExecutor.Callback() {
            @Override
            public void success() {
                removeCommandFile(queuedDir, queuedFileName);
                writeCommandFile(completedDir, (inboundFileName != null ? inboundFileName : queuedFileName), parameters);
            }

            @Override
            public void failure() {
                removeCommandFile(queuedDir, queuedFileName);
                writeCommandFile(failedDir, (inboundFileName != null ? inboundFileName : queuedFileName), parameters);
            }
        });
    }

    private File[] getCommandFiles(File dir) {
        File[] files = dir.listFiles((file) -> (file.isFile() && file.getName().toLowerCase().endsWith(".apon")));
        if (files != null && files.length > 0) {
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
        } catch (AponParseException e) {
            log.warn("Failed to read command file: " + file.getAbsolutePath(), e);
            return null;
        }
    }

    private String writeCommandFile(File dir, String fileName, CommandParameters parameters) {
        File file = null;
        try {
            synchronized (lock) {
                file = FilenameUtils.getUniqueFile(new File(dir, fileName));
                file.createNewFile();
            }
            if (log.isDebugEnabled()) {
                log.debug("Write command file: " + file.getAbsolutePath());
            }
            AponWriter aponWriter = new AponWriter(file);
            aponWriter.setPrettyPrint(true);
            aponWriter.setIndentString("  ");
            aponWriter.write(parameters);
            aponWriter.close();
            return file.getName();
        } catch (IOException e) {
            if (file != null) {
                log.warn("Failed to write command file: " + file.getAbsolutePath(), e);
            } else {
                File f = new File(dir, fileName);
                log.warn("Failed to write command file: " + f.getAbsolutePath(), e);
            }
            return null;
        }
    }

    private void removeCommandFile(File dir, String fileName) {
        File file = new File(dir, fileName);
        if (log.isDebugEnabled()) {
            log.debug("Delete command file: " + file.getAbsolutePath());
        }
        if (!file.delete()) {
            log.warn("Failed to delete command file: " + file.getAbsolutePath());
        }
    }

}
