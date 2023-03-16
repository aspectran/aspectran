/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.core.util.FilenameUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandParameters;

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
public class DefaultCommandFilePoller extends AbstractCommandFilePoller {

    protected final Logger logger = LoggerFactory.getLogger(DefaultCommandFilePoller.class);

    private static final String COMMANDS_PATH = "/cmd";

    private static final String QUEUED_PATH = COMMANDS_PATH + "/queued";

    private static final String COMPLETED_PATH = COMMANDS_PATH + "/completed";

    private static final String FAILED_PATH = COMMANDS_PATH + "/failed";

    private static final String DEFAULT_INCOMING_PATH = COMMANDS_PATH + "/incoming";

    private final Object lock = new Object();

    private final File incomingDir;

    private final File queuedDir;

    private final File completedDir;

    private final File failedDir;

    public DefaultCommandFilePoller(Daemon daemon, DaemonPollerConfig pollerConfig) throws Exception {
        super(daemon, pollerConfig);

        try {
            File commandsDir = new File(getDaemon().getBasePath(), COMMANDS_PATH);
            commandsDir.mkdirs();

            File queuedDir = new File(getDaemon().getBasePath(), QUEUED_PATH);
            queuedDir.mkdirs();
            this.queuedDir = queuedDir;

            File completedDir = new File(getDaemon().getBasePath(), COMPLETED_PATH);
            completedDir.mkdirs();
            this.completedDir = completedDir;

            File failedDir = new File(getDaemon().getBasePath(), FAILED_PATH);
            failedDir.mkdirs();
            this.failedDir = failedDir;

            String incomingPath = pollerConfig.getIncoming(DEFAULT_INCOMING_PATH);
            File incomingDir;
            if (incomingPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
                // Using url fully qualified paths
                URI uri = URI.create(incomingPath);
                incomingDir = new File(uri);
            } else {
                incomingDir = new File(getDaemon().getBasePath(), incomingPath);
            }
            incomingDir.mkdirs();
            this.incomingDir = incomingDir;

            File[] incomingFiles = getCommandFiles(incomingDir);
            if (incomingFiles != null) {
                for (File file : incomingFiles) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon command poller", e);
        }
    }

    public File getIncomingDir() {
        return incomingDir;
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
            if (isRequeuable()) {
                for (File file : queuedFiles) {
                    CommandParameters parameters = readCommandFile(file);
                    if (parameters != null) {
                        if (parameters.isRequeuable()) {
                            writeCommandFile(incomingDir, file.getName(), parameters);
                        }
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
        File[] files = getCommandFiles(incomingDir);
        if (files != null) {
            int limit = getMaxThreads() - getExecutor().getQueueSize();
            for (int i = 0; i < files.length && i < limit; i++) {
                File file = files[i];
                CommandParameters parameters = readCommandFile(file);
                if (parameters != null) {
                    String incomingFileName = file.getName();
                    String queuedFileName = writeCommandFile(queuedDir, incomingFileName, parameters);
                    if (queuedFileName != null) {
                        removeCommandFile(incomingDir, incomingFileName);
                        executeQueuedCommand(parameters, queuedFileName);
                    }
                }
            }
        }
    }

    private void executeQueuedCommand(final CommandParameters parameters, final String queuedFileName) {
        getExecutor().execute(parameters, new CommandExecutor.Callback() {
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
        if (logger.isDebugEnabled()) {
            logger.debug("Read command file: " + file.getAbsolutePath());
        }
        try {
            CommandParameters parameters = new CommandParameters();
            AponReader.parse(file, parameters);
            return parameters;
        } catch (IOException e) {
            logger.error("Failed to read command file: " + file.getAbsolutePath(), e);
            removeCommandFile(incomingDir, file.getName());
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
            if (logger.isDebugEnabled()) {
                logger.debug("Write command file: " + file.getAbsolutePath());
            }
            AponWriter aponWriter = new AponWriter(file).nullWritable(false);
            aponWriter.write(parameters);
            aponWriter.close();
            return file.getName();
        } catch (IOException e) {
            if (file != null) {
                logger.warn("Failed to write command file: " + file.getAbsolutePath(), e);
            } else {
                File f = new File(dir, fileName);
                logger.warn("Failed to write command file: " + f.getAbsolutePath(), e);
            }
            return null;
        }
    }

    private void removeCommandFile(File dir, String fileName) {
        File file = new File(dir, fileName);
        if (logger.isDebugEnabled()) {
            logger.debug("Delete command file: " + file.getAbsolutePath());
        }
        if (!file.delete()) {
            logger.warn("Failed to delete command file: " + file.getAbsolutePath());
        }
    }

}
