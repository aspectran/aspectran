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
package com.aspectran.daemon.command.polling;

import com.aspectran.core.context.config.DaemonPollingConfig;
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
 * File system-based commander.
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public class DefaultFileCommander extends AbstractFileCommander {

    protected final Logger logger = LoggerFactory.getLogger(DefaultFileCommander.class);

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

    public DefaultFileCommander(Daemon daemon, DaemonPollingConfig pollingConfig) throws Exception {
        super(daemon, pollingConfig);

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

            String incomingPath = pollingConfig.getIncoming(DEFAULT_INCOMING_PATH);
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

            File[] incomingFiles = retrieveCommandFiles(incomingDir);
            if (incomingFiles != null) {
                for (File file : incomingFiles) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Delete old incoming command file: " + file);
                    }
                    file.delete();
                }
            }
        } catch (Exception e) {
            throw new Exception("Could not create directory structure for file commander", e);
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
        File[] queuedFiles = retrieveCommandFiles(queuedDir);
        if (queuedFiles != null) {
            if (isRequeuable()) {
                for (File file : queuedFiles) {
                    CommandParameters parameters = readCommandFile(file);
                    if (parameters != null) {
                        if (parameters.isRequeuable()) {
                            writeIncomingCommand(parameters, file.getName());
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
        File[] files = retrieveCommandFiles(incomingDir);
        if (files != null) {
            int limit = getCommandExecutor().getAvailableThreads();
            for (int i = 0; i < files.length && i < limit; i++) {
                File file = files[i];
                CommandParameters parameters = readCommandFile(file);
                if (parameters != null) {
                    String incomingFileName = file.getName();
                    String queuedFileName = writeQueuedCommand(parameters, incomingFileName);
                    if (queuedFileName != null) {
                        removeCommandFile(incomingDir, incomingFileName);
                        executeQueuedCommand(parameters, queuedFileName);
                    }
                }
            }
        }
    }

    private void executeQueuedCommand(final CommandParameters parameters, final String fileName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Execute Command: " + fileName + "\n" + parameters);
        }
        getCommandExecutor().execute(parameters, new CommandExecutor.Callback() {
            @Override
            public void success() {
                removeCommandFile(queuedDir, fileName);
                writeCompletedCommand(parameters, makeFileName());
                if (logger.isTraceEnabled()) {
                    logger.trace("Result of Completed Command: " + fileName + "\n" + parameters);
                }
            }

            @Override
            public void failure() {
                removeCommandFile(queuedDir, fileName);
                writeFailedCommand(parameters, makeFileName());
                if (logger.isTraceEnabled()) {
                    logger.trace("Result of Failed Command: " + fileName + "\n" + parameters);
                }
            }

            private String makeFileName() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssSSS");
                String datetime = formatter.format(LocalDateTime.now());
                return datetime + "_" + fileName;
            }
        });
    }

    private File[] retrieveCommandFiles(File dir) {
        File[] files = dir.listFiles((file) -> (file.isFile() && file.getName().toLowerCase().endsWith(".apon")));
        if (files != null && files.length > 0) {
            Arrays.sort(files, Comparator.comparing(File::getName));
        }
        return files;
    }

    private CommandParameters readCommandFile(File file) {
        if (logger.isTraceEnabled()) {
            logger.trace("Read command file: " + file);
        }
        try {
            CommandParameters parameters = new CommandParameters();
            AponReader.parse(file, parameters);
            return parameters;
        } catch (IOException e) {
            logger.error("Failed to read command file: " + file, e);
            removeCommandFile(incomingDir, file.getName());
            return null;
        }
    }

    private void writeIncomingCommand(CommandParameters parameters, String fileName) {
        String written = writeCommandFile(incomingDir, fileName, parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Incoming Command: " + written + " in " + incomingDir);
        }
    }

    private String writeQueuedCommand(CommandParameters parameters, String fileName) {
        String written = writeCommandFile(queuedDir, fileName, parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Queued Command: " + written + " in " + queuedDir);
        }
        return written;
    }

    private void writeCompletedCommand(CommandParameters parameters, String fileName) {
        String written = writeCommandFile(completedDir, fileName, parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Completed Command: " + written + " in " + completedDir);
        }
    }

    private void writeFailedCommand(CommandParameters parameters, String fileName) {
        String written = writeCommandFile(failedDir, fileName, parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Failed Command: " + written + " in " + failedDir);
        }
    }

    private String writeCommandFile(File dir, String fileName, CommandParameters parameters) {
        File file = null;
        try {
            synchronized (lock) {
                file = FilenameUtils.generateUniqueFile(new File(dir, fileName));
                file.createNewFile();
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Write command file: " + file);
            }
            AponWriter aponWriter = new AponWriter(file).nullWritable(false);
            aponWriter.write(parameters);
            aponWriter.close();
            return file.getName();
        } catch (IOException e) {
            if (file != null) {
                logger.warn("Failed to write command file: " + file, e);
            } else {
                File f = new File(dir, fileName);
                logger.warn("Failed to write command file: " + f, e);
            }
            return null;
        }
    }

    private void removeCommandFile(File dir, String fileName) {
        File file = new File(dir, fileName);
        if (logger.isTraceEnabled()) {
            logger.trace("Delete command file: " + file);
        }
        if (!file.delete()) {
            logger.warn("Failed to delete command file: " + file);
        }
    }

}
