/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.daemon.Daemon;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * File system-based command poller.
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public class FileCommandPoller extends AbstractCommandPoller {

    protected final Log log = LogFactory.getLog(FileCommandPoller.class);

    private static final String DEFAULT_INBOUND_PATH = "/inbound";

    private static final String INBOUND_QUEUED_DIR = "queued";

    private static final String INBOUND_COMPLETED_DIR = "completed";

    private static final String INBOUND_FAILED_DIR = "failed";

    private final Object lock = new Object();

    private final File inboundDir;

    private final File queuedDir;

    private final File completedDir;

    private final File failedDir;

    public FileCommandPoller(Daemon daemon, DaemonPollerConfig pollerConfig) throws Exception {
        super(daemon, pollerConfig);

        try {
            String basePath = (getDaemon().getService() != null ? getDaemon().getService().getBasePath() : null);
            String inboundPath = pollerConfig.getInboundPath(DEFAULT_INBOUND_PATH);
            File inboundDir;
            if (inboundPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
                // Using url fully qualified paths
                URI uri = URI.create(inboundPath);
                inboundDir = new File(uri);
            } else {
                if (basePath != null) {
                    inboundDir = new File(basePath, inboundPath);
                } else {
                    inboundDir = new File(inboundPath);
                }
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

    @Override
    public void requeue() {
        File[] queuedFiles = getCommandFiles(queuedDir);
        if (queuedFiles != null) {
            if (isRequeue()) {
                for (File file : queuedFiles) {
                    CommandParameters parameters = readCommandFile(file);
                    if (parameters != null) {
                        writeCommandFile(inboundDir, file.getName(), parameters);
                        removeCommandFile(queuedDir, file.getName());
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
                    if (queuedFileName != null) {
                        removeCommandFile(inboundDir, inboundFileName);
                        executeQueuedCommand(parameters, queuedFileName);
                    }
                }
            }
        }
    }

    private boolean executeQueuedCommand(final CommandParameters parameters, final String queuedFileName) {
        return getExecutor().execute(parameters, new CommandExecutor.Callback() {
            @Override
            public void success() {
                removeCommandFile(queuedDir, queuedFileName);
                writeCommandFile(completedDir, makeFileName(), parameters);
            }

            @Override
            public void failure() {
                removeCommandFile(queuedDir, queuedFileName);
                writeCommandFile(failedDir, makeFileName(), parameters);
            }

            private String makeFileName() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssSSS");
                String datetime = formatter.format(LocalDateTime.now());
                return datetime + "_" + queuedFileName;
            }
        });
    }

    private File[] getCommandFiles(File dir) {
        File[] files = dir.listFiles((file) -> (file.isFile() && file.getName().toLowerCase().endsWith(".apon")));
        if (files != null && files.length > 0) {
            Arrays.sort(files, Comparator.comparing(File::getName));
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
        } catch (IOException e) {
            log.error("Failed to read command file: " + file.getAbsolutePath(), e);
            removeCommandFile(inboundDir, file.getName());
            return null;
        }
    }

    private String writeCommandFile(File dir, String fileName, CommandParameters parameters) {
        File file = null;
        try {
            synchronized (lock) {
                file = FilenameUtils.generateUniqueFile(new File(dir, fileName));
                file.createNewFile();
            }
            if (log.isDebugEnabled()) {
                log.debug("Write command file: " + file.getAbsolutePath());
            }
            AponWriter aponWriter = new AponWriter(file);
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
