/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.apon.AponReader;
import com.aspectran.utils.apon.AponWriterCloseable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The default {@link FileCommander} implementation that uses the local filesystem
 * as a command queue.
 * <p>
 * This commander manages a directory structure for processing command files:
 * <ul>
 *   <li>{@code /cmd/incoming}: New command files are placed here to be picked up by the poller.</li>
 *   <li>{@code /cmd/queued}: Command files are moved here while they are being processed.</li>
 *   <li>{@code /cmd/completed}: Successfully processed command files are moved here.</li>
 *   <li>{@code /cmd/failed}: Command files that failed during processing are moved here.</li>
 * </ul>
 * The polling cycle retrieves command files from the incoming directory, moves them
 * to the queued directory, executes them via the {@link CommandExecutor}, and then
 * moves them to either the completed or failed directory.
 * If re-queuing is enabled, commands in the queued directory can be moved back to
 * the incoming directory upon restart.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public class DefaultFileCommander extends AbstractFileCommander {

    protected final Logger logger = LoggerFactory.getLogger(DefaultFileCommander.class);

    private static final String COMMANDS_PATH = "cmd";

    private static final String QUEUED_PATH = "queued";

    private static final String COMPLETED_PATH = "completed";

    private static final String FAILED_PATH = "failed";

    private static final String DEFAULT_INCOMING_PATH = COMMANDS_PATH + "/incoming";

    private final Object lock = new Object();

    private final Path incomingDir;

    private final Path queuedDir;

    private final Path completedDir;

    private final Path failedDir;

    /**
     * Instantiates a new DefaultFileCommander.
     * @param daemon the daemon that owns this commander
     * @param pollingConfig the polling configuration
     * @throws Exception if an error occurs while creating the directory structure
     */
    public DefaultFileCommander(Daemon daemon, DaemonPollingConfig pollingConfig) throws Exception {
        super(daemon, pollingConfig);

        try {
            Path basePath = Paths.get(getDaemon().getBasePath());
            Path cmdDir = basePath.resolve(COMMANDS_PATH);

            this.queuedDir = cmdDir.resolve(QUEUED_PATH);
            Files.createDirectories(this.queuedDir);

            this.completedDir = cmdDir.resolve(COMPLETED_PATH);
            Files.createDirectories(this.completedDir);

            this.failedDir = cmdDir.resolve(FAILED_PATH);
            Files.createDirectories(this.failedDir);

            String incomingPath = pollingConfig.getIncoming(DEFAULT_INCOMING_PATH);
            if (incomingPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
                // Using url fully qualified paths
                this.incomingDir = Paths.get(URI.create(incomingPath));
            } else {
                // Resolve against basePath, not cmdDir, because incomingPath
                // from config may already contain the '/cmd/' prefix
                if (incomingPath.startsWith("/")) {
                    incomingPath = incomingPath.substring(1);
                }
                this.incomingDir = basePath.resolve(incomingPath);
            }
            Files.createDirectories(this.incomingDir);

            List<Path> incomingFiles = retrieveCommandFiles(this.incomingDir);
            if (incomingFiles != null) {
                for (Path file : incomingFiles) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Delete old incoming command file: {}", file);
                    }
                    Files.deleteIfExists(file);
                }
            }
        } catch (Exception e) {
            throw new Exception("Could not create directory structure for file commander", e);
        }
    }

    /**
     * Returns the directory where incoming command files are placed.
     * @return the incoming command directory
     */
    public Path getIncomingDir() {
        return incomingDir;
    }

    /**
     * Returns the directory where command files are moved while being processed.
     * @return the queued command directory
     */
    public Path getQueuedDir() {
        return queuedDir;
    }

    /**
     * Returns the directory where successfully processed command files are moved.
     * @return the completed command directory
     */
    public Path getCompletedDir() {
        return completedDir;
    }

    /**
     * Returns the directory where failed command files are moved.
     * @return the failed command directory
     */
    public Path getFailedDir() {
        return failedDir;
    }

    @Override
    public void requeue() {
        List<Path> queuedFiles = retrieveCommandFiles(queuedDir);
        if (queuedFiles != null) {
            if (isRequeuable()) {
                for (Path file : queuedFiles) {
                    CommandParameters parameters = readCommandFile(file);
                    if (parameters != null) {
                        if (parameters.isRequeuable()) {
                            try {
                                Path incomingFile = incomingDir.resolve(file.getFileName());
                                incomingFile = FilenameUtils.generateUniqueFile(incomingFile.toFile()).toPath();
                                Files.move(file, incomingFile, StandardCopyOption.REPLACE_EXISTING);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Re-queued Command: {} in {}", incomingFile.getFileName(), incomingDir);
                                }
                            } catch (IOException e) {
                                logger.warn("Failed to re-queue command file: {}", file, e);
                                removeCommandFile(file);
                            }
                        } else {
                            removeCommandFile(file);
                        }
                    } else {
                        // Malformed file already handled in readCommandFile
                    }
                }
            } else {
                for (Path file : queuedFiles) {
                    removeCommandFile(file);
                }
            }
        }
    }

    @Override
    public void polling() {
        List<Path> files = retrieveCommandFiles(incomingDir);
        if (files != null) {
            int limit = getCommandExecutor().getAvailableThreads();
            for (int i = 0; i < files.size() && i < limit; i++) {
                Path file = files.get(i);
                CommandParameters parameters = readCommandFile(file);
                if (parameters != null) {
                    try {
                        Path queuedFile = queuedDir.resolve(file.getFileName());
                        queuedFile = FilenameUtils.generateUniqueFile(queuedFile.toFile()).toPath();

                        // Move to queued directory
                        Files.move(file, queuedFile, StandardCopyOption.REPLACE_EXISTING);

                        String queuedFileName = queuedFile.getFileName().toString();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Queued Command: {} in {}", queuedFileName, queuedDir);
                        }

                        if (!executeQueuedCommand(parameters, queuedFileName)) {
                            // Rollback if executor rejected the command
                            Files.move(queuedFile, file, StandardCopyOption.REPLACE_EXISTING);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Command execution rejected, rolled back to incoming: {}", file.getFileName());
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Failed to process incoming command file: {}", file, e);
                    }
                }
            }
        }
    }

    private boolean executeQueuedCommand(CommandParameters parameters, String fileName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Execute Command: {}{}{}", fileName, System.lineSeparator(), parameters);
        }
        return getCommandExecutor().execute(parameters, new CommandExecutor.Callback() {
            @Override
            public void success() {
                removeCommandFile(queuedDir.resolve(fileName));
                writeCompletedCommand(parameters, makeFileName());
                if (logger.isTraceEnabled()) {
                    logger.trace("Result of Completed Command: {}{}{}", fileName, System.lineSeparator(), parameters);
                }
            }

            @Override
            public void failure() {
                removeCommandFile(queuedDir.resolve(fileName));
                writeFailedCommand(parameters, makeFileName());
                if (logger.isTraceEnabled()) {
                    logger.trace("Result of Failed Command: {}{}{}", fileName, System.lineSeparator(), parameters);
                }
            }

            @NonNull
            private String makeFileName() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssSSS");
                String datetime = formatter.format(LocalDateTime.now());
                return datetime + "_" + fileName;
            }
        });
    }

    @Nullable
    private List<Path> retrieveCommandFiles(@NonNull Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.apon")) {
            List<Path> files = new ArrayList<>();
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    files.add(path);
                }
            }
            files.sort(Comparator.comparing(Path::getFileName));
            return (files.isEmpty() ? null : files);
        } catch (IOException e) {
            logger.warn("Failed to retrieve command files from directory: {}", dir, e);
            return null;
        }
    }

    @Nullable
    private CommandParameters readCommandFile(@NonNull Path file) {
        if (logger.isTraceEnabled()) {
            logger.trace("Read command file: {}", file);
        }
        try {
            CommandParameters parameters = new CommandParameters();
            AponReader.read(file.toFile(), parameters);
            return parameters;
        } catch (Exception e) {
            logger.error("Failed to read command file: {}", file, e);
            handleMalformedFile(file, e);
            return null;
        }
    }

    private void handleMalformedFile(@NonNull Path file, Exception e) {
        try {
            String source = Files.readString(file);
            removeCommandFile(file);

            CommandParameters parameters = new CommandParameters();
            parameters.setResult("[FAILED] Malformed command file: " + file.getFileName());
            parameters.setError(ExceptionUtils.getStacktrace(e));
            parameters.setSource(source);

            writeCommandFile(failedDir, file.getFileName().toString(), parameters);
            if (logger.isWarnEnabled()) {
                logger.warn("Malformed command file handled and moved to failed directory: {}", file.getFileName());
            }
        } catch (IOException ioe) {
            logger.error("Failed to handle malformed command file: {}", file, ioe);
            removeCommandFile(file);
        }
    }

    private void writeCompletedCommand(CommandParameters parameters, String fileName) {
        String written = writeCommandFile(completedDir, fileName, parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Completed Command: {} in {}", written, completedDir);
        }
    }

    private void writeFailedCommand(CommandParameters parameters, String fileName) {
        String written = writeCommandFile(failedDir, fileName, parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Failed Command: {} in {}", written, failedDir);
        }
    }

    @Nullable
    private String writeCommandFile(@NonNull Path dir, String fileName, CommandParameters parameters) {
        Path file = null;
        try {
            synchronized (lock) {
                file = FilenameUtils.generateUniqueFile(dir.resolve(fileName).toFile()).toPath();
                Files.createFile(file);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Write command file: {}", file);
            }
            try (AponWriterCloseable aponWriter = new AponWriterCloseable(file.toFile()).nullWritable(false)) {
                aponWriter.write(parameters);
            }
            return file.getFileName().toString();
        } catch (IOException e) {
            if (file != null) {
                logger.warn("Failed to write command file: {}", file, e);
            } else {
                Path f = dir.resolve(fileName);
                logger.warn("Failed to write command file: {}", f, e);
            }
            return null;
        }
    }

    private void removeCommandFile(Path file) {
        if (logger.isTraceEnabled()) {
            logger.trace("Delete command file: {}", file);
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.warn("Failed to delete command file: {}", file, e);
        }
    }

}
